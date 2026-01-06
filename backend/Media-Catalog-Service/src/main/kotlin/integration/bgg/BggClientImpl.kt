package integration.bgg

import com.collektar.imagecache.ImageCacheClient
import domain.MediaItem
import domain.SearchResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

class BggClientImpl(
    private val httpClient: HttpClient,
    private val config: BggConfig,
    private val imageCacheClient: ImageCacheClient  // ✅ Add image cache client
) : BggClient {

    private val logger = LoggerFactory.getLogger(BggClientImpl::class.java)
    private val rateLimitMutex = Mutex()
    private var lastCallTime: Long = 0

    companion object {
        private const val BGG_MAX_ITEMS_PER_REQUEST = 20
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 5000L
        private const val USER_AGENT = "MediaCatalogService/1.0"
    }

    private suspend fun <T> rateLimited(block: suspend () -> T): T =
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastCallTime
            if (elapsed < config.minDelayMillis) {
                val waitTime = config.minDelayMillis - elapsed
                logger.debug("Rate limiting: waiting ${waitTime}ms")
                delay(waitTime)
            }
            val result = block()
            lastCallTime = System.currentTimeMillis()
            result
        }

    override suspend fun hotBoardGames(limit: Int, offset: Int): SearchResult = rateLimited {
        val hotXml = fetchWithRetry("hot") {
            httpClient.get("${config.baseUrl}/hot") {
                parameter("type", "boardgame")
                header(HttpHeaders.UserAgent, USER_AGENT)
                header(HttpHeaders.Accept, "application/xml")
                headerIfToken()
            }
        }

        val all = BggMapper.mapHotResponse(hotXml, imageCacheClient)
        val paged = all.drop(offset).take(limit)

        SearchResult(
            total = all.size,
            limit = limit,
            offset = offset,
            items = paged
        )
    }

    override suspend fun searchBoardGames(query: String, limit: Int, offset: Int): SearchResult = rateLimited {
        val searchXml = fetchWithRetry("search for '$query'") {
            httpClient.get("${config.baseUrl}/search") {
                parameter("query", query)
                parameter("type", "boardgame")
                header(HttpHeaders.UserAgent, USER_AGENT)
                header(HttpHeaders.Accept, "application/xml")
                headerIfToken()
            }
        }

        val (total, allIds) = BggMapper.parseSearchIds(searchXml)
        val pageIds = allIds.drop(offset).take(limit)

        if (pageIds.isEmpty()) {
            return@rateLimited SearchResult(
                total = total,
                limit = limit,
                offset = offset,
                items = emptyList()
            )
        }

        val fetched = getBoardGamesInternal(pageIds)
        val byId = fetched.associateBy { it.id.removePrefix("bgg:").toLongOrNull() }
        val ordered = pageIds.mapNotNull { id -> byId[id] }

        SearchResult(
            total = total,
            limit = limit,
            offset = offset,
            items = ordered
        )
    }

    override suspend fun getBoardGames(ids: List<Long>): List<MediaItem> = rateLimited {
        getBoardGamesInternal(ids)
    }

    private suspend fun getBoardGamesInternal(ids: List<Long>): List<MediaItem> {
        if (ids.isEmpty()) return emptyList()

        if (ids.size > BGG_MAX_ITEMS_PER_REQUEST) {
            logger.debug("Batching ${ids.size} items into chunks of $BGG_MAX_ITEMS_PER_REQUEST")

            val results = mutableListOf<MediaItem>()
            val chunks = ids.chunked(BGG_MAX_ITEMS_PER_REQUEST)

            chunks.forEachIndexed { index, batch ->
                val batchResults = fetchBoardGamesBatch(batch)
                results.addAll(batchResults)

                if (index < chunks.size - 1) {
                    logger.debug("Waiting ${config.minDelayMillis}ms before next batch")
                    delay(config.minDelayMillis)
                }
            }
            return results
        }

        return fetchBoardGamesBatch(ids)
    }

    private suspend fun fetchBoardGamesBatch(ids: List<Long>): List<MediaItem> {
        val idCsv = ids.joinToString(",")
        logger.debug("Fetching board game details for ${ids.size} IDs")

        val xml = fetchWithRetry("thing details for IDs: $idCsv") {
            httpClient.get("${config.baseUrl}/thing") {
                parameter("id", idCsv)
                parameter("stats", 1)
                header(HttpHeaders.UserAgent, USER_AGENT)
                header(HttpHeaders.Accept, "application/xml")
                headerIfToken()
            }
        }

        return BggMapper.mapThingListResponse(xml, imageCacheClient)  // ✅ Pass image cache
    }

    private suspend fun fetchWithRetry(
        operation: String,
        block: suspend () -> HttpResponse
    ): String {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                logger.debug("Attempting BGG request for $operation (attempt ${attempt + 1}/$MAX_RETRIES)")

                val response = block()
                val statusCode = response.status
                val body: String = response.body()

                logger.debug("Received response with status $statusCode and ${body.length} characters")

                if (!statusCode.isSuccess()) {
                    logger.warn("BGG returned non-success status: $statusCode for $operation")
                    if (statusCode == HttpStatusCode.TooManyRequests || statusCode.value == 429) {
                        logger.warn("Rate limited by BGG (429). Waiting longer...")
                        if (attempt < MAX_RETRIES - 1) {
                            delay(RETRY_DELAY_MS * 2)
                            return@repeat
                        }
                    }
                    if (attempt < MAX_RETRIES - 1) {
                        delay(RETRY_DELAY_MS)
                        return@repeat
                    } else {
                        throw IllegalStateException("BGG returned status $statusCode after $MAX_RETRIES attempts")
                    }
                }

                val trimmed = body.trim().trimStart('\uFEFF')

                if (trimmed.isEmpty()) {
                    logger.warn("Empty response from BGG for $operation (attempt ${attempt + 1}/$MAX_RETRIES), status was $statusCode")
                    if (attempt < MAX_RETRIES - 1) {
                        logger.info("Waiting ${RETRY_DELAY_MS}ms before retry...")
                        delay(RETRY_DELAY_MS)
                        return@repeat
                    } else {
                        throw IllegalStateException("BGG returned empty response after $MAX_RETRIES attempts for $operation")
                    }
                }

                if (!trimmed.startsWith("<?xml") && !trimmed.startsWith("<")) {
                    val preview = trimmed.take(200)
                    logger.warn("Non-XML response from BGG for $operation (status $statusCode): $preview")
                    if (attempt < MAX_RETRIES - 1) {
                        logger.info("Waiting ${RETRY_DELAY_MS}ms before retry...")
                        delay(RETRY_DELAY_MS)
                        return@repeat
                    } else {
                        throw IllegalStateException("BGG returned non-XML response: $preview")
                    }
                }

                logger.debug("Successfully received valid XML response for $operation")
                return trimmed

            } catch (e: Exception) {
                lastException = e
                logger.warn("Request failed for $operation (attempt ${attempt + 1}/$MAX_RETRIES): ${e.message}")
                if (attempt < MAX_RETRIES - 1) {
                    logger.info("Waiting ${RETRY_DELAY_MS}ms before retry...")
                    delay(RETRY_DELAY_MS)
                }
            }
        }

        throw IllegalStateException("Failed to fetch from BGG after $MAX_RETRIES attempts for $operation", lastException)
    }

    private fun HttpRequestBuilder.headerIfToken() {
        config.token?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}