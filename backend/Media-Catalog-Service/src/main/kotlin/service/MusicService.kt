package service

import domain.MediaItem
import domain.SearchResult
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyMapper

class MusicService(private val spotifyClient: SpotifyClient) {

    suspend fun search(query: String, limit: Int = 20, offset: Int = 0): SearchResult<MediaItem> {
        require(query.isNotBlank()) { "query must not be blank" }
        val dto = spotifyClient.searchTracks(query = query, limit = limit, offset = offset)
        val tracks = dto.tracks?.items ?: emptyList()
        val items = tracks.map { SpotifyMapper.trackToMediaItem(it) }

        return SearchResult(
            total = items.size,
            limit = limit,
            offset = offset,
            items = items
        )
    }
}
