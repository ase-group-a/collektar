package services

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMovieDto
import integration.tmdb.TmdbMovieSearchResponse
import io.mockk.mockk
import domain.MediaType
import integration.tmdb.*
import io.mockk.every
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import service.MovieService
import service.ShowService
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbServiceTest {

    private val imageCacheClient = mockk<ImageCacheClient>()
    
    private class FakeTmdbClient(
        var nextMovieResponse: TmdbMovieSearchResponse? = null,
        var nextShowResponse: TmdbShowSearchResponse? = null
    ) : TmdbClient {

        var receivedQuery: String? = null
        var receivedPage: Int? = null
        var callCount: Int = 0

        override suspend fun searchMovies(
            query: String?,
            page: Int
        ): TmdbMovieSearchResponse {
            receivedQuery = query
            receivedPage = page
            callCount++
            return nextMovieResponse
                ?: error("searchMovies should not be called in MovieService tests")
        }

        override suspend fun searchShows(
            query: String?,
            page: Int
        ): TmdbShowSearchResponse {
            receivedQuery = query
            receivedPage = page
            callCount++
            return nextShowResponse
                ?: error("searchShows should not be called in MovieService tests")
        }
    }

    @Test
    fun `searchMovies forwards correct query and page for first page`() = runTest {
        val tmdbResponse = TmdbMovieSearchResponse(
            page = 1,
            results = listOf(
                TmdbMovieDto(id = 1, title = "Inception")
            ),
            totalResults = 1,
            totalPages = 1
        )

        val fakeClient = FakeTmdbClient(nextMovieResponse = tmdbResponse)
        val service = MovieService(fakeClient, imageCacheClient)

        service.searchMovies("inception", limit = 20, offset = 0)

        assertEquals("inception", fakeClient.receivedQuery)
        assertEquals(1, fakeClient.receivedPage)
        assertEquals(1, fakeClient.callCount)
    }

    @Test
    fun `searchMovies forwards null query and correct page`() = runTest {
        val tmdbResponse = TmdbMovieSearchResponse(
            page = 1,
            results = emptyList(),
            totalResults = 0,
            totalPages = 0
        )

        val fakeClient = FakeTmdbClient(nextMovieResponse = tmdbResponse)
        val service = MovieService(fakeClient, imageCacheClient)

        service.searchMovies(null, limit = 20, offset = 0)

        assertNull(fakeClient.receivedQuery)
        assertEquals(1, fakeClient.receivedPage)
        assertEquals(1, fakeClient.callCount)
    }

    @Test
    fun `searchShows forwards correct query and calculates page`() = runTest {
        val tmdbResponse = TmdbShowSearchResponse(
            page = 2,
            results = emptyList(),
            totalResults = 0,
            totalPages = 2
        )

        val fakeClient = FakeTmdbClient(nextShowResponse = tmdbResponse)
        val service = ShowService(fakeClient, imageCacheClient)

        service.searchShows("got", limit = 10, offset = 10)

        assertEquals("got", fakeClient.receivedQuery)
        assertEquals(2, fakeClient.receivedPage)
        assertEquals(1, fakeClient.callCount)
    }

    @Test
    fun `searchShows forwards null query for popular shows`() = runTest {
        val tmdbResponse = TmdbShowSearchResponse(
            page = 1,
            results = emptyList(),
            totalResults = 0,
            totalPages = 0
        )

        val fakeClient = FakeTmdbClient(nextShowResponse = tmdbResponse)
        val service = ShowService(fakeClient, imageCacheClient)

        service.searchShows(null, limit = 20, offset = 0)

        assertNull(fakeClient.receivedQuery)
        assertEquals(1, fakeClient.receivedPage)
        assertEquals(1, fakeClient.callCount)
    }

    @Test
    fun `searchShows maps tmdb results to media items`() = runTest {
        val tmdbResponse = TmdbShowSearchResponse(
            page = 1,
            results = listOf(
                TmdbShowDto(
                    id = 1399,
                    name = "Game of Thrones",
                    overview = "Winter is coming",
                    posterPath = "/poster.jpg"
                )
            ),
            totalResults = 1,
            totalPages = 1
        )

        every { imageCacheClient.getImageUrl(ImageSource.TMBD, tmdbResponse.results.first().posterPath!!) } returns "${tmdbResponse.results.first().posterPath!!}_mock"
        
        val fakeClient = FakeTmdbClient(nextShowResponse = tmdbResponse)
        val service = ShowService(fakeClient, imageCacheClient)

        val result = service.searchShows("got", limit = 20, offset = 0)

        assertEquals(1, result.items.size)

        val item = result.items.first()
        assertEquals("tmdb:show:1399", item.id)
        assertEquals("Game of Thrones", item.title)
        assertEquals(MediaType.SHOW, item.type)
        assertEquals("${tmdbResponse.results.first().posterPath!!}_mock", item.imageUrl)
        assertEquals("Winter is coming", item.description)
        assertEquals("tmdb", item.source)
    }

    @Test
    fun `searchShows returns correct SearchResult metadata`() = runTest {
        val tmdbResponse = TmdbShowSearchResponse(
            page = 3,
            results = emptyList(),
            totalResults = 100,
            totalPages = 5
        )

        val fakeClient = FakeTmdbClient(nextShowResponse = tmdbResponse)
        val service = ShowService(fakeClient, imageCacheClient)

        val result = service.searchShows("test", limit = 10, offset = 20)

        assertEquals(100, result.total)
        assertEquals(10, result.limit)
        assertEquals(20, result.offset)
        assertEquals(0, result.items.size)
    }
}
