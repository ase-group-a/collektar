package integration.tmdb

import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbMapperTest {

    @Test
    fun `movieToMediaItem maps all fields including image url`() {
        val dto = TmdbMovieDto(
            id = 42,
            title = "The Answer",
            overview = "Life, the universe and everything",
            posterPath = "/poster.png"
        )

        val result = TmdbMapper.movieToMediaItem(dto)

        assertEquals("tmdb:movie:42", result.id)
        assertEquals("The Answer", result.title)
        assertEquals(MediaType.MOVIE, result.type)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.png", result.imageUrl)
        assertEquals("Life, the universe and everything", result.description)
        assertEquals("tmdb", result.source)
    }

    @Test
    fun `movieToMediaItem sets imageUrl to null when posterPath is null`() {
        val dto = TmdbMovieDto(
            id = 1,
            title = "No Poster Movie",
            overview = null,
            posterPath = null
        )

        val result = TmdbMapper.movieToMediaItem(dto)

        assertEquals("tmdb:movie:1", result.id)
        assertNull(result.imageUrl)
    }

    @Test
    fun `showToMediaItem maps all fields including image url`() {
        val dto = TmdbShowDto(
            id = 1399,
            name = "Game of Thrones",
            overview = "Winter is coming",
            posterPath = "/poster.jpg"
        )

        val result = TmdbMapper.showToMediaItem(dto)

        assertEquals("tmdb:show:1399", result.id)
        assertEquals("Game of Thrones", result.title)
        assertEquals(MediaType.SHOW, result.type)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result.imageUrl)
        assertEquals("Winter is coming", result.description)
        assertEquals("tmdb", result.source)
    }

    @Test
    fun `showToMediaItem sets imageUrl to null when posterPath is null`() {
        val dto = TmdbShowDto(
            id = 1,
            name = "No Poster Show",
            overview = null,
            posterPath = null
        )

        val result = TmdbMapper.showToMediaItem(dto)

        assertEquals("tmdb:show:1", result.id)
        assertNull(result.imageUrl)
    }
}
