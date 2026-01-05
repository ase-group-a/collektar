package integration.bgg

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BggConfigTest {

    @Test
    fun `BggConfig constructor with all parameters`() {
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = "test-token-abc123",
            minDelayMillis = 2000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals("test-token-abc123", config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig with null token`() {
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
}