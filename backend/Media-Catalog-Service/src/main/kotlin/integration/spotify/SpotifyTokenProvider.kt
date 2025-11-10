package integration.spotify

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.Base64

class SpotifyTokenProvider(
    private val httpClient: HttpClient,
    private val config: SpotifyConfig,
    private val tokenCache: SpotifyTokenCache = SpotifyTokenCache()
) {

    private val mutex = Mutex()

    private suspend fun fetchAccessToken(): SpotifyTokenResponse {
        val basic = Base64.getEncoder()
            .encodeToString("${config.clientId}:${config.clientSecret}".toByteArray(Charsets.UTF_8))

        val response: HttpResponse = httpClient.post(config.tokenUrl) {
            header(HttpHeaders.Authorization, "Basic $basic")
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(listOf("grant_type" to "client_credentials").formUrlEncode())
        }

        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) throw RuntimeException("Failed to fetch Spotify token: ${response.status} - $bodyText")

        return Json.decodeFromString(SpotifyTokenResponse.serializer(), bodyText)
    }

    suspend fun getToken(): String {
        tokenCache.getIfValid()?.let { return it }

        return mutex.withLock {
            tokenCache.getIfValid()?.let { return it }
            val tokenResp = fetchAccessToken()
            tokenCache.put(tokenResp.accessToken, tokenResp.expiresIn)
            tokenResp.accessToken
        }
    }
}
