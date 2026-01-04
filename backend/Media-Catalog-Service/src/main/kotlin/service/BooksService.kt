package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.SearchResult
import integration.books.BooksClient
import integration.books.BooksMapper

class BooksService(
    private val booksClient: BooksClient,
    private val imageCacheClient: ImageCacheClient
) {
    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val result = booksClient.searchBooks(query ?: "", limit, offset)

        val items = result.items?.map { book -> BooksMapper.bookToMediaItem(book, imageIdentifierMapper = { imageIdentifier ->
            imageCacheClient.getImageUrl(
                ImageSource.GOOGLE_BOOKS, imageIdentifier
            )
        })} ?: emptyList()

        return SearchResult(
            total = result.totalItems ?: items.size,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}