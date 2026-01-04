package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.SearchResult
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMapper

class ShowService(
    private val tmdbClient: TmdbClient,
    private val imageCacheClient: ImageCacheClient
) {

    suspend fun searchShows(query: String?, limit: Int, offset: Int): SearchResult {
        val page = (offset / limit) + 1

        val tmdbResult = tmdbClient.searchShows(query, page)

        val items = tmdbResult.results.map {
            TmdbMapper.showToMediaItem(it, imageIdentifierMapper = { imageIdentifier ->
                imageCacheClient.getImageUrl(
                    ImageSource.TMBD, imageIdentifier
                )
            })
        }

        return SearchResult(
            total = tmdbResult.totalResults,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}
