package integration.shared

import com.collektar.di.modules.OauthParameterType
import com.collektar.integration.shared.OauthConfig
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Values used for testing
const val OAUTH_CLIENT_ID = "clientId"
const val OAUTH_CLIENT_SECRET = "clientSecret"
const val OAUTH_ACCESS_TOKEN = "OAUTH-ACCESS-TOKEN"
const val OAUTH_EXPIRES_IN = 12345L
const val OAUTH_TOKEN_TYPE = "bearer"
const val OAUTH_TOKEN_URL = "https://api.example.com/token"
const val OAUTH_RESPONSE = """
{
    "access_token": "$OAUTH_ACCESS_TOKEN",
    "expires_in": $OAUTH_EXPIRES_IN,
    "token_type": "$OAUTH_TOKEN_TYPE"
}
"""

class OauthTokenProviderTest {
    private var mockTokenCache : OauthTokenCache? = null
    private var mockConfig : OauthConfig? = null

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) }) {
            install(ContentNegotiation) { json() }
        }
    }
    
    @BeforeTest
    fun init() {
        mockTokenCache = mockk<OauthTokenCache>().apply {
            every { getIfValid() } returns null
            every { put(any(), any()) } returns Unit
        }
        
        mockConfig = mockk<OauthConfig>().apply { 
            every { clientId } returns OAUTH_CLIENT_ID
            every { clientSecret } returns OAUTH_CLIENT_SECRET
            every { tokenUrl } returns OAUTH_TOKEN_URL
        }
    }
    
    @Test
    fun `getToken returns saved token from OauthTokenCache`() {
        every { mockTokenCache?.getIfValid() } returns OAUTH_ACCESS_TOKEN
        
        val mockHttpClient = mockk<HttpClient>()
        
        val oauthTokenProvider = OauthTokenProvider(
            mockHttpClient,
            mockTokenCache!!,
            mockConfig!!,
            OauthParameterType.BODY_URLENCODED
        )
        
        val token = runBlocking { oauthTokenProvider.getToken() }

        assertEquals(OAUTH_ACCESS_TOKEN, token)
    }
    
    @Test
    fun `getToken correctly fetches and returns a token with BODY_URLENCODED`() {
        val httpClient = mockHttpClient { request ->
            assertEquals(
                OAUTH_TOKEN_URL,
                request.url.toString()
            )
            assertEquals(
                ContentType.Application.FormUrlEncoded,
                request.body.contentType
            )
            
            respond(
                content = OAUTH_RESPONSE,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        
        val oauthTokenProvider = OauthTokenProvider(
            httpClient,
            mockTokenCache!!,
            mockConfig!!,
            OauthParameterType.BODY_URLENCODED
        )
        
        val token = runBlocking { oauthTokenProvider.getToken() }
        assertEquals(OAUTH_ACCESS_TOKEN, token)

        verify { mockTokenCache!!.put(OAUTH_ACCESS_TOKEN, OAUTH_EXPIRES_IN) }
    }

    @Test
    fun `getToken correctly fetches and returns a token with URL_PARAMETER_URLENCODED`() {
        val httpClient = mockHttpClient { request ->
            assertEquals(
                OAUTH_TOKEN_URL,
                request.url.toString().split("?").first()
            )

            respond(
                content = OAUTH_RESPONSE,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val oauthTokenProvider = OauthTokenProvider(
            httpClient,
            mockTokenCache!!,
            mockConfig!!,
            OauthParameterType.URL_PARAMETER_URLENCODED
        )

        val token = runBlocking { oauthTokenProvider.getToken() }
        assertEquals(OAUTH_ACCESS_TOKEN, token)

        verify { mockTokenCache!!.put(OAUTH_ACCESS_TOKEN, OAUTH_EXPIRES_IN) }
    }
    
    @Test
    fun `RuntimeException thrown if API did not return an OK status`() {
        val httpClient = mockHttpClient { _ ->
            respond(
                content = "",
                status = HttpStatusCode.InternalServerError
            )
        }
        
        val oauthTokenProvider = OauthTokenProvider(
            httpClient,
            mockTokenCache!!,
            mockConfig!!,
            OauthParameterType.BODY_URLENCODED
        )
        
        assertFailsWith<RuntimeException> { runBlocking { oauthTokenProvider.getToken() } }
    }
}