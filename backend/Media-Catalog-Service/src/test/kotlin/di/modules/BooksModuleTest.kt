package di.modules

import com.collektar.imagecache.ImageCacheClient
import integration.books.BooksClient
import integration.books.BooksClientImpl
import integration.books.BooksConfig
import controllers.BooksController
import controllers.Controller
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.*
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import service.BooksService
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

class BooksModuleTest {

    private val imageCacheClient = mockk<ImageCacheClient>()
    
    @BeforeEach
    fun setup() {
        try { stopKoin() } catch (_: Exception) {}
    }

    @AfterEach
    fun teardown() {
        try { stopKoin() } catch (_: Exception) {}
    }

    @Test
    fun `booksModule provides expected dependencies`() {

        val mapConfig = MapApplicationConfig(
            "books.bookApiKey" to "TEST_KEY",
            "books.baseUrl" to "https://example.com"
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val httpClient = mockk<io.ktor.client.HttpClient>(relaxed = true)

        startKoin {
            modules(
                module {
                    single { httpClient }
                    single { imageCacheClient }
                },
                booksModule(env)
            )
        }

        val cfg = GlobalContext.get().get<BooksConfig>(named(BOOKS_CONFIG_NAME))
        assertEquals("TEST_KEY", cfg.bookApiKey)
        assertEquals("https://example.com", cfg.baseUrl)

        val client = GlobalContext.get().get<BooksClient>()
        assertIs<BooksClientImpl>(client)

        val service = GlobalContext.get().get<BooksService>()
        assertNotNull(service)

        val controller = GlobalContext.get().get<Controller>(named(BOOKS_CONTROLLER_NAME))
        assertIs<BooksController>(controller)
    }
}
