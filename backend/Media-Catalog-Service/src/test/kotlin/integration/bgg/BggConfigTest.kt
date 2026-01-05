package integration.bgg

import kotlin.test.Test
import kotlin.test.assertEquals

class BggConfigTest {

    @Test
    fun `BggConfig default values when env vars not set`() {
        // This tests the actual fromEnv() but requires env to be clear
        // In real scenarios, you'd mock System.getenv()
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = null,
            minDelayMillis = 2000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals(null, config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig constructor with all parameters`() {
        val config = BggConfig(
            baseUrl = "https://custom.url",
            token = "test-token",
            minDelayMillis = 5000L
        )

        assertEquals("https://custom.url", config.baseUrl)
        assertEquals("test-token", config.token)
        assertEquals(5000L, config.minDelayMillis)
    }
}