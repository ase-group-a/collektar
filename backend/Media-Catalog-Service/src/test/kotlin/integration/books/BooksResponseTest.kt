package integration.books

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BooksResponseTest {

    @Test
    fun `VolumeInfo all parameters`() {
        val authors = listOf("Author1", "Author2")
        val vi = VolumeInfo(title = "Title", authors = authors, description = "Desc")
        assertEquals("Title", vi.title)
        assertEquals(authors, vi.authors)
        assertEquals("Desc", vi.description)
    }

    @Test
    fun `VolumeInfo default parameters`() {
        val vi = VolumeInfo(title = "Default Title")
        assertEquals("Default Title", vi.title)
        assertNull(vi.authors)
        assertNull(vi.description)
    }

    @Test
    fun `Volume constructor with all parameters`() {
        val vi = VolumeInfo(title = "Title", authors = listOf("A1"), description = "Desc")
        val volume = Volume(id = "vol1", volumeInfo = vi)
        assertEquals("vol1", volume.id)
        assertEquals("Title", volume.volumeInfo?.title)
    }

    @Test
    fun `Volume constructor with default parameter`() {
        val volume = Volume(id = "vol2")
        assertEquals("vol2", volume.id)
        assertNull(volume.volumeInfo)
    }

    @Test
    fun `VolumesResponse with items`() {
        val volume = Volume(id = "vol3")
        val response = VolumesResponse(items = listOf(volume), totalItems = 1)
        assertEquals(1, response.totalItems)
        assertEquals("vol3", response.items?.first()?.id)
    }

    @Test
    fun `VolumesResponse with default items null`() {
        val response = VolumesResponse(totalItems = 0)
        assertEquals(0, response.totalItems)
        assertNull(response.items)
    }
}
