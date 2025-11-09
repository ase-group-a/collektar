package di.modules

import controllers.Controller
import controllers.MusicController
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import integration.spotify.SpotifyTokenCache
import io.ktor.server.application.*
import org.koin.dsl.module
import service.MusicService

fun spotifyModule(env: ApplicationEnvironment) = module {
    single { SpotifyConfig.fromEnv(env) }

    single { SpotifyTokenCache() }

    single<SpotifyClient> {
        SpotifyClientImpl(
            httpClient = get(),
            clientId = get<SpotifyConfig>().clientId,
            clientSecret = get<SpotifyConfig>().clientSecret,
            tokenUrl = get<SpotifyConfig>().tokenUrl,
            baseUrl = get<SpotifyConfig>().baseUrl,
            tokenCache = get()
        )
    }

    single { MusicService(get()) }

    single<Controller> { MusicController(get()) }
}
