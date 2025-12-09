package integration.books

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class BooksClientImplTest {

    private val config = BooksConfig(
        bookApiKey = "dummyKey",
        baseUrl = "https://example.com"
    )

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
        }
    }

    @Test
    fun `valid search returns  response`() = runBlocking {
        val mockResponse = """
            {
              "totalItems": 1,
              "items": [
                {
                  "id": "bookTestID",
                  "volumeInfo": {
                    "title": "Test Book Title",
                    "authors": ["AuthorTester"]
                  }
                }
              ]
            }
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            assertEquals("/volumes", request.url.encodedPath)
            assertEquals("example.com", request.url.host)
            assertEquals("query", request.url.parameters["q"])
            assertEquals(config.bookApiKey, request.url.parameters["key"])
            assertEquals("10", request.url.parameters["maxResults"])
            assertEquals("0", request.url.parameters["startIndex"])

            respond(
                content = mockResponse,
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
                )
            )
        }

        val client = BooksClientImpl(httpClient, config)

        val result = client.searchBooks("query", 10, 0)

        assertEquals(1, result.totalItems)
        assertEquals(1, result.items?.size)
        assertEquals("bookTestID", result.items?.first()?.id)
        assertEquals("Test Book Title", result.items?.first()?.volumeInfo?.title)
    }

    @Test
    fun `blank query uses default bestseller search`() = runBlocking {
        val httpClient = mockHttpClient { request ->
            assertEquals("bestseller", request.url.parameters["q"])

            respond(
                content = """{ "totalItems": 0, "items": [] }""",
                status = HttpStatusCode.OK,
                headers = headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
                )
            )
        }

        val client = BooksClientImpl(httpClient, config)

        val result = client.searchBooks("", 10, 0)

        assertEquals(0, result.totalItems)
        assertTrue(result.items!!.isEmpty())
    }

    @Test
    fun `non success status throws RuntimeException`() = runBlocking {
        val httpClient = mockHttpClient {
            respond("Server error", HttpStatusCode.InternalServerError)
        }

        val client = BooksClientImpl(httpClient, config)

        val exception = assertFailsWith<RuntimeException> {
            client.searchBooks("query", 10, 0)
        }

        assertTrue(exception.message!!.contains("Google Books API error"))
    }

    @Test
    fun `malformed json throws exception`(): Unit = runBlocking {
        val httpClient = mockHttpClient {
            respond("not a json", HttpStatusCode.OK)
        }

        val client = BooksClientImpl(httpClient, config)

        assertFailsWith<Exception> {
            client.searchBooks("query", 10, 0)
        }
    }
}