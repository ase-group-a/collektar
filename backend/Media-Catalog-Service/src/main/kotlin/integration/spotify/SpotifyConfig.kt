package integration.spotify

import io.ktor.server.application.*

data class SpotifyConfig(
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String,
    val tokenUrl: String
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
                ?: "https://api.spotify.com/v1"

            val tokenUrl = System.getenv("SPOTIFY_TOKEN_URL")
                ?: env.config.propertyOrNull("spotify.tokenUrl")?.getString()
                ?: "https://accounts.spotify.com/api/token"

            return SpotifyConfig(clientId, clientSecret, baseUrl, tokenUrl)
        }
    }
}
