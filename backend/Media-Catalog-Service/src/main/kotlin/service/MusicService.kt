package service

import domain.SearchResult
import integration.spotify.SpotifyClient

class MusicService(
    private val spotifyClient: SpotifyClient
) {

    suspend fun search(query: String?, limit: Int, offset: Int): SearchResult {
        val spotifyResult = spotifyClient.searchTracks(query, limit, offset)

        val mediaItems = spotifyResult.tracks.items.map { track ->
            integration.spotify.SpotifyMapper.trackToMediaItem(track)
        }

        return SearchResult(
            total = spotifyResult.tracks.total,
            limit = limit,
            offset = offset,
            items = mediaItems
        )
    }
}
