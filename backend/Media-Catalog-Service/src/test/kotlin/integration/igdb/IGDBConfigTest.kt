package integration.igdb

import com.collektar.integration.igdb.IGDBConfig
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class IGDBConfigTest {
    
    companion object {
        const val IGDB_CLIENT_ID = "igdb_client_id"
        const val IGDB_CLIENT_SECRET = "igdb_client_secret"
        const val IGDB_BASE_URL = "https://api.igdb.com/v4"
        const val IGDB_TOKEN_URL = "https://id.twitch.tv/oauth2/token"
    }
    
    @Test
    fun `fromEnv reads values correctly from ApplicationEnvironment`() {
        val appConfig = MapApplicationConfig(
            "igdb.clientId" to IGDB_CLIENT_ID,
            "igdb.clientSecret" to IGDB_CLIENT_SECRET,
            "igdb.baseUrl" to IGDB_BASE_URL,
            "igdb.tokenUrl" to IGDB_TOKEN_URL
        )
        
        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns appConfig
        
        val cfg = IGDBConfig.fromEnv(env)
        
        assertEquals(IGDB_CLIENT_ID, cfg.clientId)
        assertEquals(IGDB_CLIENT_SECRET, cfg.clientSecret)
        assertEquals(IGDB_BASE_URL, cfg.baseUrl)
        assertEquals(IGDB_TOKEN_URL, cfg.tokenUrl)
    }
}