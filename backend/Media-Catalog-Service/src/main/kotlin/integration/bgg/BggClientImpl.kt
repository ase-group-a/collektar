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
            val response: String = httpClient.get("${config.baseUrl}/search") {
                parameter("query", query)
                parameter("type", "boardgame")
                headerIfToken()
            }.body()

            BggMapper.mapSearchResponse(response, limit, offset)
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
