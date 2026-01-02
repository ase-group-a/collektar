package integration.tmdb

interface TmdbClient {
    suspend fun searchMovies(query: String?, page: Int = 1): TmdbMovieSearchResponse
    suspend fun searchShows(query: String?, page: Int = 1): TmdbShowSearchResponse
}
