package integration.tmdb

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class TmdbClientImplTest {

    @Test
    fun `searchMovies with null query calls popular endpoint without query param`() = runTest {
        var capturedRequest: HttpRequestData? = null

        val mockEngine = MockEngine { request ->
            capturedRequest = request

            respond(
                content = """
                    {
                      "page": 1,
                      "results": [],
                      "total_results": 0,
                      "total_pages": 0
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }

        val config = TmdbConfig(
            baseUrl = "https://api.themoviedb.org/3",
            bearerToken = "test-token"
        )

        val tmdbClient: TmdbClient = TmdbClientImpl(client, config)

        // Act: call with null query to trigger /movie/popular branch
        val result = tmdbClient.searchMovies(null, page = 1)

        // Assert: response deserialized
        assertEquals(1, result.page)
        assertEquals(0, result.totalResults)
        assertEquals(0, result.totalPages)
        assertEquals(0, result.results.size)

        // Assert: correct endpoint and no "query" parameter
        val req = capturedRequest ?: error("No request captured")
        assertEquals("/3/movie/popular", req.url.encodedPath)
        assertFalse(req.url.parameters.contains("query"))
        assertEquals("false", req.url.parameters["include_adult"]) // should also be absent here
    }

    @Test
    fun `searchMovies with non-empty query calls search endpoint with query param`() = runTest {
        var capturedRequest: HttpRequestData? = null

        val mockEngine = MockEngine { request ->
            capturedRequest = request

            respond(
                content = """
                    {
                      "page": 1,
                      "results": [],
                      "total_results": 0,
                      "total_pages": 0
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }

        val config = TmdbConfig(
            baseUrl = "https://api.themoviedb.org/3",
            bearerToken = "test-token"
        )

        val tmdbClient: TmdbClient = TmdbClientImpl(client, config)

        // Act: normal search
        tmdbClient.searchMovies("inception", page = 2)

        // Assert: correct endpoint, page and query parameter
        val req = capturedRequest ?: error("No request captured")
        assertEquals("/3/search/movie", req.url.encodedPath)
        assertEquals("inception", req.url.parameters["query"])
        assertEquals("2", req.url.parameters["page"])
        // include_adult should be false
        assertEquals("false", req.url.parameters["include_adult"])
    }
}
