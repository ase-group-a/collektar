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
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }
        routing {
            movieController.register(this)
        }
    }

    @Test
    fun `GET movies returns results with valid query`() = testApplication {
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

        // Route is now just "/movies"
        val response = client.get("/movies?q=inception")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Inception"))
        assertTrue(body.contains("tmdb:movie:27205"))
        coVerify(exactly = 1) { movieService.searchMovies("inception", 20, 0) }
    }

    @Test
    fun `GET movies without query uses popular fallback`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        // Now we expect null query to be forwarded to the service
        coEvery { movieService.searchMovies(null, 20, 0) } returns searchResult

        val response = client.get("/movies")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { movieService.searchMovies(null, 20, 0) }
    }

    @Test
    fun `GET movies handles empty query string`() = testApplication {
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

        val response = client.get("/movies?q=")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { movieService.searchMovies("", 20, 0) }
    }

    @Test
    fun `GET movies passes pagination parameters to service`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 100,
            limit = 5,
            offset = 10,
            items = emptyList()
        )

        coEvery { movieService.searchMovies("matrix", 5, 10) } returns searchResult

        val response = client.get("/movies?q=matrix&limit=5&offset=10")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { movieService.searchMovies("matrix", 5, 10) }
    }
}
