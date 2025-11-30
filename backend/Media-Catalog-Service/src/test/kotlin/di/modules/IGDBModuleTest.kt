package di.modules

import com.collektar.integration.igdb.IGDBClient
import com.collektar.integration.igdb.IGDBClientImpl
import com.collektar.integration.igdb.IGDBConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import controllers.Controller
import controllers.GamesController
import di.coreModule
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import kotlin.test.assertIs

const val IGDB_CLIENT_ID = "igdb_client_id"
const val IGDB_CLIENT_SECRET = "igdb_client_secret"
const val IGDB_BASE_URL = "https://api.igdb.com/v4"
const val IGDB_TOKEN_URL = "https://id.twitch.tv/oauth2/token"

class IGDBModuleTest {
    @BeforeEach
    fun setUp() {
        try {
            stopKoin()
        } catch (_: Exception) {
        }
    }

    @AfterEach
    fun tearDown() {
        try {
            stopKoin()
        } catch (_: Exception) {
        }
    }

    @Test
    fun `igdbModule provides expected dependencies`() {
        val mapConfig = MapApplicationConfig(
            "igdb.clientId" to IGDB_CLIENT_ID,
            "igdb.clientSecret" to IGDB_CLIENT_SECRET,
            "igdb.baseUrl" to IGDB_BASE_URL,
            "igdb.tokenUrl" to IGDB_TOKEN_URL
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        startKoin {
            modules(listOf(coreModule, igdbModule(env)))
        }

        val cfg = GlobalContext.get().get<IGDBConfig>(named(IGDB_CONFIG_NAME))
        assertEquals(IGDB_CLIENT_ID, cfg.clientId)
        assertEquals(IGDB_CLIENT_SECRET, cfg.clientSecret)
        assertEquals(IGDB_BASE_URL, cfg.baseUrl)
        assertEquals(IGDB_TOKEN_URL, cfg.tokenUrl)

        val cache = GlobalContext.get().get<OauthTokenCache>(named(IGDB_TOKEN_CACHE_NAME))
        assertNotNull(cache)

        val tokenProvider = GlobalContext.get().get<OauthTokenProvider>(named(IGDB_TOKEN_PROVIDER_NAME))
        assertNotNull(tokenProvider)

        val client = GlobalContext.get().get<IGDBClient>()
        assertIs<IGDBClientImpl>(client)

        val gamesService = GlobalContext.get().get<service.GamesService>()
        assertNotNull(gamesService)

        val controller = GlobalContext.get().get<Controller>(named(IGDB_CONTROLLER_NAME))
        assertIs<GamesController>(controller)
    }
}