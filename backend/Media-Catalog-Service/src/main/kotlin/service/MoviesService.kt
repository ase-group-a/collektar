package service

import domain.SearchResult
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMapper

class MovieService(
    private val tmdbClient: TmdbClient
) {

    suspend fun searchMovies(query: String, limit: Int, offset: Int): SearchResult {
        val page = (offset / limit) + 1
        val tmdbResult = tmdbClient.searchMovies(query, page)

        val mediaItems = tmdbResult.results
            .map { TmdbMapper.movieToMediaItem(it) }

        return SearchResult(
            total = tmdbResult.totalResults,
            limit = limit,
            offset = offset,
            items = mediaItems
        )
    }

}