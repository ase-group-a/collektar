package integration.shared

import com.collektar.integration.shared.OauthTokenResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class OauthModelsTest {

    private val json = Json { encodeDefaults = true }
    
    @Test
    fun `OauthTokenResponse serializes and deserializes correctly`() {
        val token = OauthTokenResponse("test", "Bearer", 3600)
        val str = json.encodeToString(token)
        val decoded = json.decodeFromString<OauthTokenResponse>(str)

        assertEquals(token.accessToken, decoded.accessToken)
        assertEquals(token.tokenType, decoded.tokenType)
        assertEquals(token.expiresIn, decoded.expiresIn)
    }
}