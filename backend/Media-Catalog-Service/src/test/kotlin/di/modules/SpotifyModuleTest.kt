package di.modules

import com.collektar.imagecache.ImageCacheClient
import com.collektar.integration.shared.OauthTokenCache
import com.collektar.integration.shared.OauthTokenProvider
import controllers.Controller
import controllers.MusicController
import di.coreModule
import integration.spotify.SpotifyClient
import integration.spotify.SpotifyClientImpl
import integration.spotify.SpotifyConfig
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.config.MapApplicationConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

// Values used for testing
const val SPOTIFY_CLIENT_ID = "clientId"
const val SPOTIFY_CLIENT_SECRET = "clientSecret"
const val SPOTIFY_BASE_URL = "https://api.spotify.com/v1"
const val SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token"
const val SPOTIFY_DEFAULT_PLAYLIST_ID = "PlaylistId"

class SpotifyModuleTest {

    private val imageCacheClient = mockk<ImageCacheClient>()
    
    @BeforeEach
    fun setUp() {
        try {
            stopKoin()
        } catch (_: Exception) {
        }
    }

    @AfterEach
    fun tearDown() {
        try {
            stopKoin()
        } catch (_: Exception) {
        }
    }

    @Test
    fun `spotifyModule provides expected dependencies`() {
        val mapConfig = MapApplicationConfig(
            "spotify.clientId" to SPOTIFY_CLIENT_ID,
            "spotify.clientSecret" to SPOTIFY_CLIENT_SECRET,
            "spotify.baseUrl" to SPOTIFY_BASE_URL,
            "spotify.tokenUrl" to SPOTIFY_TOKEN_URL,
            "spotify.defaultPlaylistId" to SPOTIFY_DEFAULT_PLAYLIST_ID
        )

        val env = mockk<ApplicationEnvironment>()
        every { env.config } returns mapConfig

        startKoin {
            modules(
                listOf(
                    coreModule,
                    module {
                        single { imageCacheClient }
                    },
                    spotifyModule(env)
                )
            )
        }

        val cfg = GlobalContext.get().get<SpotifyConfig>(named(SPOTIFY_CONFIG_NAME))
        assertEquals(SPOTIFY_CLIENT_ID, cfg.clientId)
        assertEquals(SPOTIFY_CLIENT_SECRET, cfg.clientSecret)
        assertEquals(SPOTIFY_BASE_URL, cfg.baseUrl)
        assertEquals(SPOTIFY_TOKEN_URL, cfg.tokenUrl)
        assertEquals(SPOTIFY_DEFAULT_PLAYLIST_ID, cfg.defaultPlaylistId)

        val cache = GlobalContext.get().get<OauthTokenCache>(named(SPOTIFY_TOKEN_CACHE_NAME))
        assertNotNull(cache)

        val tokenProvider = GlobalContext.get().get<OauthTokenProvider>(named(SPOTIFY_TOKEN_PROVIDER_NAME))
        assertNotNull(tokenProvider)

        val client = GlobalContext.get().get<SpotifyClient>()
        assertIs<SpotifyClientImpl>(client)

        val musicService = GlobalContext.get().get<service.MusicService>()
        assertNotNull(musicService)

        val controller = GlobalContext.get().get<Controller>(named(SPOTIFY_CONTROLLER_NAME))
        assertIs<MusicController>(controller)
    }
}