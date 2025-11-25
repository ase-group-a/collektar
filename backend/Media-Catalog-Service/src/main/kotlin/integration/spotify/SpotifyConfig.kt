package integration.spotify

import io.ktor.server.application.*
import com.collektar.config.ConfigUtils.getConfigValue

data class SpotifyConfig(
    val clientId: String,
    val clientSecret: String,
    val baseUrl: String,
    val tokenUrl: String,
    val defaultPlaylistId: String
) {
    companion object {
        fun fromEnv(env: ApplicationEnvironment): SpotifyConfig {
            return SpotifyConfig(
                clientId = getConfigValue(env, "SPOTIFY_CLIENT_ID", "spotify.clientId"),
                clientSecret = getConfigValue(env, "SPOTIFY_CLIENT_SECRET", "spotify.clientSecret"),
                baseUrl = getConfigValue(env, "SPOTIFY_BASE_URL", "spotify.baseUrl"),
                tokenUrl = getConfigValue(env, "SPOTIFY_TOKEN_URL", "spotify.tokenUrl"),
                defaultPlaylistId = getConfigValue(env, "SPOTIFY_DEFAULT_PLAYLIST_ID", "spotify.defaultPlaylistId")
            )
        }
    }
}