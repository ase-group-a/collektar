package integration.bgg

import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BggMapperTest {

    @Test
    fun `parseSearchIds returns correct total and IDs`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="3" termsofuse="...">
                <item type="boardgame" id="123">
                    <name type="primary" value="Game 1"/>
                </item>
                <item type="boardgame" id="456">
                    <name type="primary" value="Game 2"/>
                </item>
                <item type="boardgame" id="789">
                    <name type="primary" value="Game 3"/>
                </item>
            </items>
        """.trimIndent()

        val (total, ids) = BggMapper.parseSearchIds(xml)

        assertEquals(3, total)
        assertEquals(listOf(123L, 456L, 789L), ids)
    }

    @Test
    fun `parseSearchIds handles empty results`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="0" termsofuse="...">
            </items>
        """.trimIndent()

        val (total, ids) = BggMapper.parseSearchIds(xml)

        assertEquals(0, total)
        assertEquals(emptyList(), ids)
    }

    @Test
    fun `mapHotResponse maps all values correctly`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="123" rank="1">
                    <thumbnail value="https://example.com/thumb.jpg"/>
                    <name value="Test Game"/>
                </item>
                <item id="456" rank="2">
                    <thumbnail value="https://example.com/thumb2.jpg"/>
                    <name value="Another Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertEquals(2, items.size)

        assertEquals("bgg:123", items[0].id)
        assertEquals("Test Game", items[0].title)
        assertEquals(MediaType.BOARDGAME, items[0].type)
        assertEquals("https://example.com/thumb.jpg", items[0].imageUrl)
        assertEquals("BGG", items[0].source)

        assertEquals("bgg:456", items[1].id)
        assertEquals("Another Game", items[1].title)
    }

    @Test
    fun `mapHotResponse handles missing thumbnail`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="123" rank="1">
                    <name value="No Thumb Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertEquals(1, items.size)
        assertEquals("bgg:123", items[0].id)
        assertEquals("No Thumb Game", items[0].title)
        assertNull(items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse maps all values`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="789">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <image>https://example.com/image.jpg</image>
                    <name type="primary" value="Primary Name"/>
                    <name type="alternate" value="Alt Name"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals(1, items.size)
        assertEquals("bgg:789", items[0].id)
        assertEquals("Primary Name", items[0].title)
        assertEquals("https://example.com/image.jpg", items[0].imageUrl)
        assertEquals(MediaType.BOARDGAME, items[0].type)
    }

    @Test
    fun `mapThingListResponse prefers image over thumbnail`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="100">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <image>https://example.com/full.jpg</image>
                    <name type="primary" value="Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals("https://example.com/full.jpg", items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse falls back to thumbnail when no image`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="100">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <name type="primary" value="Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals("https://example.com/thumb.jpg", items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse handles missing name`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="999">
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals(1, items.size)
        assertEquals("Unknown", items[0].title)
    }
}