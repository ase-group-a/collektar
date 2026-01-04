package di.modules

import com.collektar.di.modules.OauthParameterType
import com.collektar.integration.igdb.IGDBClient
import com.collektar.integration.igdb.IGDBClientImpl
import com.collektar.integration.igdb.IGDBConfig
import com.collektar.integration.shared.OauthConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import controllers.Controller
import controllers.GamesController
import io.ktor.server.application.*
import org.koin.core.qualifier.named
import org.koin.dsl.binds
import org.koin.dsl.module
import service.GamesService

const val IGDB_CONFIG_NAME = "igdb_config"
const val IGDB_TOKEN_CACHE_NAME = "igdb_token_cache"
const val IGDB_TOKEN_PROVIDER_NAME = "igdb_token_provider"

const val IGDB_CONTROLLER_NAME = "games"

fun igdbModule(env: ApplicationEnvironment) = module {
    single(named(IGDB_CONFIG_NAME)) { IGDBConfig.fromEnv(env) } binds arrayOf(OauthConfig::class, IGDBConfig::class)
    single(named(IGDB_TOKEN_CACHE_NAME)) { OauthTokenCache() }
    single(named(IGDB_TOKEN_PROVIDER_NAME)) {
        OauthTokenProvider(
            get(),
            get(named(IGDB_TOKEN_CACHE_NAME)),
            get(named(IGDB_CONFIG_NAME)),
            OauthParameterType.URL_PARAMETER_URLENCODED
        )
    }
    single<IGDBClient> {
        IGDBClientImpl(
            get(),
            get(named(IGDB_CONFIG_NAME)),
            get(named(IGDB_TOKEN_PROVIDER_NAME))
        )
    }
    single { GamesService(get(), get()) }
    single<Controller>(named(IGDB_CONTROLLER_NAME)) { GamesController(get()) }
}