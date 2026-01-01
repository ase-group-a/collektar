package di

import com.collektar.di.modules.imageCacheModule
import di.modules.*
import io.ktor.server.application.*

fun appModules(env: ApplicationEnvironment) = listOf(
    coreModule,
    spotifyModule(env),
    tmdbModule(env),
    igdbModule(env),
    booksModule(env),
    imageCacheModule(env),
)
