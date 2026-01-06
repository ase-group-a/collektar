package integration.bgg

import com.collektar.imagecache.ImageCacheClient
import domain.MediaType
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class BggClientImplTest {

    private val configWithToken = BggConfig(
        baseUrl = "https://boardgamegeek.com/xmlapi2",
        token = "test-token-123",
        minDelayMillis = 100L
    )

    private val configWithoutToken = BggConfig(
        baseUrl = "https://boardgamegeek.com/xmlapi2",
        token = null,
        minDelayMillis = 100L
    )

    private val mockImageCacheClient = mockk<ImageCacheClient>(relaxed = true)

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) })
    }

    @Test
    fun `hotBoardGames returns valid response`() = runBlocking {
        val mockXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item id="174430" rank="1">
                    <thumbnail value="https://cf.geekdo-images.com/thumb.jpg"/>
                    <name value="Gloomhaven"/>
                </item>
                <item id="13" rank="2">
                    <thumbnail value="https://cf.geekdo-images.com/catan.jpg"/>
                    <name value="Catan"/>
                </item>
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            assertEquals("/xmlapi2/hot", request.url.encodedPath)
            assertEquals("boardgame", request.url.parameters["type"])
            assertNotNull(request.headers[HttpHeaders.UserAgent])
            assertEquals("application/xml", request.headers[HttpHeaders.Accept])

            respond(
                content = mockXml,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType to listOf(ContentType.Application.Xml.toString()))
            )
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.hotBoardGames(limit = 10, offset = 0)

        assertEquals(2, result.total)
        assertEquals(10, result.limit)
        assertEquals(0, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("bgg:174430", result.items[0].id)
        assertEquals("Gloomhaven", result.items[0].title)
        assertEquals(MediaType.BOARDGAME, result.items[0].type)
    }

    @Test
    fun `hotBoardGames sends Authorization header when token is provided`() {
        runBlocking {
            val mockXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <items termsofuse="...">
                    <item id="123" rank="1">
                        <thumbnail value="url"/>
                        <name value="Game"/>
                    </item>
                </items>
            """.trimIndent()

            val httpClient = mockHttpClient { request ->
                assertEquals("Bearer test-token-123", request.headers[HttpHeaders.Authorization])
                respond(mockXml, HttpStatusCode.OK)
            }

            val client = BggClientImpl(httpClient, configWithToken, mockImageCacheClient)
            client.hotBoardGames(10, 0)
        }
    }

    @Test
    fun `hotBoardGames does not send Authorization header when token is null`() {
        runBlocking {
            val mockXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <items termsofuse="...">
                    <item id="123" rank="1">
                        <thumbnail value="url"/>
                        <name value="Game"/>
                    </item>
                </items>
            """.trimIndent()

            val httpClient = mockHttpClient { request ->
                assertNull(request.headers[HttpHeaders.Authorization])
                respond(mockXml, HttpStatusCode.OK)
            }

            val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
            client.hotBoardGames(10, 0)
        }
    }

    @Test
    fun `hotBoardGames with pagination`() = runBlocking {
        val mockXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item id="1" rank="1"><thumbnail value="url1"/><name value="Game 1"/></item>
                <item id="2" rank="2"><thumbnail value="url2"/><name value="Game 2"/></item>
                <item id="3" rank="3"><thumbnail value="url3"/><name value="Game 3"/></item>
                <item id="4" rank="4"><thumbnail value="url4"/><name value="Game 4"/></item>
                <item id="5" rank="5"><thumbnail value="url5"/><name value="Game 5"/></item>
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { respond(mockXml, HttpStatusCode.OK) }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.hotBoardGames(limit = 2, offset = 1)

        assertEquals(5, result.total)
        assertEquals(2, result.limit)
        assertEquals(1, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("bgg:2", result.items[0].id)
        assertEquals("bgg:3", result.items[1].id)
    }

    @Test
    fun `searchBoardGames returns valid response`() = runBlocking {
        val searchXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="2" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="13">
                    <name type="primary" value="Catan"/>
                </item>
                <item type="boardgame" id="822">
                    <name type="primary" value="Carcassonne"/>
                </item>
            </items>
        """.trimIndent()

        val thingXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
                <item type="boardgame" id="13">
                    <thumbnail>https://example.com/catan-thumb.jpg</thumbnail>
                    <image>https://example.com/catan.jpg</image>
                    <name type="primary" value="Catan"/>
                </item>
                <item type="boardgame" id="822">
                    <thumbnail>https://example.com/carc-thumb.jpg</thumbnail>
                    <image>https://example.com/carcassonne.jpg</image>
                    <name type="primary" value="Carcassonne"/>
                </item>
            </items>
        """.trimIndent()

        var callCount = 0
        val httpClient = mockHttpClient { request ->
            callCount++
            when (request.url.encodedPath) {
                "/xmlapi2/search" -> {
                    assertEquals("catan", request.url.parameters["query"])
                    assertEquals("boardgame", request.url.parameters["type"])
                    respond(searchXml, HttpStatusCode.OK)
                }
                "/xmlapi2/thing" -> {
                    assertEquals("13,822", request.url.parameters["id"])
                    assertEquals("1", request.url.parameters["stats"])
                    respond(thingXml, HttpStatusCode.OK)
                }
                else -> error("Unexpected path: ${request.url.encodedPath}")
            }
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.searchBoardGames("catan", limit = 10, offset = 0)

        assertEquals(2, result.total)
        assertEquals(10, result.limit)
        assertEquals(0, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("bgg:13", result.items[0].id)
        assertEquals("Catan", result.items[0].title)
        assertEquals(2, callCount)
    }

    @Test
    fun `searchBoardGames sends Authorization header when token is provided`() {
        runBlocking {
            val searchXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <items total="1" termsofuse="...">
                    <item type="boardgame" id="13">
                        <name type="primary" value="Catan"/>
                    </item>
                </items>
            """.trimIndent()

            val thingXml = """
                <?xml version="1.0" encoding="utf-8"?>
                <items termsofuse="...">
                    <item type="boardgame" id="13">
                        <image>url</image>
                        <name type="primary" value="Catan"/>
                    </item>
                </items>
            """.trimIndent()

            val httpClient = mockHttpClient { request ->
                assertEquals("Bearer test-token-123", request.headers[HttpHeaders.Authorization])

                when (request.url.encodedPath) {
                    "/xmlapi2/search" -> respond(searchXml, HttpStatusCode.OK)
                    "/xmlapi2/thing" -> respond(thingXml, HttpStatusCode.OK)
                    else -> error("Unexpected path")
                }
            }

            val client = BggClientImpl(httpClient, configWithToken, mockImageCacheClient)
            client.searchBoardGames("catan", 10, 0)
        }
    }

    @Test
    fun `searchBoardGames with empty results`() = runBlocking {
        val emptyXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="0" termsofuse="https://boardgamegeek.com/xmlapi/termsofuse">
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { respond(emptyXml, HttpStatusCode.OK) }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.searchBoardGames("nonexistentgame12345", limit = 20, offset = 0)

        assertEquals(0, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(0, result.items.size)
    }

    @Test
    fun `searchBoardGames with pagination`() = runBlocking {
        val searchXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items total="100" termsofuse="...">
                <item type="boardgame" id="1"><name type="primary" value="Game 1"/></item>
                <item type="boardgame" id="2"><name type="primary" value="Game 2"/></item>
                <item type="boardgame" id="3"><name type="primary" value="Game 3"/></item>
            </items>
        """.trimIndent()

        val thingXml = """
            <?xml version="1.0" encoding="utf-8"?>
            <items termsofuse="...">
                <item type="boardgame" id="2">
                    <image>url2</image>
                    <name type="primary" value="Game 2"/>
                </item>
            </items>
        """.trimIndent()

        val httpClient = mockHttpClient { request ->
            when (request.url.encodedPath) {
                "/xmlapi2/search" -> respond(searchXml, HttpStatusCode.OK)
                "/xmlapi2/thing" -> {
                    assertEquals("2", request.url.parameters["id"])
                    respond(thingXml, HttpStatusCode.OK)
                }
                else -> error("Unexpected")
            }
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.searchBoardGames("game", limit = 1, offset = 1)

        // Fixed: After pagination (limit=1, offset=1), we get items[1] which is id=2
        assertEquals(100, result.total)  // Total from XML attribute
        assertEquals(1, result.limit)
        assertEquals(1, result.offset)
        assertEquals(1, result.items.size)  // Only 1 item after applying limit=1
        assertEquals("bgg:2", result.items[0].id)
    }

    @Test
    fun `getBoardGames batches requests over 20 items`() = runBlocking {
        val ids = (1L..25L).toList()

        var batchCount = 0
        val httpClient = mockHttpClient { request ->
            batchCount++
            val requestedIds = request.url.parameters["id"]?.split(",") ?: emptyList()

            when (batchCount) {
                1 -> assertEquals(20, requestedIds.size)
                2 -> assertEquals(5, requestedIds.size)
            }

            respond(
                """<?xml version="1.0"?><items termsofuse="..."></items>""",
                HttpStatusCode.OK
            )
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        client.getBoardGames(ids)

        assertEquals(2, batchCount)
    }

    @Test
    fun `getBoardGames with empty list returns empty`() = runBlocking {
        val httpClient = mockHttpClient { error("Should not be called") }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        val result = client.getBoardGames(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `retries on empty response`() = runBlocking {
        var attemptCount = 0
        val httpClient = mockHttpClient {
            attemptCount++
            if (attemptCount < 3) {
                respond("", HttpStatusCode.OK)
            } else {
                respond(
                    """<?xml version="1.0"?><items termsofuse="..."></items>""",
                    HttpStatusCode.OK
                )
            }
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        client.hotBoardGames(10, 0)

        assertEquals(3, attemptCount)
    }

    @Test
    fun `throws exception after max retries on empty response`() {
        runBlocking {
            val httpClient = mockHttpClient { respond("", HttpStatusCode.OK) }

            val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)

            val exception = assertFailsWith<IllegalStateException> {
                client.hotBoardGames(10, 0)
            }

            assertTrue(exception.message?.contains("after 3 attempts") == true)
        }
    }

    @Test
    fun `handles non-XML response with retry`() {
        runBlocking {
            val httpClient = mockHttpClient { respond("Not XML content", HttpStatusCode.OK) }

            val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)

            val exception = assertFailsWith<IllegalStateException> {
                client.hotBoardGames(10, 0)
            }

            assertTrue(exception.message?.contains("non-XML response") == true)
        }
    }

    @Test
    fun `handles 429 rate limit with extended retry`() = runBlocking {
        var attemptCount = 0
        val httpClient = mockHttpClient {
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

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        client.hotBoardGames(10, 0)

        assertEquals(2, attemptCount)
    }

    @Test
    fun `handles 500 server error with retry`() = runBlocking {
        var attemptCount = 0
        val httpClient = mockHttpClient {
            attemptCount++
            if (attemptCount < 2) {
                respond("Server Error", HttpStatusCode.InternalServerError)
            } else {
                respond(
                    """<?xml version="1.0"?><items termsofuse="..."></items>""",
                    HttpStatusCode.OK
                )
            }
        }

        val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)
        client.hotBoardGames(10, 0)

        assertEquals(2, attemptCount)
    }

    @Test
    fun `throws after max retries on server error`() {
        runBlocking {
            val httpClient = mockHttpClient {
                respond("Server Error", HttpStatusCode.InternalServerError)
            }

            val client = BggClientImpl(httpClient, configWithoutToken, mockImageCacheClient)

            val exception = assertFailsWith<IllegalStateException> {
                client.hotBoardGames(10, 0)
            }

            assertTrue(
                exception.message?.contains("500") == true ||
                        exception.message?.contains("Internal Server Error") == true
            )
        }
    }

    @Test
    fun `handles 401 Unauthorized with invalid token`() {
        runBlocking {
            val httpClient = mockHttpClient { request ->
                respond("Unauthorized", HttpStatusCode.Unauthorized)
            }

            val client = BggClientImpl(httpClient, configWithToken, mockImageCacheClient)

            val exception = assertFailsWith<IllegalStateException> {
                client.hotBoardGames(10, 0)
            }

            assertTrue(
                exception.message?.contains("401") == true ||
                        exception.message?.contains("Unauthorized") == true
            )
        }
    }
}