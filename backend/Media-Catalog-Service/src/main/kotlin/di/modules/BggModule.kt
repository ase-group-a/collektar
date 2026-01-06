package di.modules

import com.collektar.imagecache.ImageCacheClient
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
    single { BggConfig.fromEnv() }

    single<BggClient> {
        BggClientImpl(
            get<HttpClient>(),
            get<BggConfig>(),
            get<ImageCacheClient>()  // Inject image cache client
        )
    }

    single { BggMediaService(get()) }

    single<Controller> { BoardGameController(get()) }
}