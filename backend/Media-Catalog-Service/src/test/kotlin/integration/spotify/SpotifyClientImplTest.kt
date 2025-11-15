package integration.spotify

import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpotifyClientImplTest {

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) { json() }
        }
    }

    private val mockConfig = SpotifyConfig(
        clientId = "id",
        clientSecret = "secret",
        baseUrl = "https://api.spotify.com/v1",
        tokenUrl = "https://accounts.spotify.com/api/token",
        defaultPlaylistId = "TEST_PLAYLIST"
    )

    private val mockTokenProvider = mockk<SpotifyTokenProvider>().apply {
        coEvery { getToken() } returns "TEST_TOKEN"
    }

    // Test: if query = null choose default Playlist route
    @Test
    fun `searchTracks returns playlist tracks when query is null`() = runTest {
        val mockResponseJson = """
        {
          "items": [
            { "track": { "id": "1", "name": "Song A" } }
          ],
          "total": 1
        }
        """.trimIndent()

        val client = mockHttpClient { request ->
            assertEquals(
                "https://api.spotify.com/v1/playlists/TEST_PLAYLIST/tracks?limit=20&offset=0",
                request.url.toString()
            )

            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val spotifyClient = SpotifyClientImpl(client, mockConfig, mockTokenProvider)

        val result = spotifyClient.searchTracks(null, limit = 20, offset = 0)

        assertEquals(1, result.tracks.items.size)
        assertEquals("Song A", result.tracks.items.first().name)
    }


    // Test: search route
    @Test
    fun `searchTracks calls search endpoint when query is provided`() = runTest {
        val mockResponseJson = """
        {
          "tracks": {
            "items": [
              { "id": "2", "name": "Search Result" }
            ],
            "total": 1
          }
        }
        """.trimIndent()

        val client = mockHttpClient { request ->
            assertEquals(
                "https://api.spotify.com/v1/search?q=test&type=track&limit=20&offset=0",
                request.url.toString()
            )

            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val spotifyClient = SpotifyClientImpl(client, mockConfig, mockTokenProvider)

        val result = spotifyClient.searchTracks("test", limit = 20, offset = 0)

        assertEquals("Search Result", result.tracks.items.first().name)
    }

    // Test: rate limit error
    @Test
    fun `searchTracks throws RateLimitException on HTTP 429`() = runTest {
        val client = mockHttpClient { _ ->
            respond(
                content = "rate limit",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf("Retry-After", "5")
            )
        }

        val spotifyClient = SpotifyClientImpl(client, mockConfig, mockTokenProvider)

        val ex = assertFailsWith<RateLimitException> {
            spotifyClient.searchTracks(null, 20, 0)
        }

        assertEquals(5, ex.retryAfterSeconds)
    }

    // Test: bad request error
    @Test
    fun `searchTracks throws RuntimeException on non-success`() = runTest {
        val client = mockHttpClient { _ ->
            respond("bad request", HttpStatusCode.BadRequest)
        }

        val spotifyClient = SpotifyClientImpl(client, mockConfig, mockTokenProvider)

        assertFailsWith<RuntimeException> {
            spotifyClient.searchTracks(null, 20, 0)
        }
    }
}
