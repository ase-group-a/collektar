package integration.tmdb


import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TmdbClientImplTest {


    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) { json() }
        }
    }

    private val mockConfig = TmdbConfig(
        bearerToken = "test_bearer_token",
        baseUrl = "https://api.themoviedb.org/3"
    )

    @Test
    fun `searchMovies returns results on successful response`(): Unit = runBlocking {
        val mockResponseJson = """
                {
                    "page": 1,
                    "results": [
                        {
                            "id": 27205,
                            "title": "Inception",
                            "overview": "Dream heist movie"
                        }
                    ],
                    "totalResults": 10,
                    "totalPages": 1
                }
            """.trimIndent()

        val httpClient = mockHttpClient { _ ->
            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = TmdbClientImpl(httpClient, mockConfig)
        val result = client.searchMovies("inception", 1)

        assertEquals(1, result.page)
        assertEquals(1, result.results.size)
        assertEquals(27205, result.results[0].id)
        assertEquals("Inception", result.results[0].title)
    }

        @Test
        fun `searchMovies sends correct query parameters for pagination`(): Unit = runBlocking {
            val mockResponseJson = """
                    {
                        "page": 1,
                        "results": [],
                        "totalResults": 0,
                        "totalPages": 0
                    }
                """.trimIndent()

            val httpClient = mockHttpClient { request ->

                assertEquals("test query with spaces", request.url.parameters["query"])
                assertEquals("1", request.url.parameters["page"])
                assertEquals("false", request.url.parameters["include_adult"])

                respond(
                    content = mockResponseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }

            val client = TmdbClientImpl(httpClient, mockConfig)

            client.searchMovies("test query with spaces", 1)
        }


                @Test
                    fun `searchMovies handles empty results`(): Unit = runBlocking {
                        val mockResponseJson = """
                                {
                                    "page": 1,
                                    "results": [],
                                    "totalResults": 0,
                                    "totalPages": 0
                                }
                                """.trimIndent()

                        val httpClient = mockHttpClient { _ ->
                            respond(
                                content = mockResponseJson,
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, "application/json")
                            )
                        }

                        val client = TmdbClientImpl(httpClient, mockConfig)
                        val result = client.searchMovies("nonexistent", 1)

                        assertEquals(0, result.results.size)
                        assertEquals(0, result.totalResults)
                    }



                        @Test
                        fun `searchMovies throws RateLimitException on 429 status`(): Unit = runBlocking {
                            val mockEngine = MockEngine {
                                respond(
                                    content = "Rate limit exceeded",
                                    status = HttpStatusCode.TooManyRequests,
                                    headers = headersOf(
                                        HttpHeaders.ContentType to listOf("text/plain"),
                                        "Retry-After" to listOf("60")
                                    )
                                )
                            }

                            val httpClient = HttpClient(mockEngine) {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                            }

                            val client = TmdbClientImpl(httpClient, mockConfig)

                            val exception = assertFailsWith<RateLimitException> {
                                client.searchMovies("test", 1)
                            }

                            assertEquals("TMDB rate limited", exception.message)
                        }

                        @Test
                        fun `searchMovies handles multiple results correctly`(): Unit = runBlocking {
                            val mockResponseJson = """
                                    {
                                        "page": 1,
                                        "results": [
                                            {"id": 1, "title": "Movie 1", "poster_path": "/p1.jpg"},
                                            {"id": 2, "title": "Movie 2", "poster_path": "/p2.jpg"},
                                            {"id": 3, "title": "Movie 3", "poster_path": null}
                                        ],
                                        "totalResults": 3,
                                        "totalPages": 1
                                    }
                                    """.trimIndent()

                            val mockEngine = MockEngine {
                                respond(
                                    content = mockResponseJson,
                                    status = HttpStatusCode.OK,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            }

                            val httpClient = HttpClient(mockEngine) {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                            }

                            val client = TmdbClientImpl(httpClient, mockConfig)
                            val result = client.searchMovies("test", 1)

                            assertEquals(3, result.results.size)
                            assertEquals(1, result.results[0].id)
                            assertEquals(2, result.results[1].id)
                            assertEquals(3, result.results[2].id)
                        }

                        @Test
                        fun `searchMovies throws RuntimeException on 401 Unauthorized`(): Unit = runBlocking {
                            val mockEngine = MockEngine {
                                respond(
                                    content = """{"status_message": "Invalid API key", "status_code": 7}""",
                                    status = HttpStatusCode.Unauthorized,
                                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                                )
                            }

                            val httpClient = HttpClient(mockEngine) {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                            }

                            val client = TmdbClientImpl(httpClient, mockConfig)

                            val exception = assertFailsWith<RuntimeException> {
                                client.searchMovies("test", 1)
                            }

                            assertTrue(exception.message?.contains("TMDB search failed") == true)
                            assertTrue(exception.message?.contains("401") == true)
                        }

                        @Test
                        fun `searchMovies throws RuntimeException on 404 Not Found`(): Unit = runBlocking {
                            val mockEngine = MockEngine {
                                respond(
                                    content = "Not found",
                                    status = HttpStatusCode.NotFound,
                                    headers = headersOf(HttpHeaders.ContentType, "text/plain")
                                )
                            }

                            val httpClient = HttpClient(mockEngine) {
                                install(ContentNegotiation) {
                                    json(Json { ignoreUnknownKeys = true })
                                }
                            }

                            val client = TmdbClientImpl(httpClient, mockConfig)

                            val exception = assertFailsWith<RuntimeException> {
                                client.searchMovies("test", 1)
                            }

                            assertTrue(exception.message?.contains("TMDB search failed") == true)
                            assertTrue(exception.message?.contains("404") == true)
                        }

    @Test
    fun `searchMovies uses popular endpoint when query is null`(): Unit = runBlocking {
        var capturedPath: String? = null
        var capturedQueryParam: String? = null
        var capturedIncludeAdult: String? = null

        val mockResponseJson = """
            {
                "page": 1,
                "results": [],
                "totalResults": 0,
                "totalPages": 0
            }
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            capturedPath = request.url.encodedPath
            capturedQueryParam = request.url.parameters["query"]
            capturedIncludeAdult = request.url.parameters["include_adult"]

            respond(
                content = mockResponseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = TmdbClientImpl(httpClient, mockConfig)
        val result = client.searchMovies(null, 1)

        // Response is deserialized
        assertEquals(1, result.page)
        assertEquals(0, result.totalResults)
        assertEquals(0, result.results.size)

        // We hit /movie/popular and no query/include_adult params are sent
        assertEquals("/3/movie/popular", capturedPath)
        assertEquals(null, capturedQueryParam)
        assertEquals(null, capturedIncludeAdult)
    }
}