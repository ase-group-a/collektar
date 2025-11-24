package integration.spotify

import com.collektar.integration.shared.OauthTokenProvider
import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class SpotifyClientImpl(
    private val httpClient: HttpClient,
    private val config: SpotifyConfig,
    private val tokenProvider: OauthTokenProvider
) : SpotifyClient {

    override suspend fun searchTracks(query: String?, limit: Int, offset: Int): SpotifyTracksSearchResponse {
        val token = tokenProvider.getToken(config)

        val response: HttpResponse = if (query.isNullOrBlank()) {
            val playlistId = config.defaultPlaylistId

            httpClient.get("${config.baseUrl}/playlists/$playlistId/tracks") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("limit", limit.toString())
                    parameters.append("offset", offset.toString())
                }
            }
        } else {
            httpClient.get("${config.baseUrl}/search") {
                header(HttpHeaders.Authorization, "Bearer $token")
                url {
                    parameters.append("q", query)
                    parameters.append("type", "track")
                    parameters.append("limit", limit.toString())
                    parameters.append("offset", offset.toString())
                }
            }
        }

        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            if (response.status == HttpStatusCode.TooManyRequests) {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull() ?: 1L
                throw RateLimitException("Spotify rate limited", retryAfter)
            }
            throw RuntimeException("Spotify request failed: ${response.status} - $text")
        }

        return if (query.isNullOrBlank()) {
            val playlist = response.body<PlaylistTracksResponse>()
            val trackDtos = playlist.items.mapNotNull { it.track }
            val tracksItems = TracksItems(items = trackDtos, total = playlist.total)
            SpotifyTracksSearchResponse(tracks = tracksItems)
        } else {
            response.body()
        }
    }
}
