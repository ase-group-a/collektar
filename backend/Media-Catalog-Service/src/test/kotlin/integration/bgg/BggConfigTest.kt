package integration.bgg

import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junitpioneer.jupiter.ClearEnvironmentVariable
import org.junitpioneer.jupiter.EnvironmentVariableExtension
import org.junitpioneer.jupiter.SetEnvironmentVariable
import kotlin.test.assertEquals

@ExtendWith(EnvironmentVariableExtension::class)
class BggConfigTest {

    private fun buildEnv(config: Map<String, String>): ApplicationEnvironment {
        val mapConfig = MapApplicationConfig().apply {
            config.forEach { (k, v) -> put(k, v) }
        }
        return mockk<ApplicationEnvironment>().also { env ->
            every { env.config } returns mapConfig
        }
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses all environment variables when set`() {
        val env = buildEnv(
            mapOf(
                "BGG_BASE_URL" to "https://test.example.com",
                "BGG_API_TOKEN" to "test-token-123",
                "BGG_MIN_DELAY_MS" to "3000"
            )
        )

        val config = BggConfig.fromEnv(env)

        assertEquals("https://test.example.com", config.baseUrl)
        assertEquals("test-token-123", config.token)
        assertEquals(3000L, config.minDelayMillis)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses default baseUrl when not set`() {
        val env = buildEnv(emptyMap())

        val config = BggConfig.fromEnv(env)

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses null token when not set`() {
        val env = buildEnv(
            mapOf("BGG_BASE_URL" to "https://boardgamegeek.com/xmlapi2")
        )

        val config = BggConfig.fromEnv(env)

        assertEquals(null, config.token)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses default minDelayMillis when not set`() {
        val env = buildEnv(
            mapOf("BGG_BASE_URL" to "https://boardgamegeek.com/xmlapi2")
        )

        val config = BggConfig.fromEnv(env)

        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv handles invalid minDelayMillis gracefully`() {
        val env = buildEnv(
            mapOf(
                "BGG_BASE_URL" to "https://boardgamegeek.com/xmlapi2",
                "BGG_MIN_DELAY_MS" to "invalid"
            )
        )

        val config = BggConfig.fromEnv(env)

        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses provided token when set`() {
        val env = buildEnv(
            mapOf(
                "BGG_BASE_URL" to "https://boardgamegeek.com/xmlapi2",
                "BGG_API_TOKEN" to "my-token"
            )
        )

        val config = BggConfig.fromEnv(env)

        assertEquals("my-token", config.token)
    }

    @Test
    @ClearEnvironmentVariable(key = "BGG_BASE_URL")
    @ClearEnvironmentVariable(key = "BGG_API_TOKEN")
    @ClearEnvironmentVariable(key = "BGG_MIN_DELAY_MS")
    fun `fromEnv uses provided baseUrl when set`() {
        val env = buildEnv(
            mapOf("BGG_BASE_URL" to "https://custom.url.com")
        )

        val config = BggConfig.fromEnv(env)

        assertEquals("https://custom.url.com", config.baseUrl)
    }
}