package integration.spotify

import io.ktor.server.application.*
import com.collektar.config.ConfigUtils.getConfigValue
import com.collektar.di.modules.OauthParameterType
import com.collektar.integration.shared.OauthConfig

data class SpotifyConfig(
    override val clientId: String,
    override val clientSecret: String,
    override val baseUrl: String,
    override val tokenUrl: String,
    val defaultPlaylistId: String,
    override val oauthParameterType: OauthParameterType = OauthParameterType.BODY_URLENCODED
) : OauthConfig {
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