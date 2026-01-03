package di.modules

import com.collektar.di.modules.OauthParameterType
import com.collektar.integration.shared.OauthConfig
import controllers.Controller
import controllers.MusicController
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import service.MusicService

const val SPOTIFY_CONFIG_NAME = "spotify_config"
const val SPOTIFY_TOKEN_CACHE_NAME = "spotify_token_cache"
const val SPOTIFY_TOKEN_PROVIDER_NAME = "spotify_token_provider"

const val SPOTIFY_CONTROLLER_NAME = "music"

fun spotifyModule(env: ApplicationEnvironment) = module {
    single(named(SPOTIFY_CONFIG_NAME)) { SpotifyConfig.fromEnv(env) } binds arrayOf(OauthConfig::class, SpotifyConfig::class)
    single(named(SPOTIFY_TOKEN_CACHE_NAME)) { OauthTokenCache() }
    single(named(SPOTIFY_TOKEN_PROVIDER_NAME)) {
        OauthTokenProvider(
            get(),
            get(named(SPOTIFY_TOKEN_CACHE_NAME)),
            get(named(SPOTIFY_CONFIG_NAME)),
            OauthParameterType.BODY_URLENCODED
        )
    }
    single<SpotifyClient> {
        SpotifyClientImpl(
            get(),
            get(named(SPOTIFY_CONFIG_NAME)),
            get(named(SPOTIFY_TOKEN_PROVIDER_NAME)))
    }
    single { MusicService(get(), get()) }
    single<Controller>(named(SPOTIFY_CONTROLLER_NAME)) { MusicController(get()) }
}
