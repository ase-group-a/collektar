package services

import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMovieDto
import integration.tmdb.TmdbMovieSearchResponse
import integration.tmdb.TmdbShowSearchResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import service.MovieService
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbServiceTest {

    private class FakeTmdbClient(
        var nextMovieResponse: TmdbMovieSearchResponse
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
        }

        override suspend fun searchShows(
            query: String?,
            page: Int
        ): TmdbShowSearchResponse {
            error("searchShows should not be called in MovieService tests")
        }
    }


    @Test
    fun `searchMovies forwards correct query and page for first page`() = runTest {
        val tmdbResponse = TmdbMovieSearchResponse(
            page = 1,
            results = listOf(
                TmdbMovieDto(
                    id = 1,
                    title = "Inception"
                )
            ),
            totalResults = 1,
            totalPages = 1
        )
        val fakeTmdbClient = FakeTmdbClient(tmdbResponse)
        val service = MovieService(fakeTmdbClient)

        val limit = 20
        val offset = 0

        service.searchMovies("inception", limit, offset)

        assertEquals("inception", fakeTmdbClient.receivedQuery)
        assertEquals(1, fakeTmdbClient.receivedPage)
        assertEquals(1, fakeTmdbClient.callCount)
    }

    @Test
    fun `searchMovies forwards null query and correct page`() = runTest {
        val tmdbResponse = TmdbMovieSearchResponse(
            page = 1,
            results = emptyList(),
            totalResults = 0,
            totalPages = 0
        )
        val fakeTmdbClient = FakeTmdbClient(tmdbResponse)
        val service = MovieService(fakeTmdbClient)

        val limit = 20
        val offset = 0

        service.searchMovies(null, limit, offset)

        assertNull(fakeTmdbClient.receivedQuery)
        assertEquals(1, fakeTmdbClient.receivedPage)
        assertEquals(1, fakeTmdbClient.callCount)
    }
}
