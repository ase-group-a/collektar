package di.modules

import controllers.Controller
import controllers.MusicController
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import io.ktor.server.application.*
import org.koin.dsl.module
import service.MusicService

fun spotifyModule(env: ApplicationEnvironment) = module {
    single { SpotifyConfig.fromEnv(env) }

    single { OauthTokenCache() }

    single { OauthTokenProvider(get(), get()) }

    single<SpotifyClient> { SpotifyClientImpl(get(), get(), get()) }

    single { MusicService(get()) }

    single<Controller> { MusicController(get()) }
}
