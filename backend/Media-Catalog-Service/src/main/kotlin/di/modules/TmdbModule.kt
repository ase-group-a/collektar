package di.modules

import controllers.Controller
import controllers.MovieController
import controllers.ShowController
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbClientImpl
import integration.tmdb.TmdbConfig
import io.ktor.server.application.ApplicationEnvironment
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.MovieService
import service.ShowService

fun tmdbModule(env: ApplicationEnvironment) = module {
    single { TmdbConfig.fromEnv(env) }

    single<TmdbClient> { TmdbClientImpl(get(), get()) }

    single { MovieService(get()) }
    single { ShowService(get()) }

    single<Controller>(named("movies")) { MovieController(get()) }
    single<Controller>(named("shows")) { ShowController(get()) }
}
