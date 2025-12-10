import integration.books.BooksClient
import integration.books.BookItemDto
import integration.books.GoogleBooksSearchResponse
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class BooksClientTest {

    class FakeBooksClient : BooksClient {
        override suspend fun searchBooks(query: String, limit: Int, offset: Int): GoogleBooksSearchResponse {
            return GoogleBooksSearchResponse(
                totalItems = limit + offset,
                items = listOf(BookItemDto(id = "fakeBook"))
            )
        }
    }

    @Test
    fun `BooksClient with default parameters`() = runBlocking {
        val client: BooksClient = FakeBooksClient()
        val result = client.searchBooks("test")

        assertEquals(20, result.totalItems)
        assertEquals(1, result.items?.size)
        assertEquals("fakeBook", result.items?.get(0)?.id)
    }
}