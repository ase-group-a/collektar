package integration.books

import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BooksMapperTest {

    @Test
    fun `bookToMediaItem maps all values`() {
        val book = BookItemDto(
            id = "testID",
            volumeInfo = VolumeInfoDto(
                title = "My Book",
                authors = listOf("Author Test1", "Author Test2"),
                imageLinks = ImageLinksDto(thumbnail = "http://image.url")
            )
        )

        val mapped = BooksMapper.bookToMediaItem(book)

        assertEquals("google:book:testID", mapped.id)
        assertEquals("My Book", mapped.title)
        assertEquals(MediaType.BOOKS, mapped.type)
        assertEquals("Author Test1, Author Test2", mapped.description)
        assertEquals("http://image.url", mapped.imageUrl)
        assertEquals("google_books", mapped.source)
    }

    @Test
    fun `bookToMediaItem handles null values`() {
        val book = BookItemDto(
            id = "noID",
            volumeInfo = null
        )

        val mapped = BooksMapper.bookToMediaItem(book)

        assertEquals("google:book:noID", mapped.id)
        assertEquals("Unknown title", mapped.title)
        assertEquals(MediaType.BOOKS, mapped.type)
        assertEquals("Unknown author", mapped.description)
        assertNull(mapped.imageUrl)
    }
}