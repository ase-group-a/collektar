package controllers

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
import service.ShowService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShowControllerTest {

    private val showService = mockk<ShowService>()
    private val showController = ShowController(showService)

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
            showController.register(this)
        }
    }

    @Test
    fun `GET shows returns results with valid query`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 10,
            limit = 20,
            offset = 0,
            items = listOf(
                MediaItem(
                    id = "tmdb:show:1399",
                    title = "Game of Thrones",
                    type = MediaType.SHOW,
                    imageUrl = "https://image.tmdb.org/t/p/w500/poster.jpg",
                    description = "Winter is coming",
                    source = "tmdb"
                )
            )
        )

        coEvery { showService.searchShows("got", 20, 0) } returns searchResult

        val response = client.get("/shows?q=got")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("Game of Thrones"))
        assertTrue(body.contains("tmdb:show:1399"))

        coVerify(exactly = 1) { showService.searchShows("got", 20, 0) }
    }

    @Test
    fun `GET shows without query uses popular fallback`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        coEvery { showService.searchShows(null, 20, 0) } returns searchResult

        val response = client.get("/shows")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { showService.searchShows(null, 20, 0) }
    }

    @Test
    fun `GET shows handles empty query string`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        coEvery { showService.searchShows("", 20, 0) } returns searchResult

        val response = client.get("/shows?q=")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { showService.searchShows("", 20, 0) }
    }

    @Test
    fun `GET shows passes pagination parameters to service`() = testApplication {
        application {
            configureTestApp()
        }

        val searchResult = SearchResult(
            total = 100,
            limit = 5,
            offset = 10,
            items = emptyList()
        )

        coEvery { showService.searchShows("breaking bad", 5, 10) } returns searchResult

        val response = client.get("/shows?q=breaking%20bad&limit=5&offset=10")

        assertEquals(HttpStatusCode.OK, response.status)
        coVerify(exactly = 1) { showService.searchShows("breaking bad", 5, 10) }
    }
}
