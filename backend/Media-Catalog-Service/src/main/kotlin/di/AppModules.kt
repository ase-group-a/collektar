package di

import di.modules.*
import io.ktor.server.application.*

fun appModules(env: ApplicationEnvironment) = listOf(
    coreModule,
    spotifyModule(env),
    tmdbModule(env),
    booksModule(env)
)
