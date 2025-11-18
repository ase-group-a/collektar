package integration.tmdb

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TmdbConfigTest {

    @Test
    fun `fromEnv reads values from ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            "tmdb.bearerToken" to "bearer",
            "tmdb.baseUrl" to "https://api.themoviedb.org/3"
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig
        val cfg = TmdbConfig.fromEnv(env)

        assertEquals("bearer", cfg.bearerToken)
        assertEquals("https://api.themoviedb.org/3", cfg.baseUrl)
    }

    @Test
    fun `fromEnv uses default baseUrl when none configured or in env`() {
        val mapConfig = MapApplicationConfig(
            "tmdb.bearerToken" to "bearer"
            // no tmdb.baseUrl
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val cfg = TmdbConfig.fromEnv(env)

        assertEquals("bearer", cfg.bearerToken)
        assertEquals("https://api.themoviedb.org/3", cfg.baseUrl)
    }
}


