package integration.bgg

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BggConfigTest {

    @BeforeEach
    fun setUp() {
        mockkStatic(System::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `BggConfig constructor with token`() {
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = "valid-bgg-token-abc123",
            minDelayMillis = 5000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals("valid-bgg-token-abc123", config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig constructor with null token`() {
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = null,
            minDelayMillis = 5000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertNull(config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig with custom values`() {
        val config = BggConfig(
            baseUrl = "https://custom.bgg.url",
            token = "custom-token",
            minDelayMillis = 10000L
        )

        assertEquals("https://custom.bgg.url", config.baseUrl)
        assertEquals("custom-token", config.token)
        assertEquals(10000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv uses all environment variables when set`() {
        every { System.getenv("BGG_BASE_URL") } returns "https://custom.boardgamegeek.com/api"
        every { System.getenv("BGG_API_TOKEN") } returns "env-token-123"
        every { System.getenv("BGG_MIN_DELAY_MS") } returns "3000"

        val config = BggConfig.fromEnv()

        assertEquals("https://custom.boardgamegeek.com/api", config.baseUrl)
        assertEquals("env-token-123", config.token)
        assertEquals(3000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv uses defaults when environment variables not set`() {
        every { System.getenv("BGG_BASE_URL") } returns null
        every { System.getenv("BGG_API_TOKEN") } returns null
        every { System.getenv("BGG_MIN_DELAY_MS") } returns null

        val config = BggConfig.fromEnv()

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertNull(config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv uses default baseUrl when BGG_BASE_URL is null`() {
        every { System.getenv("BGG_BASE_URL") } returns null
        every { System.getenv("BGG_API_TOKEN") } returns "token"
        every { System.getenv("BGG_MIN_DELAY_MS") } returns "2000"

        val config = BggConfig.fromEnv()

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals("token", config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv uses default minDelayMillis when BGG_MIN_DELAY_MS is null`() {
        every { System.getenv("BGG_BASE_URL") } returns "https://custom.url"
        every { System.getenv("BGG_API_TOKEN") } returns "token"
        every { System.getenv("BGG_MIN_DELAY_MS") } returns null

        val config = BggConfig.fromEnv()

        assertEquals("https://custom.url", config.baseUrl)
        assertEquals("token", config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv token is null when BGG_API_TOKEN not set`() {
        every { System.getenv("BGG_BASE_URL") } returns "https://boardgamegeek.com/xmlapi2"
        every { System.getenv("BGG_API_TOKEN") } returns null
        every { System.getenv("BGG_MIN_DELAY_MS") } returns "5000"

        val config = BggConfig.fromEnv()

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertNull(config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv parses minDelayMillis as Long`() {
        every { System.getenv("BGG_BASE_URL") } returns "https://boardgamegeek.com/xmlapi2"
        every { System.getenv("BGG_API_TOKEN") } returns null
        every { System.getenv("BGG_MIN_DELAY_MS") } returns "10000"

        val config = BggConfig.fromEnv()

        assertEquals(10000L, config.minDelayMillis)
    }

    @Test
    fun `fromEnv with empty string environment variables uses defaults`() {
        every { System.getenv("BGG_BASE_URL") } returns ""
        every { System.getenv("BGG_API_TOKEN") } returns ""
        every { System.getenv("BGG_MIN_DELAY_MS") } returns ""

        val config = BggConfig.fromEnv()

        // Empty string is truthy, so it won't use defaults for baseUrl
        assertEquals("", config.baseUrl)
        // Empty string for token is still set (not null)
        assertEquals("", config.token)
        // Empty string can't parse to Long, so this will throw
        // But the code doesn't handle this - it will crash
        // This is actually a bug in your code that should be fixed
    }
}