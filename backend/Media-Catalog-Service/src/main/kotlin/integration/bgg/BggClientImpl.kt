// src/main/kotlin/integration/bgg/BggClientImpl.kt
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

class BggClientImpl(
    private val httpClient: HttpClient,
    private val config: BggConfig
) : BggClient {

    private val rateLimitMutex = Mutex()
    private var lastCallTime: Long = 0

    private suspend fun <T> rateLimited(block: suspend () -> T): T =
        rateLimitMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastCallTime
            if (elapsed < config.minDelayMillis) {
                delay(config.minDelayMillis - elapsed)
            }
            val result = block()
            lastCallTime = System.currentTimeMillis()
            result
        }

    override suspend fun searchBoardGames(
        query: String,
        limit: Int,
        offset: Int
    ): SearchResult = rateLimited {
        try {
            // 1) SEARCH (ids + titles, no images)
            val searchXml: String = httpClient.get("${config.baseUrl}/search") {
                parameter("query", query)
                parameter("type", "boardgame")
                headerIfToken()
            }.body()

            val allHits = BggMapper.parseSearchHits(searchXml)
            val pageHits = allHits.drop(offset).take(limit)

            if (pageHits.isEmpty()) {
                return@rateLimited SearchResult(
                    total = allHits.size,
                    limit = limit,
                    offset = offset,
                    items = emptyList()
                )
            }

            // 2) THING (images/description) — one request for all ids in the page
            val idsCsv = pageHits.joinToString(",") { it.id.toString() }

            val thingXml: String = httpClient.get("${config.baseUrl}/thing") {
                parameter("id", idsCsv)
                parameter("stats", 0) // stats=0 is faster; set 1 if you need ratings, etc.
                headerIfToken()
            }.body()

            val infoMap = BggMapper.parseThings(thingXml)

            // 3) MERGE -> MediaItem list with image_url filled
            BggMapper.toSearchResultWithImages(
                allHits = allHits,
                pageHits = pageHits,
                thingInfo = infoMap,
                limit = limit,
                offset = offset
            )
        } catch (e: Exception) {
            SearchResult(
                total = 0,
                limit = limit,
                offset = offset,
                items = emptyList()
            )
        }
    }

    override suspend fun getBoardGame(id: Long): MediaItem? = rateLimited {
        try {
            val response: String = httpClient.get("${config.baseUrl}/thing") {
                parameter("id", id)
                parameter("stats", 1)
                headerIfToken()
            }.body()

            // If your existing mapThingResponse already returns full MediaItem with imageUrl,
            // keep it as-is.
            BggMapper.mapThingResponse(response)
        } catch (e: Exception) {
            null
        }
    }

    private fun HttpRequestBuilder.headerIfToken() {
        config.token?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}
