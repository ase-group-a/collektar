package integration.igdb

import com.collektar.integration.igdb.IGDBClientImpl
import com.collektar.integration.igdb.IGDBConfig
import com.collektar.integration.shared.OauthTokenProvider
import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

class IGDBClientImplTest {
    private var mockConfig: IGDBConfig? = null
    private var mockTokenProvider: OauthTokenProvider? = null

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) { json() }
        }
    }

    @BeforeTest
    fun init() {
        mockConfig = mockk<IGDBConfig>().apply {
            every { clientId } returns CLIENT_ID
            every { clientSecret } returns CLIENT_SECRET
            every { baseUrl } returns BASE_URL
            every { tokenUrl } returns TOKEN_URL
        }

        mockTokenProvider = mockk<OauthTokenProvider>().apply {
            coEvery { getToken() } returns TOKEN
        }
    }

    @Test
    fun `Valid search should return correct IGDBGamesResponse`() {
        val httpClient = mockHttpClient { request ->
            assertEquals(
                GAMES_URL, request.url.toString()
            )
            assertEquals(
                ContentType.Text.Plain.toString(),
                request.body.contentType.toString().split(";").first() // ignore charset that might be set
            )
            assertEquals(
                AUTH_HEADER_VALUE, request.headers["Authorization"]
            )
            assertEquals(
                CLIENT_ID, request.headers["Client-ID"]
            )
            assertTrue { request.body is TextContent && (request.body as TextContent).text.isNotBlank() }

            respond(
                content = EXAMPLE_IGDB_RESPONSE_JSON, status = HttpStatusCode.OK, headers = headersOf(
                    "X-Count" to listOf("$EXAMPLE_IGDB_RESPONSE_X_COUNT"),
                    HttpHeaders.ContentType to listOf(ContentType.Application.Json.toString())
                )
            )
        }

        val client = IGDBClientImpl(httpClient, mockConfig!!, mockTokenProvider!!)

        val res = assertDoesNotThrow { runBlocking { client.searchGames(QUERY, LIMIT, OFFSET) } }

        assertEquals(EXAMPLE_IGDB_RESPONSE_COUNT, res.items.size)
        assertEquals(EXAMPLE_IGDB_RESPONSE_X_COUNT, res.total)

        coVerify { mockTokenProvider!!.getToken() }
    }

    @Test
    fun `Invalid API limits return IllegalArgumentException`() {
        val httpClient = mockk<HttpClient>()

        val client = IGDBClientImpl(httpClient, mockConfig!!, mockTokenProvider!!)

        assertFailsWith<IllegalArgumentException> { runBlocking { client.searchGames(QUERY, -1, OFFSET) } }
        assertFailsWith<IllegalArgumentException> { runBlocking { client.searchGames(QUERY, 501, OFFSET) } }
    }

    @Test
    fun `Verify otional search query`() {
        val httpClient = mockHttpClient { request ->
            assertEquals(
                GAMES_URL, request.url.toString()
            )
            assertEquals(
                ContentType.Text.Plain.toString(),
                request.body.contentType.toString().split(";").first() // ignore charset that might be set
            )
            assertNotEquals(true, (request.body as? TextContent)?.text?.contains("search"))

            respond(
                // Respond with error to skip further processing
                content = "", status = HttpStatusCode.InternalServerError
            )
        }

        val client = IGDBClientImpl(httpClient, mockConfig!!, mockTokenProvider!!)
        assertFailsWith<RuntimeException> { runBlocking { client.searchGames(null, LIMIT, OFFSET) } }
    }

    @Test
    fun `Exceeded rate limit throws RateLimitException`() {
        val httpClient = mockHttpClient { _ ->
            respond(
                content = "", status = HttpStatusCode.TooManyRequests
            )
        }

        val client = IGDBClientImpl(httpClient, mockConfig!!, mockTokenProvider!!)
        assertFailsWith<RateLimitException> { runBlocking { client.searchGames(QUERY, LIMIT, OFFSET) } }
    }

    @Test
    fun `Failed request return RuntimeException`() {
        val httpClient = mockHttpClient { _ ->
            respond(
                content = "", status = HttpStatusCode.InternalServerError
            )
        }

        val client = IGDBClientImpl(httpClient, mockConfig!!, mockTokenProvider!!)
        assertFailsWith<RuntimeException> { runBlocking { client.searchGames(QUERY, LIMIT, OFFSET) } }
    }
}