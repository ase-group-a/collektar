package integration.spotify

import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SpotifyClientImpl(
    private val httpClient: HttpClient,
    private val config: SpotifyConfig,
    private val tokenProvider: SpotifyTokenProvider
) : SpotifyClient {

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): SpotifyTracksSearchResponse {
        val token = tokenProvider.getToken()

        val response: HttpResponse = httpClient.get("${config.baseUrl}/search") {
            header(HttpHeaders.Authorization, "Bearer $token")
            url {
                parameters.append("q", query)
                parameters.append("type", "track")
                parameters.append("limit", limit.toString())
                parameters.append("offset", offset.toString())
            }
        }

        val text = response.bodyAsText()

        if (!response.status.isSuccess()) {
            if (response.status == HttpStatusCode.TooManyRequests) {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull() ?: 1L
                throw RateLimitException("Spotify rate limited", retryAfter)
            }
            throw RuntimeException("Spotify search failed: ${response.status} - $text")
        }

        return response.body()
    }
}
