package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.SearchResult
import integration.spotify.SpotifyClient

class MusicService(
    private val spotifyClient: SpotifyClient,
    private val imageCacheClient: ImageCacheClient
) {

    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val spotifyResult = spotifyClient.searchTracks(query, limit, offset)

        val mediaItems = spotifyResult.tracks.items.map { track ->
            integration.spotify.SpotifyMapper.trackToMediaItem(track, imageIdentifierMapper = { imageIdentifier ->
                imageCacheClient.getImageUrl(
                    ImageSource.SPOTIFY, imageIdentifier
                )
            })
        }

        return SearchResult(
            total = spotifyResult.tracks.total,
            limit = limit,
            offset = offset,
            items = mediaItems
        )
    }
}
