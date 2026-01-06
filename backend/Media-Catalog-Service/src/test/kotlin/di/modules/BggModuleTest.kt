package di.modules

import controllers.BoardGameController
import controllers.Controller
import integration.bgg.BggClient
import integration.bgg.BggClientImpl
import integration.bgg.BggConfig
import io.ktor.client.HttpClient
import io.ktor.server.application.ApplicationEnvironment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import service.BggMediaService
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class BggModuleTest {

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
        unmockkAll()
    }

    @Test
    fun `bggModule provides expected dependencies`() {
        // Mock BggConfig.fromEnv()
        mockkObject(BggConfig.Companion)
        val expectedConfig = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = null,
            minDelayMillis = 5000L
        )
        every { BggConfig.fromEnv() } returns expectedConfig

        val env = mockk<ApplicationEnvironment>(relaxed = true)
        val httpClient = mockk<HttpClient>(relaxed = true)

        startKoin {
            modules(
                module {
                    single { httpClient }
                },
                bggModule(env)
            )
        }

        val cfg = GlobalContext.get().get<BggConfig>()
        assertEquals("https://boardgamegeek.com/xmlapi2", cfg.baseUrl)
        assertEquals(null, cfg.token)
        assertEquals(5000L, cfg.minDelayMillis)

        val client = GlobalContext.get().get<BggClient>()
        assertIs<BggClientImpl>(client)

        val service = GlobalContext.get().get<BggMediaService>()
        assertNotNull(service)

        val controller = GlobalContext.get().get<Controller>()
        assertIs<BoardGameController>(controller)
    }

    @Test
    fun `bggModule with custom config`() {
        mockkObject(BggConfig.Companion)
        val expectedConfig = BggConfig(
            baseUrl = "https://custom.bgg.url",
            token = "custom-token",
            minDelayMillis = 10000L
        )
        every { BggConfig.fromEnv() } returns expectedConfig

        val env = mockk<ApplicationEnvironment>(relaxed = true)
        val httpClient = mockk<HttpClient>(relaxed = true)

        startKoin {
            modules(
                module {
                    single { httpClient }
                },
                bggModule(env)
            )
        }

        val cfg = GlobalContext.get().get<BggConfig>()
        assertEquals("https://custom.bgg.url", cfg.baseUrl)
        assertEquals("custom-token", cfg.token)
        assertEquals(10000L, cfg.minDelayMillis)
    }
}