package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import com.collektar.integration.igdb.IGDBClient
import domain.SearchResult
import integration.igdb.IGDBMapper

class GamesService(
    private val igdbClient: IGDBClient,
    private val imageCacheClient: ImageCacheClient
) {
    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val igdbResult = igdbClient.searchGames(query, limit, offset)

        val mediaItems = igdbResult.items.map { item ->
            IGDBMapper.gameToMediaItem(item, imageIdentifierMapper = { imageIdentifier ->
                imageCacheClient.getImageUrl(
                    ImageSource.IGDB, imageIdentifier
                )
            })
        }

        return SearchResult(
            total = igdbResult.total,
            limit = limit,
            offset = offset,
            items = mediaItems,
        )
    }
}