package di.modules

import com.collektar.HttpProvider
import controllers.BoardGameController
import controllers.Controller
import integration.bgg.BggClient
import integration.bgg.BggClientImpl
import integration.bgg.BggConfig
import io.ktor.server.application.*
import org.koin.dsl.module
import service.BggMediaService

fun bggModule(env: ApplicationEnvironment) = module {

    // Load BGG settings from env variables
    single { BggConfig.fromEnv() }

    // Use HttpProvider.client (your existing HTTP client)
    single<BggClient> { BggClientImpl(HttpProvider.client, get()) }

    // Business logic
    single { BggMediaService(get()) }

    // Controller for auto-registration
    single<Controller> { BoardGameController(get()) }
}
