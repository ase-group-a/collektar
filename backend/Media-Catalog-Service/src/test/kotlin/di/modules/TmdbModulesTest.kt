package di.modules


import com.collektar.imagecache.ImageCacheClient
import controllers.Controller
import controllers.MovieController
import controllers.ShowController
import integration.tmdb.TmdbClient
import integration.tmdb.TmdbClientImpl
import integration.tmdb.TmdbConfig
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
import service.MovieService
import service.ShowService
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TmdbModuleTest {
    private val imageCacheClient = mockk<ImageCacheClient>()
    
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
    fun `tmdbModule provides expected dependencies`() {
        val mapConfig = MapApplicationConfig(
            "tmdb.bearerToken" to "test-bearer-token",
            "tmdb.baseUrl" to "https://api.themoviedb.org/3"
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        startKoin {
            modules(
                listOf(
                    module {
                        single { HttpClient() }
                    },
                    module {
                        single { imageCacheClient }
                    },
                    tmdbModule(env)
                )
            )
        }

        val cfg = GlobalContext.get().get<TmdbConfig>()
        assertEquals("test-bearer-token", cfg.bearerToken)
        assertEquals("https://api.themoviedb.org/3", cfg.baseUrl)

        val client = GlobalContext.get().get<TmdbClient>()
        assertIs<TmdbClientImpl>(client)

        val movieService = GlobalContext.get().get<MovieService>()
        val showService = GlobalContext.get().get<ShowService>()
        assertNotNull(movieService)
        assertNotNull(showService)

        val movieController = GlobalContext.get().get<Controller>(named("movies"))
        val showController = GlobalContext.get().get<Controller>(named("shows"))

        assertIs<MovieController>(movieController)
        assertIs<ShowController>(showController)
    }
}
