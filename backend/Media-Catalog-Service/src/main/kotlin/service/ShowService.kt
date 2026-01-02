package service

import domain.SearchResult
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMapper

class ShowService(
    private val tmdbClient: TmdbClient
) {

    suspend fun searchShows(query: String?, limit: Int, offset: Int): SearchResult {
        val page = (offset / limit) + 1

        val tmdbResult = tmdbClient.searchShows(query, page)

        val items = tmdbResult.results.map {
            TmdbMapper.showToMediaItem(it)
        }

        return SearchResult(
            total = tmdbResult.totalResults,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}
