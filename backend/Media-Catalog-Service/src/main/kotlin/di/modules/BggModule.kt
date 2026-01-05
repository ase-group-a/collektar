package di.modules

import controllers.BoardGameController
import controllers.Controller
import integration.bgg.BggClient
import integration.bgg.BggClientImpl
import integration.bgg.BggConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import service.BggMediaService

fun bggModule(env: ApplicationEnvironment) = module {

    // Load BGG config from environment variables
    single {
        BggConfig.fromEnv()
    }

    // BGG HTTP client (reuse shared Ktor HttpClient from coreModule)
    single<BggClient> {
        BggClientImpl(
            get(), // HttpClient from coreModule
            get()  // BggConfig
        )
    }

    // Service layer
    single {
        BggMediaService(get())
    }

    // Controller (auto-registered)
    single<Controller> {
        BoardGameController(get())
    }
}
