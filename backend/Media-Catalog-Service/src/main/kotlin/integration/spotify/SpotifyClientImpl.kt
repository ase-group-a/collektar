package integration.spotify

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import java.util.Base64
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class SpotifyClientImpl(
    private val httpClient: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val tokenUrl: String = "https://accounts.spotify.com/api/token",
    private val baseUrl: String = "https://api.spotify.com/v1",
    private val tokenCache: SpotifyTokenCache = SpotifyTokenCache()
) : SpotifyClient {

    private val tokenMutex = Mutex()

    private suspend fun fetchAccessToken(): SpotifyTokenResponse {
        val basic = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray(Charsets.UTF_8))

        val response: HttpResponse = httpClient.post(tokenUrl) {
            header(HttpHeaders.Authorization, "Basic $basic")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(listOf("grant_type" to "client_credentials").formUrlEncode())
        }

        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw RuntimeException("Failed to fetch spotify token: ${response.status} - $body")
        }

        return Json.decodeFromString(SpotifyTokenResponse.serializer(), body)
    }

    private suspend fun getAccessToken(): String {
        tokenCache.getIfValid()?.let { return it }

        return tokenMutex.withLock {
            tokenCache.getIfValid()?.let { return it }
            val tokenResp = fetchAccessToken()
            tokenCache.put(tokenResp.accessToken, tokenResp.expiresIn)
            tokenResp.accessToken
        }
    }

    override suspend fun searchTracks(query: String, limit: Int, offset: Int): SpotifyTracksSearchResponse {
        val token = getAccessToken()

        val url = "$baseUrl/search"
        val response: HttpResponse = httpClient.get(url) {
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
                throw RateLimitException(retryAfter)
            }
            throw RuntimeException("Spotify search failed: ${response.status} - $text")
        }

        return response.body<SpotifyTracksSearchResponse>()
    }

    class RateLimitException(val retryAfterSeconds: Long) : RuntimeException("Rate limited")
}
