package di.modules

import com.collektar.imagecache.ImageCacheClient
import controllers.BoardGameController
import controllers.Controller
import integration.bgg.BggClient
import integration.bgg.BggClientImpl
import integration.bgg.BggConfig
import io.ktor.client.HttpClient
import io.ktor.server.application.ApplicationEnvironment
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.BggMediaService

const val BGG_CONFIG_NAME = "bgg_config"
const val BGG_CONTROLLER_NAME = "boardgames"

fun bggModule(env: ApplicationEnvironment) = module {
    single(named(BGG_CONFIG_NAME)) { BggConfig.fromEnv(env) }

    single<BggClient> {
        BggClientImpl(
            get<HttpClient>(),
            get(named(BGG_CONFIG_NAME)),
            get<ImageCacheClient>()
        )
    }

    single { BggMediaService(get()) }

    single<Controller>(named(BGG_CONTROLLER_NAME)) { BoardGameController(get()) }
}