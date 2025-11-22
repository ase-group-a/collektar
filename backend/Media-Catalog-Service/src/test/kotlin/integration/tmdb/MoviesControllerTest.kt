import controllers.MovieController
import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import service.MovieService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovieControllerTest {

    private val movieService = mockk<MovieService>()
    private val movieController = MovieController(movieService)

    private fun Application.configureTestApp() {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        routing {
            movieController.register(this)
        }
    }

    @Test
    fun `GET search movies returns results with valid query`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 10,
            limit = 20,
            offset = 0,
            items = listOf(
                MediaItem(
                    id = "tmdb:movie:27205",
                    title = "Inception",
                    type = MediaType.MOVIE,
                    imageUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
                    description = "Dream heist movie",
                    source = "tmdb"
                )
            )
        )

        coEvery { movieService.searchMovies("inception", 20, 0) } returns searchResult

        val response = client.get("/movies/search/movies?q=inception")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Inception"))
        assertTrue(body.contains("tmdb:movie:27205"))
        coVerify(exactly = 1) { movieService.searchMovies("inception", 20, 0) }
    }

    @Test
    fun `GET search movies returns BadRequest when query parameter is missing`() = testApplication {
        application {
            configureTestApp()
        }

        val response = client.get("/movies/search/movies")

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("Missing query parameter 'q'", response.bodyAsText())
        coVerify(exactly = 0) { movieService.searchMovies(any(), any(), any()) }
    }

    @Test
    fun `GET search movies handles empty query string`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        coEvery { movieService.searchMovies("", 20, 0) } returns searchResult

        val response = client.get("/movies/search/movies?q=")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { movieService.searchMovies("", 20, 0) }
    }

}
