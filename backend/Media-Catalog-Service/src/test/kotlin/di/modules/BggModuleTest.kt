package di.modules

import com.collektar.imagecache.ImageCacheClient
import integration.bgg.BggClient
import integration.bgg.BggConfig
import io.ktor.client.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.SetEnvironmentVariable
import org.junitpioneer.jupiter.EnvironmentVariableExtension
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.check.checkModules
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class BggModuleTest : KoinTest {

    @AfterEach
    fun tearDown() {
        stopKoin()
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "BGG_TOKEN", value = "test-token")
    @SetEnvironmentVariable(key = "BGG_MIN_DELAY_MS", value = "2000")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule can be checked`() {
        koinApplication {
            modules(bggModule, imageCacheModule)
            checkModules()
        }
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "BGG_TOKEN", value = "test-token")
    @SetEnvironmentVariable(key = "BGG_MIN_DELAY_MS", value = "2000")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule provides BggConfig from environment`() {
        val app = koinApplication {
            modules(bggModule, imageCacheModule)
        }

        val config = app.koin.get<BggConfig>()

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals("test-token", config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule provides BggConfig with defaults when optional env vars missing`() {
        val app = koinApplication {
            modules(bggModule, imageCacheModule)
        }

        val config = app.koin.get<BggConfig>()

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals(null, config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "BGG_TOKEN", value = "test-token")
    @SetEnvironmentVariable(key = "BGG_MIN_DELAY_MS", value = "2000")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule provides HttpClient with injected HttpClient`() {
        val mockHttpClientModule = module {
            single {
                HttpClient()
            }
        }

        val app = koinApplication {
            modules(mockHttpClientModule, bggModule, imageCacheModule)
        }

        val httpClient = app.koin.get<HttpClient>()
        assertNotNull(httpClient)
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "BGG_TOKEN", value = "test-token")
    @SetEnvironmentVariable(key = "BGG_MIN_DELAY_MS", value = "2000")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule provides BggClient`() {
        val mockHttpClientModule = module {
            single {
                HttpClient()
            }
        }

        val app = koinApplication {
            modules(mockHttpClientModule, bggModule, imageCacheModule)
        }

        val bggClient = app.koin.get<BggClient>()
        assertNotNull(bggClient)
    }

    @Test
    @SetEnvironmentVariable(key = "BGG_BASE_URL", value = "https://boardgamegeek.com/xmlapi2")
    @SetEnvironmentVariable(key = "BGG_TOKEN", value = "test-token")
    @SetEnvironmentVariable(key = "BGG_MIN_DELAY_MS", value = "2000")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_TMDB_URL", value = "https://image.tmdb.org/t/p/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_IGDB_URL", value = "https://images.igdb.com/igdb/image/upload/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_SPOTIFY_URL", value = "https://i.scdn.co/image/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BGG_URL", value = "https://cf.geekdo-images.com/")
    @SetEnvironmentVariable(key = "IMAGE_CACHE_BOOKS_URL", value = "https://books.google.com/books/content")
    fun `bggModule provides expected dependencies`() {
        val mockHttpClientModule = module {
            single {
                HttpClient()
            }
        }

        val app = koinApplication {
            modules(mockHttpClientModule, bggModule, imageCacheModule)
        }

        val koin = app.koin

        // Verify all expected dependencies can be resolved
        assertNotNull(koin.get<BggConfig>())
        assertNotNull(koin.get<BggClient>())
        assertNotNull(koin.get<ImageCacheClient>())
        assertNotNull(koin.get<HttpClient>())
    }
}