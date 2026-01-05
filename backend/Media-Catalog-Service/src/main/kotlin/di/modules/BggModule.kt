package di.modules

import controllers.BoardGameController
import controllers.Controller
import integration.bgg.BggClient
import integration.bgg.BggClientImpl
import integration.bgg.BggConfig
import io.ktor.client.HttpClient
import io.ktor.server.application.ApplicationEnvironment
import org.koin.dsl.module
import service.BggMediaService

fun bggModule(env: ApplicationEnvironment) = module {

    // Your fromEnv() takes no args
    single { BggConfig.fromEnv() }

    // Use the shared HttpClient from coreModule
    single<BggClient> { BggClientImpl(get<HttpClient>(), get()) }

    single { BggMediaService(get()) }

    // Controller for auto-registration
    single<Controller> { BoardGameController(get()) }
}
