package service

import domain.SearchResult
import integration.spotify.SpotifyClient

class MusicService(
    private val spotifyClient: SpotifyClient
) {

    suspend fun search(query: String, limit: Int, offset: Int): SearchResult {
        val spotifyResult = spotifyClient.searchTracks(query, limit, offset)

        val mediaItems = spotifyResult.tracks?.items?.map { track ->
            integration.spotify.SpotifyMapper.trackToMediaItem(track)
        } ?: emptyList()

        return SearchResult(
            total = mediaItems.size, //TODO: Get total count of items from spotify / api directly
            limit = limit,
            offset = offset,
            items = mediaItems
        )
    }
}
