package integration.bgg

import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class BggMapperTest {

    @Test
    fun `parseSearchIds returns correct total and IDs`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="3" termsofuse="...">
                <item type="boardgame" id="174430">
                    <name type="primary" value="Gloomhaven"/>
                </item>
                <item type="boardgame" id="13">
                    <name type="primary" value="Catan"/>
                </item>
                <item type="boardgame" id="822">
                    <name type="primary" value="Carcassonne"/>
                </item>
            </items>
        """.trimIndent()

        val (total, ids) = BggMapper.parseSearchIds(xml)

        assertEquals(3, total)
        assertEquals(listOf(174430L, 13L, 822L), ids)
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
    fun `parseSearchIds ignores items without valid IDs`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="2" termsofuse="...">
                <item type="boardgame" id="123">
                    <name type="primary" value="Valid Game"/>
                </item>
                <item type="boardgame" id="invalid">
                    <name type="primary" value="Invalid ID"/>
                </item>
            </items>
        """.trimIndent()

        val (total, ids) = BggMapper.parseSearchIds(xml)

        assertEquals(2, total)
        assertEquals(listOf(123L), ids)
    }

    @Test
    fun `mapHotResponse maps all values correctly`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="174430" rank="1">
                    <thumbnail value="https://cf.geekdo-images.com/gloomhaven-thumb.jpg"/>
                    <name value="Gloomhaven"/>
                </item>
                <item id="13" rank="2">
                    <thumbnail value="https://cf.geekdo-images.com/catan-thumb.jpg"/>
                    <name value="Catan"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertEquals(2, items.size)

        assertEquals("bgg:174430", items[0].id)
        assertEquals("Gloomhaven", items[0].title)
        assertEquals(MediaType.BOARDGAMES, items[0].type)
        assertEquals("https://cf.geekdo-images.com/gloomhaven-thumb.jpg", items[0].imageUrl)
        assertEquals("BGG", items[0].source)
        assertNull(items[0].description)

        assertEquals("bgg:13", items[1].id)
        assertEquals("Catan", items[1].title)
    }

    @Test
    fun `mapHotResponse handles missing thumbnail`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="123" rank="1">
                    <name value="No Thumbnail Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertEquals(1, items.size)
        assertEquals("bgg:123", items[0].id)
        assertEquals("No Thumbnail Game", items[0].title)
        assertNull(items[0].imageUrl)
    }

    @Test
    fun `mapHotResponse handles missing name`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="999" rank="1">
                    <thumbnail value="https://example.com/thumb.jpg"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertEquals(1, items.size)
        assertEquals("Unknown", items[0].title)
    }

    @Test
    fun `mapHotResponse handles blank thumbnail value`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item id="123" rank="1">
                    <thumbnail value="   "/>
                    <name value="Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapHotResponse(xml)

        assertNull(items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse maps all values`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="174430">
                    <thumbnail>https://cf.geekdo-images.com/gloom-thumb.jpg</thumbnail>
                    <image>https://cf.geekdo-images.com/gloomhaven-full.jpg</image>
                    <name type="primary" value="Gloomhaven"/>
                    <name type="alternate" value="Gloomhaven: Alternate"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals(1, items.size)
        assertEquals("bgg:174430", items[0].id)
        assertEquals("Gloomhaven", items[0].title)
        assertEquals("https://cf.geekdo-images.com/gloomhaven-full.jpg", items[0].imageUrl)
        assertEquals(MediaType.BOARDGAMES, items[0].type)
        assertEquals("BGG", items[0].source)
    }

    @Test
    fun `mapThingListResponse prefers image over thumbnail`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="13">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <image>https://example.com/full.jpg</image>
                    <name type="primary" value="Catan"/>
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
                <item type="boardgame" id="13">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <name type="primary" value="Catan"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals("https://example.com/thumb.jpg", items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse handles no images at all`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="13">
                    <name type="primary" value="Catan"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertNull(items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse handles missing primary name`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="999">
                    <name type="alternate" value="Alternate Name"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals(1, items.size)
        assertEquals("Unknown", items[0].title)
    }

    @Test
    fun `mapThingListResponse handles blank image text`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="123">
                    <image>   </image>
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <name type="primary" value="Game"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals("https://example.com/thumb.jpg", items[0].imageUrl)
    }

    @Test
    fun `mapThingListResponse handles multiple items`() {
        val xml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="1">
                    <image>url1</image>
                    <name type="primary" value="Game 1"/>
                </item>
                <item type="boardgame" id="2">
                    <image>url2</image>
                    <name type="primary" value="Game 2"/>
                </item>
                <item type="boardgame" id="3">
                    <image>url3</image>
                    <name type="primary" value="Game 3"/>
                </item>
            </items>
        """.trimIndent()

        val items = BggMapper.mapThingListResponse(xml)

        assertEquals(3, items.size)
        assertEquals("bgg:1", items[0].id)
        assertEquals("bgg:2", items[1].id)
        assertEquals("bgg:3", items[2].id)
    }

    @Test
    fun `parseXml throws exception on invalid XML`() {
        val invalidXml = "This is not XML"

        val exception = assertFailsWith<IllegalStateException> {
            BggMapper.parseSearchIds(invalidXml)
        }

        assertTrue(exception.message!!.contains("Failed to parse BGG XML response"))
    }

    @Test
    fun `parseXml handles BOM character`() {
        val xmlWithBom = "\uFEFF<?xml version=\"1.0\"?><items total=\"1\"><item id=\"123\"/></items>"

        val (total, ids) = BggMapper.parseSearchIds(xmlWithBom)

        assertEquals(1, total)
        assertEquals(listOf(123L), ids)
    }

    @Test
    fun `parseXml handles extra whitespace`() {
        val xmlWithWhitespace = "  \n\n  <?xml version=\"1.0\"?><items total=\"1\"><item id=\"123\"/></items>  "

        val (total, ids) = BggMapper.parseSearchIds(xmlWithWhitespace)

        assertEquals(1, total)
        assertEquals(listOf(123L), ids)
    }
}