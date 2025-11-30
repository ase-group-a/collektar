package integration.books

interface BooksClient {
    suspend fun searchBooks(query: String, limit: Int = 20, offset: Int = 0): GoogleBooksSearchResponse
}