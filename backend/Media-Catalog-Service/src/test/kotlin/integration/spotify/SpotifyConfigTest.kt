package integration.spotify

import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpotifyConfigTest {

    @Test
    fun `fromEnv reads values from ApplicationEnvironment config`() {
        val mapConfig = MapApplicationConfig(
            "spotify.clientId" to "id",
            "spotify.clientSecret" to "secret",
            "spotify.baseUrl" to "https://api.spotify.com/v1",
            "spotify.tokenUrl" to "https://accounts.spotify.com/api/token",
            "spotify.defaultPlaylistId" to "PlaylistId"
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        val cfg = SpotifyConfig.fromEnv(env)

        assertEquals("id", cfg.clientId)
        assertEquals("secret", cfg.clientSecret)
        assertEquals("https://api.spotify.com/v1", cfg.baseUrl)
        assertEquals("https://accounts.spotify.com/api/token", cfg.tokenUrl)
        assertEquals("PlaylistId", cfg.defaultPlaylistId)
    }

    @Test
    fun `fromEnv throws when required values missing`() {
        val originalClientId = System.getenv("SPOTIFY_CLIENT_ID")
        try {
            System.clearProperty("SPOTIFY_CLIENT_ID")
            val emptyConfig = MapApplicationConfig()
            val env = mockk<ApplicationEnvironment>()
            every { env.config } returns emptyConfig

            assertFailsWith<IllegalStateException> {
                SpotifyConfig.fromEnv(env)
            }
        } finally {
            if (originalClientId != null) {
                System.setProperty("SPOTIFY_CLIENT_ID", originalClientId)
            }
        }
    }

}
