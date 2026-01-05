package integration.bgg

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BggResponseTest {

    @Test
    fun `BggConfig with all parameters`() {
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
            minDelayMillis = 2000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertNull(config.token)
        assertEquals(2000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig with custom delay`() {
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = null,
            minDelayMillis = 5000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `MediaItem with all parameters for board game`() {
        val item = MediaItem(
            id = "bgg:174430",
            title = "Gloomhaven",
            type = MediaType.BOARDGAME,
            imageUrl = "https://cf.geekdo-images.com/sZYp_3BTDGjh2unaZfZmuA__original/img/FBxMvxdYDbMjrYtDCh0L0xOp_4Y=/0x0/filters:format(jpeg)/pic2437871.jpg",
            description = "A cooperative game of tactical combat",
            source = "BGG"
        )

        assertEquals("bgg:174430", item.id)
        assertEquals("Gloomhaven", item.title)
        assertEquals(MediaType.BOARDGAME, item.type)
        assertEquals("https://cf.geekdo-images.com/sZYp_3BTDGjh2unaZfZmuA__original/img/FBxMvxdYDbMjrYtDCh0L0xOp_4Y=/0x0/filters:format(jpeg)/pic2437871.jpg", item.imageUrl)
        assertEquals("A cooperative game of tactical combat", item.description)
        assertEquals("BGG", item.source)
    }

    @Test
    fun `MediaItem with null image and description`() {
        val item = MediaItem(
            id = "bgg:12345",
            title = "Unknown Game",
            type = MediaType.BOARDGAME,
            imageUrl = null,
            description = null,
            source = "BGG"
        )

        assertEquals("bgg:12345", item.id)
        assertEquals("Unknown Game", item.title)
        assertNull(item.imageUrl)
        assertNull(item.description)
    }

    @Test
    fun `SearchResult with items`() {
        val item = MediaItem(
            id = "bgg:13",
            title = "Catan",
            type = MediaType.BOARDGAME,
            imageUrl = "https://cf.geekdo-images.com/W3Bsga_uLP9kO91gZ7H8yw__original/img/M_3Vg1j2HlNgkv7PL2xl2BJE2sM=/0x0/filters:format(jpeg)/pic2419375.jpg",
            description = null,
            source = "BGG"
        )

        val result = SearchResult(
            total = 100,
            limit = 20,
            offset = 0,
            items = listOf(item)
        )

        assertEquals(100, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)
        assertEquals("bgg:13", result.items[0].id)
        assertEquals("Catan", result.items[0].title)
    }

    @Test
    fun `SearchResult with empty items`() {
        val result = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        assertEquals(0, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `SearchResult with pagination`() {
        val item1 = MediaItem("bgg:1", "Brass: Birmingham", MediaType.BOARDGAME, null, null, "BGG")
        val item2 = MediaItem("bgg:2", "Pandemic Legacy: Season 1", MediaType.BOARDGAME, null, null, "BGG")

        val result = SearchResult(
            total = 150,
            limit = 20,
            offset = 40,
            items = listOf(item1, item2)
        )

        assertEquals(150, result.total)
        assertEquals(20, result.limit)
        assertEquals(40, result.offset)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `MediaItem id format extraction`() {
        val item = MediaItem("bgg:174430", "Gloomhaven", MediaType.BOARDGAME, null, null, "BGG")

        assertEquals("bgg:174430", item.id)

        // Verify we can extract the numeric ID
        val numericId = item.id.removePrefix("bgg:").toLongOrNull()
        assertEquals(174430L, numericId)
    }
}