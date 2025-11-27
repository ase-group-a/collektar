package integration.books

import domain.MediaItem
import domain.MediaType

object BooksMapper {
    fun bookToMediaItem(book: BookItemDto): MediaItem {
        val info = book.volumeInfo

        return MediaItem(
            id = "google:book:${book.id}",
            title = info.title ?: "Unknown title",
            type = MediaType.BOOK,
            imageUrl = info.imageLinks?.thumbnail,
            description = info.authors?.joinToString(", ") ?: "Unknown author",
            source = "google_books"
        )
    }
}