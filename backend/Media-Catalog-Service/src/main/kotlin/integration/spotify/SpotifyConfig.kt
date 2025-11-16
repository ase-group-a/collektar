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

            fun get(keyEnv: String, keyConf: String): String =
                env.config.propertyOrNull(keyConf)?.getString()
                    ?: System.getenv(keyEnv)
                    ?: error("$keyEnv not set")

            return SpotifyConfig(
                clientId = get("SPOTIFY_CLIENT_ID", "spotify.clientId"),
                clientSecret = get("SPOTIFY_CLIENT_SECRET", "spotify.clientSecret"),
                baseUrl = get("SPOTIFY_BASE_URL", "spotify.baseUrl"),
                tokenUrl = get("SPOTIFY_TOKEN_URL", "spotify.tokenUrl"),
                defaultPlaylistId = get("SPOTIFY_DEFAULT_PLAYLIST_ID", "spotify.defaultPlaylistId")
            )
        }
    }
}

