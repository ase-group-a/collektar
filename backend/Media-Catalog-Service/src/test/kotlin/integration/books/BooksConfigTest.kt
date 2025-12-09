package integration.books

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.application.ApplicationEnvironment
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BooksConfigTest {

    @Test
    fun `BooksConfig reads values from env`() {
        val apiKey = "testKey"
        val baseUrl = "https://example.com/books"

        val mapConfig = MapApplicationConfig(
            "books.bookApiKey" to apiKey,
            "books.baseUrl" to baseUrl
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val config = BooksConfig.fromEnv(env)
        assertEquals(apiKey, config.bookApiKey)
        assertEquals(baseUrl, config.baseUrl)
    }
}