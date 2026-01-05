package integration.bgg

import domain.MediaItem
import domain.SearchResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory

class BggClientImpl(
    private val httpClient: HttpClient,
    private val config: BggConfig
) : BggClient {

    private val logger = LoggerFactory.getLogger(BggClientImpl::class.java)
    private val rateLimitMutex = Mutex()
    private var lastCallTime: Long = 0

    companion object {
        private const val BGG_MAX_ITEMS_PER_REQUEST = 20
    }

    private suspend fun <T> rateLimited(block: suspend () -> T): T =
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastCallTime
            if (elapsed < config.minDelayMillis) delay(config.minDelayMillis - elapsed)
            val result = block()
            lastCallTime = System.currentTimeMillis()
            result
        }

    override suspend fun hotBoardGames(limit: Int, offset: Int): SearchResult = rateLimited {
        val hotXml: String = httpClient.get("${config.baseUrl}/hot") {
            parameter("type", "boardgame")
            headerIfToken()
        }.body()

        val all = BggMapper.mapHotResponse(hotXml)
        val paged = all.drop(offset).take(limit)

        SearchResult(
            total = all.size,
            limit = limit,
            offset = offset,
            items = paged
        )
    }

    override suspend fun searchBoardGames(query: String, limit: Int, offset: Int): SearchResult = rateLimited {
        val searchXml: String = httpClient.get("${config.baseUrl}/search") {
            parameter("query", query)
            parameter("type", "boardgame")
            headerIfToken()
        }.body()

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

        // ✅ Call internal method without additional rate limiting
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

    // Public API with rate limiting
    override suspend fun getBoardGames(ids: List<Long>): List<MediaItem> = rateLimited {
        getBoardGamesInternal(ids)
    }

    // ✅ Internal method without rate limiting - can be called from within other rateLimited blocks
    // ✅ Handles batching for BGG's 20-item limit
    private suspend fun getBoardGamesInternal(ids: List<Long>): List<MediaItem> {
        if (ids.isEmpty()) return emptyList()

        // ✅ Batch requests if more than 20 items
        if (ids.size > BGG_MAX_ITEMS_PER_REQUEST) {
            logger.debug("Batching ${ids.size} items into chunks of $BGG_MAX_ITEMS_PER_REQUEST")

            val results = mutableListOf<MediaItem>()
            ids.chunked(BGG_MAX_ITEMS_PER_REQUEST).forEach { batch ->
                val batchResults = fetchBoardGamesBatch(batch)
                results.addAll(batchResults)

                // Add delay between batches to respect rate limits
                if (batch !== ids.chunked(BGG_MAX_ITEMS_PER_REQUEST).last()) {
                    delay(config.minDelayMillis)
                }
            }
            return results
        }

        return fetchBoardGamesBatch(ids)
    }

    private suspend fun fetchBoardGamesBatch(ids: List<Long>): List<MediaItem> {
        val idCsv = ids.joinToString(",")
        logger.debug("Fetching board game details for ${ids.size} IDs: $idCsv")

        val xml: String = httpClient.get("${config.baseUrl}/thing") {
            parameter("id", idCsv)
            parameter("stats", 1)
            headerIfToken()
        }.body()

        logger.debug("Thing response length: ${xml.length}")

        // Trim any leading/trailing whitespace or BOM
        val cleanedXml = xml.trim().trimStart('\uFEFF')

        if (!cleanedXml.startsWith("<?xml") && !cleanedXml.startsWith("<")) {
            logger.error("Invalid XML response: ${cleanedXml.take(200)}")
            throw IllegalStateException("BGG returned non-XML response: ${cleanedXml.take(200)}")
        }

        return BggMapper.mapThingListResponse(cleanedXml)
    }

    private fun HttpRequestBuilder.headerIfToken() {
        config.token?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}