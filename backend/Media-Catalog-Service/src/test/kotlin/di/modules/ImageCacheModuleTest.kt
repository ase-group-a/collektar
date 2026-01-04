package di.modules

import com.collektar.controllers.ImageCacheController
import com.collektar.di.modules.IMAGE_CACHE_CONTROLLER_NAME
import com.collektar.di.modules.imageCacheModule
import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageCacheConfig
import com.collektar.service.ImageCacheService
import controllers.Controller
import di.coreModule
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import redis.clients.jedis.RedisClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

const val IMAGE_CACHE_URL_PREFIX = "/api/media/images"
const val IMAGE_CACHE_REDIS_URL = "redis://default:pass@localhost:6379"
const val IMAGE_CACHE_REDIS_POOL_SIZE = "10"
const val IMAGE_CACHE_TTL = "604800" // 1 week
const val SPOTIFY_IMAGE_URL = "https://i.scdn.co/image/"
const val TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/w500/"
const val IGDB_IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/"
const val GOOGLE_BOOKS_IMAGE_URL = "https://books.google.com/books/content?printsec=frontcover&img=1&zoom=1&id="

class ImageCacheModuleTest {
    private val redis = mockk<RedisClient>()
    
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
    fun `imageCacheModule provides expected dependencies`() {
        val mapConfig = MapApplicationConfig(
            "imageCache.urlPrefix" to IMAGE_CACHE_URL_PREFIX,
            "imageCache.redisUrl" to IMAGE_CACHE_REDIS_URL,
            "imageCache.redisPoolSize" to IMAGE_CACHE_REDIS_POOL_SIZE,
            "imageCache.cacheTTL" to IMAGE_CACHE_TTL,
            "imageCache.spotifyUrlPrefix" to SPOTIFY_IMAGE_URL,
            "imageCache.tmdbUrlPrefix" to TMDB_IMAGE_URL,
            "imageCache.igdbUrlPrefix" to IGDB_IMAGE_URL,
            "imageCache.googleBooksUrlPrefix" to GOOGLE_BOOKS_IMAGE_URL
        )
        
        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        startKoin {
            modules(
                listOf(
                    coreModule,
                    module {
                        single { redis } // Mocked redis client
                    },
                    imageCacheModule(env)
                )
            )
        }
        
        val cfg = GlobalContext.get().get<ImageCacheConfig>()
        assertEquals(IMAGE_CACHE_URL_PREFIX, cfg.urlPrefix)
        assertEquals(IMAGE_CACHE_REDIS_URL, cfg.redisUrl)
        assertEquals(IMAGE_CACHE_REDIS_POOL_SIZE.toIntOrNull(), cfg.redisPoolSize)
        assertEquals(IMAGE_CACHE_TTL.toLongOrNull(), cfg.cacheTTL)
        assertEquals(SPOTIFY_IMAGE_URL, cfg.spotifyUrlPrefix)
        assertEquals(TMDB_IMAGE_URL, cfg.tmdbUrlPrefix)
        assertEquals(IGDB_IMAGE_URL, cfg.igdbUrlPrefix)
        assertEquals(GOOGLE_BOOKS_IMAGE_URL, cfg.googleBooksUrlPrefix)
        
        val imageCacheClient = GlobalContext.get().get<ImageCacheClient>()
        assertNotNull(imageCacheClient)
        
        val imageCacheService = GlobalContext.get().get<ImageCacheService>()
        assertNotNull(imageCacheService)
        
        val controller = GlobalContext.get().get<Controller>(named(IMAGE_CACHE_CONTROLLER_NAME))
        assertIs<ImageCacheController>(controller)
    }
}