package integration.books

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BooksModelsTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `GoogleBooksSearchResponse serializes and deserializes correctly`() {
        val book = BookItemDto(
            id = "11",
            volumeInfo = VolumeInfoDto(
                title = "Test Title",
                authors = listOf("Author"),
                description = "Desc",
                imageLinks = ImageLinksDto(thumbnail = "http://thumbnail")
            )
        )
        val response = GoogleBooksSearchResponse(totalItems = 1, items = listOf(book))

        val serialized = json.encodeToString(GoogleBooksSearchResponse.serializer(), response)
        val deserialized = json.decodeFromString(GoogleBooksSearchResponse.serializer(), serialized)

        assertEquals(1, deserialized.totalItems)
        assertNotNull(deserialized.items)
        assertEquals("11", deserialized.items?.first()?.id)
        assertEquals("Test Title", deserialized.items?.first()?.volumeInfo?.title)
    }

    @Test
    fun `GoogleBooksSearchResponse handles empty list and null`() {
        val response = GoogleBooksSearchResponse(totalItems = 0, items = null)
        val serialized = json.encodeToString(GoogleBooksSearchResponse.serializer(), response)
        val deserialized = json.decodeFromString(GoogleBooksSearchResponse.serializer(), serialized)

        assertEquals(0, deserialized.totalItems)
        assertNull(deserialized.items)
    }

    @Test
    fun `GoogleBooksSearchResponse with default values`() {
        val response = GoogleBooksSearchResponse()
        assertEquals(0, response.totalItems)
        assertEquals(emptyList<BookItemDto>(), response.items)
    }

    @Test
    fun `BookItemDto deserializes correctly`() {
        val jsonString = """
            {
                "id": "12",
                "volumeInfo": {
                    "title": "Test Title",
                    "authors": ["Author Test1", "Author Test2"],
                    "description": "description",
                    "imageLinks": {
                        "thumbnail": "http://thumbnail.url",
                        "smallThumbnail": "http://small.url"
                    }
                }
            }
        """.trimIndent()

        val book = json.decodeFromString(BookItemDto.serializer(), jsonString)
        assertEquals("12", book.id)
        assertNotNull(book.volumeInfo)
        assertEquals("Test Title", book.volumeInfo?.title)
        assertEquals("description", book.volumeInfo?.description)
        assertEquals(listOf("Author Test1", "Author Test2"), book.volumeInfo?.authors!!)
        assertEquals("http://thumbnail.url", book.volumeInfo?.imageLinks?.thumbnail)
    }

    @Test
    fun `BookItemDto deserializes with null volumeInfo`() {
        val jsonString = """{"id": "444"}"""
        val book = json.decodeFromString(BookItemDto.serializer(), jsonString)
        assertEquals("444", book.id)
        assertNull(book.volumeInfo)
    }

    @Test
    fun `BookItemDto with default values`() {
        val book = BookItemDto(id = "defaultId")
        assertEquals("defaultId", book.id)
        assertNull(book.volumeInfo)
    }

    @Test
    fun `VolumeInfo constructor and getters`() {
        val authors = listOf("Author Tets1", "Author Test2")
        val volumeInfo = VolumeInfo(
            title = "My Book Title",
            authors = authors,
            description = "description"
        )

        assertEquals("My Book Title", volumeInfo.title)
        assertEquals(authors, volumeInfo.authors)
        assertEquals("description", volumeInfo.description)
    }

    @Test
    fun `Volume constructor and getters`() {
        val volumeInfo = VolumeInfo(
            title = "Title",
            authors = listOf("Author"),
            description = "Desc"
        )

        val volume = Volume(
            id = "vol1",
            volumeInfo = volumeInfo
        )

        assertEquals("vol1", volume.id)
        assertEquals(volumeInfo, volume.volumeInfo)
        assertEquals("Title", volume.volumeInfo?.title)
    }

    @Test
    fun `VolumesResponse constructor and getters`() {
        val volumeInfo = VolumeInfo(
            title = "Title",
            authors = listOf("Author"),
            description = "Desc"
        )
        val volume = Volume(
            "vol1",
            volumeInfo
        )

        val response = VolumesResponse(
            items = listOf(volume),
            totalItems = 1
        )

        assertEquals(1, response.totalItems)
        assertNotNull(response.items)
        assertEquals(1, response.items!!.size)
        assertEquals(volume, response.items!!.first())
    }

    @Test
    fun `VolumeInfoDto with default parameters`() {
        val volumeInfo = VolumeInfoDto(title = "Default Title")
        assertEquals("Default Title", volumeInfo.title)
        assertEquals(emptyList<String>(), volumeInfo.authors ?: emptyList())
        assertEquals(null, volumeInfo.description)
    }
}
