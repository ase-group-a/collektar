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
            minDelayMillis = 5000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertNull(config.token)
        assertEquals(5000L, config.minDelayMillis)
    }

    @Test
    fun `BggConfig with custom delay`() {
        val config = BggConfig(
            baseUrl = "https://boardgamegeek.com/xmlapi2",
            token = null,
            minDelayMillis = 10000L
        )

        assertEquals("https://boardgamegeek.com/xmlapi2", config.baseUrl)
        assertEquals(10000L, config.minDelayMillis)
    }

    @Test
    fun `SearchResult with board game items`() {
        val item1 = MediaItem(
            id = "bgg:174430",
            title = "Gloomhaven",
            type = MediaType.BOARDGAME,
            imageUrl = "https://cf.geekdo-images.com/gloomhaven.jpg",
            description = null,
            source = "BGG"
        )
        val item2 = MediaItem(
            id = "bgg:13",
            title = "Catan",
            type = MediaType.BOARDGAME,
            imageUrl = "https://cf.geekdo-images.com/catan.jpg",
            description = null,
            source = "BGG"
        )

        val result = SearchResult(
            total = 100,
            limit = 20,
            offset = 0,
            items = listOf(item1, item2)
        )

        assertEquals(100, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("bgg:174430", result.items[0].id)
        assertEquals("Gloomhaven", result.items[0].title)
        assertEquals("bgg:13", result.items[1].id)
        assertEquals("Catan", result.items[1].title)
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
        val item = MediaItem(
            id = "bgg:1",
            title = "Test Game",
            type = MediaType.BOARDGAME,
            imageUrl = null,
            description = null,
            source = "BGG"
        )

        val result = SearchResult(
            total = 150,
            limit = 25,
            offset = 50,
            items = listOf(item)
        )

        assertEquals(150, result.total)
        assertEquals(25, result.limit)
        assertEquals(50, result.offset)
        assertEquals(1, result.items.size)
    }

    @Test
    fun `MediaItem for board game with all fields`() {
        val item = MediaItem(
            id = "bgg:174430",
            title = "Gloomhaven",
            type = MediaType.BOARDGAME,
            imageUrl = "https://cf.geekdo-images.com/sZYp_3BTDGjh2unaZfZmuA__original/img/image.jpg",
            description = "A cooperative game of tactical combat",
            source = "BGG"
        )

        assertEquals("bgg:174430", item.id)
        assertEquals("Gloomhaven", item.title)
        assertEquals(MediaType.BOARDGAME, item.type)
        assertEquals("https://cf.geekdo-images.com/sZYp_3BTDGjh2unaZfZmuA__original/img/image.jpg", item.imageUrl)
        assertEquals("A cooperative game of tactical combat", item.description)
        assertEquals("BGG", item.source)
    }

    @Test
    fun `MediaItem for board game with null optional fields`() {
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
        assertEquals(MediaType.BOARDGAME, item.type)
        assertNull(item.imageUrl)
        assertNull(item.description)
        assertEquals("BGG", item.source)
    }

    @Test
    fun `MediaItem id format for BGG`() {
        val item = MediaItem(
            id = "bgg:174430",
            title = "Gloomhaven",
            type = MediaType.BOARDGAME,
            imageUrl = null,
            description = null,
            source = "BGG"
        )

        // Verify ID format
        assertEquals("bgg:174430", item.id)

        // Can extract numeric ID
        val numericId = item.id.removePrefix("bgg:").toLongOrNull()
        assertEquals(174430L, numericId)
    }

    @Test
    fun `MediaItem with different board game titles`() {
        val catan = MediaItem("bgg:13", "Catan", MediaType.BOARDGAME, null, null, "BGG")
        val gloomhaven = MediaItem("bgg:174430", "Gloomhaven", MediaType.BOARDGAME, null, null, "BGG")
        val pandemic = MediaItem("bgg:30549", "Pandemic", MediaType.BOARDGAME, null, null, "BGG")

        assertEquals("Catan", catan.title)
        assertEquals("Gloomhaven", gloomhaven.title)
        assertEquals("Pandemic", pandemic.title)
    }

    @Test
    fun `SearchResult pagination calculations`() {
        val result = SearchResult(
            total = 150,
            limit = 20,
            offset = 40,
            items = emptyList()
        )

        // Verify pagination values
        assertEquals(150, result.total)
        assertEquals(20, result.limit)
        assertEquals(40, result.offset)

        // Can calculate current page: offset / limit = 40 / 20 = page 2 (0-indexed)
        val currentPage = result.offset / result.limit
        assertEquals(2, currentPage)

        // Can calculate total pages: ceil(total / limit) = ceil(150 / 20) = 8
        val totalPages = (result.total + result.limit - 1) / result.limit
        assertEquals(8, totalPages)

        // Can calculate if there's a next page
        val hasNextPage = (result.offset + result.limit) < result.total
        assertEquals(true, hasNextPage)
    }
}