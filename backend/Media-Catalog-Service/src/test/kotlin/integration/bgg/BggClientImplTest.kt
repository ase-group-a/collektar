package integration.bgg

import domain.MediaItem
import domain.MediaType
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class BggClientImplTest {

    private val config = BggConfig(
        baseUrl = "https://boardgamegeek.com/xmlapi2",
        token = null,
        minDelayMillis = 100L // Short delay for tests
    )

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) })
    }

    @Test
    fun `hotBoardGames returns valid response`() = runBlocking {
        val mockXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item id="123" rank="1">
                    <thumbnail value="https://example.com/thumb.jpg"/>
                    <name value="Test Game"/>
                </item>
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            assertEquals("/hot", request.url.encodedPath)
            assertEquals("boardgame", request.url.parameters["type"])
            assertNotNull(request.headers[HttpHeaders.UserAgent])
            assertNull(request.headers[HttpHeaders.Authorization]) // Should not have auth

            respond(
                content = mockXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Xml.toString()))
            )
        }

        val client = BggClientImpl(httpClient, config)
        val result = client.hotBoardGames(limit = 10, offset = 0)

        assertEquals(1, result.total)
        assertEquals(1, result.items.size)
        assertEquals("bgg:123", result.items[0].id)
        assertEquals("Test Game", result.items[0].title)
        assertEquals(MediaType.BOARDGAME, result.items[0].type)
    }

    @Test
    fun `searchBoardGames returns valid response`() = runBlocking {
        val searchXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="1" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="456">
                    <name type="primary" value="Catan"/>
                    <yearpublished value="1995"/>
                </item>
            </items>
        """.trimIndent()

        val thingXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="456">
                    <thumbnail>https://example.com/thumb.jpg</thumbnail>
                    <image>https://example.com/image.jpg</image>
                    <name type="primary" value="Catan"/>
                </item>
            </items>
        """.trimIndent()

        var callCount = 0
        val httpClient = mockHttpClient { request ->
            callCount++
            when (request.url.encodedPath) {
                "/search" -> {
                    assertEquals("catan", request.url.parameters["query"])
                    assertEquals("boardgame", request.url.parameters["type"])
                    respond(searchXml, HttpStatusCode.OK)
                }
                "/thing" -> {
                    assertEquals("456", request.url.parameters["id"])
                    assertEquals("1", request.url.parameters["stats"])
                    respond(thingXml, HttpStatusCode.OK)
                }
                else -> error("Unexpected path: ${request.url.encodedPath}")
            }
        }

        val client = BggClientImpl(httpClient, config)
        val result = client.searchBoardGames("catan", limit = 10, offset = 0)

        assertEquals(1, result.total)
        assertEquals(1, result.items.size)
        assertEquals("bgg:456", result.items[0].id)
        assertEquals("Catan", result.items[0].title)
        assertEquals("https://example.com/image.jpg", result.items[0].imageUrl)
        assertEquals(2, callCount) // Should make 2 calls: search + thing
    }

    @Test
    fun `searchBoardGames with empty results`() = runBlocking {
        val emptyXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="0" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            respond(emptyXml, HttpStatusCode.OK)
        }

        val client = BggClientImpl(httpClient, config)
        val result = client.searchBoardGames("nonexistent", limit = 10, offset = 0)

        assertEquals(0, result.total)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `getBoardGames batches requests over 20 items`() = runBlocking {
        val ids = (1L..25L).toList() // 25 items should trigger 2 batches

        var batchCount = 0
        val httpClient = mockHttpClient { request ->
            batchCount++
            val requestedIds = request.url.parameters["id"]?.split(",")?.size ?: 0

            // First batch should have 20, second should have 5
            when (batchCount) {
                1 -> assertEquals(20, requestedIds)
                2 -> assertEquals(5, requestedIds)
            }

            respond(
                """<?xml version="1.0"?><items termsofuse="..."></items>""",
                HttpStatusCode.OK
            )
        }

        val client = BggClientImpl(httpClient, config)
        client.getBoardGames(ids)

        assertEquals(2, batchCount)
    }

    @Test
    fun `retries on empty response`() = runBlocking {
        var attemptCount = 0
        val httpClient = mockHttpClient { request ->
            attemptCount++
            if (attemptCount < 3) {
                // Return empty on first 2 attempts
                respond("", HttpStatusCode.OK)
            } else {
                // Return valid XML on 3rd attempt
                respond(
                    """<?xml version="1.0"?><items termsofuse="..."></items>""",
                    HttpStatusCode.OK
                )
            }
        }

        val client = BggClientImpl(httpClient, config)
        client.hotBoardGames(10, 0)

        assertEquals(3, attemptCount)
    }

    @Test
    fun `throws exception after max retries on empty response`() = runBlocking {
        val httpClient = mockHttpClient { request ->
            respond("", HttpStatusCode.OK) // Always return empty
        }

        val client = BggClientImpl(httpClient, config)

        val exception = assertFailsWith<IllegalStateException> {
            client.hotBoardGames(10, 0)
        }

        assertTrue(exception.message!!.contains("after 3 attempts"))
    }

    @Test
    fun `handles non-XML response with retry`() = runBlocking {
        val httpClient = mockHttpClient { request ->
            respond("Not XML content", HttpStatusCode.OK)
        }

        val client = BggClientImpl(httpClient, config)

        val exception = assertFailsWith<IllegalStateException> {
            client.hotBoardGames(10, 0)
        }

        assertTrue(exception.message!!.contains("non-XML response"))
    }

    @Test
    fun `handles 429 rate limit with extended retry`() = runBlocking {
        var attemptCount = 0
        val httpClient = mockHttpClient { request ->
            attemptCount++
            if (attemptCount < 2) {
                respond("Rate limited", HttpStatusCode.TooManyRequests)
            } else {
                respond(
                    """<?xml version="1.0"?><items termsofuse="..."></items>""",
                    HttpStatusCode.OK
                )
            }
        }

        val client = BggClientImpl(httpClient, config)
        client.hotBoardGames(10, 0)

        assertEquals(2, attemptCount)
    }
}