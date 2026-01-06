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
                "bgg.token" to "test-token",
                "bgg.minDelayMs" to "2000",
                "imageCache.tmdbUrl" to "https://image.tmdb.org/t/p/",
                "imageCache.igdbUrl" to "https://images.igdb.com/igdb/image/upload/",
                "imageCache.spotifyUrl" to "https://i.scdn.co/image/",
                "imageCache.bggUrl" to "https://cf.geekdo-images.com/",
                "imageCache.booksUrl" to "https://books.google.com/books/content"
            )
        )

        val httpClient = mockk<HttpClient>(relaxed = true)

        startKoin {
            modules(
                module { single { httpClient } },
                bggModule(env),
                imageCacheModule(env),
            )
        }

        // basic smoke: can resolve key beans
        val koin = GlobalContext.get()
        assertNotNull(koin.get<BggConfig>())
        assertNotNull(koin.get<BggClient>())
        assertNotNull(koin.get<ImageCacheClient>())
        assertNotNull(koin.get<HttpClient>())
    }

    @Test
    fun `bggModule provides BggConfig from config`() {
        val env = buildEnv(
            mapOf(
                "bgg.baseUrl" to "https://boardgamegeek.com/xmlapi2",
                "bgg.token" to "test-token",
                "bgg.minDelayMs" to "2000",
                "imageCache.tmdbUrl" to "https://image.tmdb.org/t/p/",
                "imageCache.igdbUrl" to "https://images.igdb.com/igdb/image/upload/",
                "imageCache.spotifyUrl" to "https://i.scdn.co/image/",
                "imageCache.bggUrl" to "https://cf.geekdo-images.com/",
                "imageCache.booksUrl" to "https://books.google.com/books/content"
            )
        )

        val httpClient = mockk<HttpClient>(relaxed = true)

        startKoin {
            modules(
                module { single { httpClient } },
                bggModule(env),
                imageCacheModule(env),
            )
        }

        val cfg = GlobalContext.get().get<BggConfig>()
        assertEquals("https://boardgamegeek.com/xmlapi2", cfg.baseUrl)
        assertEquals("test-token", cfg.token)
        assertEquals(2000L, cfg.minDelayMillis)
    }

    @Test
    fun `bggModule provides BggConfig with defaults when optional config missing`() {
        val env = buildEnv(
            mapOf(
                "bgg.baseUrl" to "https://boardgamegeek.com/xmlapi2",
                // token missing
                // minDelayMs missing -> default expected
                "imageCache.tmdbUrl" to "https://image.tmdb.org/t/p/",
                "imageCache.igdbUrl" to "https://images.igdb.com/igdb/image/upload/",
                "imageCache.spotifyUrl" to "https://i.scdn.co/image/",
                "imageCache.bggUrl" to "https://cf.geekdo-images.com/",
                "imageCache.booksUrl" to "https://books.google.com/books/content"
            )
        )

        val httpClient = mockk<HttpClient>(relaxed = true)

        startKoin {
            modules(
                module { single { httpClient } },
                bggModule(env),
                imageCacheModule(env),
            )
        }

        val cfg = GlobalContext.get().get<BggConfig>()
        assertEquals("https://boardgamegeek.com/xmlapi2", cfg.baseUrl)
        assertEquals(null, cfg.token)
        assertEquals(2000L, cfg.minDelayMillis)
    }
}
