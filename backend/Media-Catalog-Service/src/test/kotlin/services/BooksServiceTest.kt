package service

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import integration.books.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.*

class BooksServiceTest {

    private val config = BooksConfig(bookApiKey = "testKey", baseUrl = "https://example.com")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private fun createMockClient(
        responseContent: String,
        expectedQuery: String = "query"
    ): BooksClient = BooksClientImpl(HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                assertEquals(expectedQuery, request.url.parameters["q"])
                respond(
                    content = responseContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
            }
        }
        install(ContentNegotiation) { json(json) }
    }, config)

    @Test
    fun `search with not null query and not null totalItems`() = runBlocking {
        val mockResponse = """
            {
              "totalItems": 2,
              "items": [
                { "id": "book1", "volumeInfo": {"title": "Book One", "authors": ["Author1"]} },
                { "id": "book2", "volumeInfo": {"title": "Book Two", "authors": ["Author2"]} }
              ]
            }
        """.trimIndent()

        val service = BooksService(createMockClient(mockResponse, "myquery"))
        val result = service.search("myquery", 10, 0)

        assertEquals(2, result.total)
        assertEquals(2, result.items.size)
        assertEquals("Book One", result.items[0].title)
        assertEquals("Book Two", result.items[1].title)
    }

    @Test
    fun `search with null query uses default bestseller`() = runBlocking {
        val mockResponse = """
            { "totalItems": 1, "items": [{ "id": "book1", "volumeInfo": {"title": "Book One", "authors": ["Author1"]} }] }
        """.trimIndent()

        val service = BooksService(createMockClient(mockResponse, "bestseller"))
        val result = service.search(null, 10, 0)

        assertEquals(1, result.total)
        assertEquals(1, result.items.size)
        assertEquals("Book One", result.items[0].title)
    }

    @Test
    fun `search uses items size if totalItems is null`() = runBlocking {
        val mockResponse = """
            {
              "totalItems": null,
              "items": [
                { "id": "book1", "volumeInfo": {"title": "Book One", "authors": ["Author1"]} },
                { "id": "book2", "volumeInfo": {"title": "Book Two", "authors": ["Author2"]} }
              ]
            }
        """.trimIndent()

        val service = BooksService(createMockClient(mockResponse))
        val result = service.search("query", 10, 0)

        assertEquals(2, result.total)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `search returns empty list when items is null`() = runBlocking {
        val mockResponse = """
            {
              "totalItems": 5,
              "items": null
            }
        """.trimIndent()

        val service = BooksService(createMockClient(mockResponse))
        val result = service.search("query", 10, 0)

        assertEquals(5, result.total)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `search returns 0 when items null and totalItems null`() = runBlocking {
        val mockResponse = """
            {
              "totalItems": null,
              "items": null
            }
        """.trimIndent()

        val service = BooksService(createMockClient(mockResponse))
        val result = service.search("query", 10, 0)

        assertEquals(0, result.total)
        assertTrue(result.items.isEmpty())
    }

    @Test
    fun `search throws exception on API error`(): Unit = runBlocking {
        val client = BooksClientImpl(HttpClient(MockEngine) {
            engine {
                addHandler { _ ->
                    respond(
                        content = "Server Error",
                        status = HttpStatusCode.InternalServerError
                    )
                }
            }
            install(ContentNegotiation) { json(json) }
        }, config)

        val service = BooksService(client)

        assertFailsWith<RuntimeException> {
            service.search("query", 10, 0)
        }
    }
}