package di

import integration.spotify.SpotifyConfig
import io.ktor.server.application.*
import org.koin.dsl.module

fun configModule(env: ApplicationEnvironment) = module {
    single { SpotifyConfig.fromEnv(env) }
}
