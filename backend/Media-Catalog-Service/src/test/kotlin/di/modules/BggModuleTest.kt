package di.modules

import com.collektar.imagecache.ImageCacheClient
import integration.bgg.BggClient
import integration.bgg.BggConfig
import io.ktor.client.HttpClient
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BggModuleTest {

    @BeforeEach
    fun setup() {
        try { stopKoin() } catch (_: Exception) {}
    }

    @AfterEach
    fun teardown() {
        try { stopKoin() } catch (_: Exception) {}
    }

    private fun buildEnv(config: Map<String, String>): ApplicationEnvironment {
        val mapConfig = MapApplicationConfig().apply {
            config.forEach { (k, v) -> put(k, v) }
        }
        return mockk<ApplicationEnvironment>().also { env ->
            every { env.config } returns mapConfig
        }
    }

    @Test
    fun `bggModule can be checked`() {
        val env = buildEnv(
            mapOf(
                "bgg.baseUrl" to "https://boardgamegeek.com/xmlapi2",
                "bgg.apiToken" to "test-token",
                "bgg.minDelayMs" to "2000",
            )
        )

        val httpClient = mockk<HttpClient>(relaxed = true)
        val imageCacheClient = mockk<ImageCacheClient>(relaxed = true)

        startKoin {
            modules(
                module {
                    single { httpClient }
                    single<ImageCacheClient> { imageCacheClient }
                },
                bggModule(env),
            )
        }

        val koin = GlobalContext.get()
        assertNotNull(koin.get<BggConfig>(named(BGG_CONFIG_NAME)))
        assertNotNull(koin.get<BggClient>())
        assertNotNull(koin.get<ImageCacheClient>())
        assertNotNull(koin.get<HttpClient>())
    }


    @Test
    fun `bggModule provides BggConfig from config`() {
        val env = buildEnv(
            mapOf(
                "bgg.baseUrl" to "https://boardgamegeek.com/xmlapi2",
                "bgg.apiToken" to "test-token",
                "bgg.minDelayMs" to "2000",
            )
        )

        val httpClient = mockk<HttpClient>(relaxed = true)
        val imageCacheClient = mockk<ImageCacheClient>(relaxed = true)

        startKoin {
            modules(
                module {
                    single { httpClient }
                    single<ImageCacheClient> { imageCacheClient }
                },
                bggModule(env),
            )
        }

        val cfg = GlobalContext.get().get<BggConfig>(named(BGG_CONFIG_NAME))
        assertEquals("https://boardgamegeek.com/xmlapi2", cfg.baseUrl)
        assertEquals("test-token", cfg.token)
        assertEquals(2000L, cfg.minDelayMillis)
    }

}