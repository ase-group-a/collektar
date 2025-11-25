package di.modules

import controllers.Controller
import controllers.MusicController
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import integration.spotify.SpotifyTokenCache
import integration.spotify.SpotifyTokenProvider
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.MusicService

fun spotifyModule(env: ApplicationEnvironment) = module {
    single { SpotifyConfig.fromEnv(env) }

    single { SpotifyTokenCache() }

    single { SpotifyTokenProvider(get(), get()) }

    single<SpotifyClient> { SpotifyClientImpl(get(), get(), get()) }

    single { MusicService(get()) }

    single<Controller>(named("music")) { MusicController(get()) }
}
