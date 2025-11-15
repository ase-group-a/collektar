package integration.spotify

import io.ktor.server.application.*

data class SpotifyConfig(
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String,
    val tokenUrl: String,
    val defaultPlaylistId: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): SpotifyConfig {
            val clientId = System.getenv("SPOTIFY_CLIENT_ID")
                ?: env.config.propertyOrNull("spotify.clientId")?.getString()
                ?: error("SPOTIFY_CLIENT_ID not set")

            val clientSecret = System.getenv("SPOTIFY_CLIENT_SECRET")
                ?: env.config.propertyOrNull("spotify.clientSecret")?.getString()
                ?: error("SPOTIFY_CLIENT_SECRET not set")

            val baseUrl = System.getenv("SPOTIFY_BASE_URL")
                ?: env.config.propertyOrNull("spotify.baseUrl")?.getString()
                ?: error("SPOTIFY_BASE_URL not set")

            val tokenUrl = System.getenv("SPOTIFY_TOKEN_URL")
                ?: env.config.propertyOrNull("spotify.tokenUrl")?.getString()
                ?: error("SPOTIFY_TOKEN_URL not set")

            val defaultPlaylistId = System.getenv("SPOTIFY_DEFAULT_PLAYLIST_ID")
                ?: env.config.propertyOrNull("spotify.defaultPlaylistId")?.getString()
                ?: error("SPOTIFY_DEFAULT_PLAYLIST_ID not set")

            return SpotifyConfig(clientId, clientSecret, baseUrl, tokenUrl, defaultPlaylistId)
        }
    }
}
