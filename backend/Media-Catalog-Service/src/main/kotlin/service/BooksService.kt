package service

import domain.SearchResult
import integration.books.BooksClient
import integration.books.BooksMapper

class BooksService(
    private val booksClient: BooksClient
) {
    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val result = booksClient.searchBooks(query ?: "", limit, offset)

        val items = result.items?.map { book -> BooksMapper.bookToMediaItem(book) } ?: emptyList()

        return SearchResult(
            total = result.totalItems ?: items.size,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}