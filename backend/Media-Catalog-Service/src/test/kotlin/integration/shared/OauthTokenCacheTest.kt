package integration.shared

import com.collektar.integration.shared.OauthTokenCache
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

const val TOKEN_STRING = "OAUTH-TOKEN-STRING"
const val TOKEN_EXPIRATION = 3600L
const val TOKEN_INSTANT_EXPIRATION = 0L

class OauthTokenCacheTest {
    private var oauthTokenCache: OauthTokenCache? = null

    @BeforeTest
    fun init() {
        oauthTokenCache = OauthTokenCache()
    }
    
    @Test
    fun `getIfValid without putting a token should return null`() {
        assertNull(oauthTokenCache?.getIfValid())
    }

    @Test
    fun `getIfValid should return token`() {
        oauthTokenCache?.put(TOKEN_STRING, TOKEN_EXPIRATION)
        
        assertNotNull(oauthTokenCache?.getIfValid())
        assertEquals(TOKEN_STRING, oauthTokenCache?.getIfValid())
    }

    @Test
    fun `getIfValid should return null when expired`() {
        oauthTokenCache?.put(TOKEN_STRING, TOKEN_INSTANT_EXPIRATION)

        assertNull(oauthTokenCache?.getIfValid())
    }

    @Test
    fun `getIfValid should return null after clear`() {
        oauthTokenCache?.put(TOKEN_STRING, TOKEN_EXPIRATION)

        assertNotNull(oauthTokenCache?.getIfValid())
        oauthTokenCache?.clear()
        assertNull(oauthTokenCache?.getIfValid())
    }
}