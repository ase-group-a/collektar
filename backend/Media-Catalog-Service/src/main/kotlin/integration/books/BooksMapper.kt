package integration.books

import domain.MediaItem
import domain.MediaType

object BooksMapper {
    fun bookToMediaItem(book: BookItemDto, imageIdentifierMapper: (String) -> String): MediaItem {
        val info = book.volumeInfo

        return MediaItem(
            id = "google:book:${book.id}",
            title = info?.title ?: "Unknown title",
            type = MediaType.BOOKS,
            imageUrl = if (info?.imageLinks?.thumbnail != null) imageIdentifierMapper(info.imageLinks.thumbnail) else null ,
            description = info?.authors?.joinToString(", ") ?: "Unknown author",
            source = "google_books"
        )
    }
}