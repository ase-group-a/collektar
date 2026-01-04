package integration.tmdb

import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

const val IMAGE_URL = "poster.png"
const val URL_MAPPER_POSTFIX = "mapped"

class TmdbMapperTest {

    @Test
    fun `movieToMediaItem maps all fields including image url`() {
        val dto = TmdbMovieDto(
            id = 42,
            title = "The Answer",
            overview = "Life, the universe and everything",
            posterPath = IMAGE_URL
        )

        val result = TmdbMapper.movieToMediaItem(dto, imageIdentifierMapper = { imageIdentifier -> "$imageIdentifier$URL_MAPPER_POSTFIX" })

        assertEquals("tmdb:movie:42", result.id)
        assertEquals("The Answer", result.title)
        assertEquals(MediaType.MOVIE, result.type)
        assertEquals("$IMAGE_URL$URL_MAPPER_POSTFIX", result.imageUrl)
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

        val result = TmdbMapper.movieToMediaItem(dto, { imageIdentifier -> "$imageIdentifier$URL_MAPPER_POSTFIX" })

        assertEquals("tmdb:movie:1", result.id)
        assertNull(result.imageUrl)
    }
}
