package integration.spotify

interface SpotifyClient {
    suspend fun searchTracks(query: String, limit: Int = 20, offset: Int = 0): SpotifyTracksSearchResponse
}