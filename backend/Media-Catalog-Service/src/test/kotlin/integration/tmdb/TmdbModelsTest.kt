package integration.tmdb

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbModelsTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `TmdbMovieDto deserializes correctly with all fields`() {
        val jsonString = """
        {
            "id": 27205,
            "title": "Inception",
            "overview": "Cobb, a skilled thief...",
            "posterPath": "/ljsZTbVsrQSqZgWeepP2B1QiDKuh.jpg",
            "backdropPath": "/backdrop.jpg",
            "releaseDate": "2010-07-16",
            "voteAverage": 8.4
        }
        """.trimIndent()

        val movie = json.decodeFromString<TmdbMovieDto>(jsonString)

        assertEquals(27205, movie.id)
        assertEquals("Inception", movie.title)
        assertEquals("Cobb, a skilled thief...", movie.overview)
        assertEquals("/ljsZTbVsrQSqZgWeepP2B1QiDKuh.jpg", movie.posterPath)
        assertEquals("/backdrop.jpg", movie.backdropPath)
        assertEquals("2010-07-16", movie.releaseDate)
        assertEquals(8.4, movie.voteAverage)
    }
    @Test
    fun `deserializes movie json with missing optional fields`() {
        val jsonString = """
            {
              "id": 1,
              "title": "Minimal Movie"
            }
        """.trimIndent()

        val movie = json.decodeFromString<TmdbMovieDto>(jsonString)

        assertEquals(1, movie.id)
        assertEquals("Minimal Movie", movie.title)
        assertNull(movie.overview)
        assertNull(movie.posterPath)
        assertNull(movie.backdropPath)
        assertNull(movie.releaseDate)
        assertNull(movie.voteAverage)
    }

    @Test
    fun `serializes movie dto to json`() {
        val movie = TmdbMovieDto(
            id = 42,
            title = "The Answer",
            overview = "Life, the Universe and Everything",
            posterPath = "/poster.png",
            backdropPath = "/backdrop.png",
            releaseDate = "2025-01-01",
            voteAverage = 7.5
        )

        val jsonString = json.encodeToString(movie)

        assertTrue(jsonString.contains("\"id\":42"))
        assertTrue(jsonString.contains("\"title\":\"The Answer\""))
        assertTrue(jsonString.contains("\"overview\":\"Life, the Universe and Everything\""))
    }

}