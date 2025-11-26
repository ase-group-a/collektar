package di.modules

import com.collektar.integration.igdb.IGDBClient
import com.collektar.integration.igdb.IGDBClientImpl
import com.collektar.integration.igdb.IGDBConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import controllers.Controller
import controllers.GamesController
import io.ktor.server.application.ApplicationEnvironment
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.GamesService

fun IGDBModule(env: ApplicationEnvironment) = module {
    single { IGDBConfig.fromEnv(env) }
    single { OauthTokenCache() }
    single { OauthTokenProvider(get(), get()) }
    single<IGDBClient> { IGDBClientImpl(get(), get(), get()) }
    single { GamesService(get()) }
    single<Controller>(named("games")) { GamesController(get()) }
}