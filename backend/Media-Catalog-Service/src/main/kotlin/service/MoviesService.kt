package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.SearchResult
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbMapper

class MovieService(
    private val tmdbClient: TmdbClient,
    private val imageCacheClient: ImageCacheClient
) {

    suspend fun searchMovies(query: String?, limit: Int, offset: Int): SearchResult {
        val page = (offset / limit) + 1

        val tmdbResult = tmdbClient.searchMovies(query, page)

        val items = tmdbResult.results.map { movie ->
            TmdbMapper.movieToMediaItem(movie, imageIdentifierMapper = { imageIdentifier ->
                imageCacheClient.getImageUrl(
                    ImageSource.TMBD, imageIdentifier
                )
            })
        }

        return SearchResult(
            total = minOf(tmdbResult.totalResults, 10000),
            limit = limit,
            offset = offset,
            items = items
        )
    }
}
