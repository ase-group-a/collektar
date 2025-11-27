package service

import domain.SearchResult
import integration.books.BooksClient
import integration.books.BookMapper

class BooksService(
    private val booksService: BooksService
) {
    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val result = booksClient.searchBooks(query, limit, offset)

        val items = result.items?.map {
            BooksMapper.bookToMedia(it)
        } ?: emptyList()

        return SearchResult(
            total = result.totalItems ?: items.size,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}