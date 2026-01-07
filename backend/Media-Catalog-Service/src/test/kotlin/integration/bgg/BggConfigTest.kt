package integration.bgg

import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class BggConfigTest {

    @Test
    fun `fromEnv reads values from ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            "bgg.baseUrl" to "https://test.example.com",
            "bgg.apiToken" to "test-token-123",
            "bgg.minDelayMs" to "3000"
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val cfg = BggConfig.fromEnv(env)

        assertEquals("https://test.example.com", cfg.baseUrl)
        assertEquals("test-token-123", cfg.token)
        assertEquals(3000L, cfg.minDelayMillis)
    }
}
