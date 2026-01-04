package service

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import domain.MediaType
import io.mockk.coEvery
import io.mockk.mockk
import integration.spotify.*
import io.mockk.every
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

const val COVER_IMAGE_URL = "https://img/1.jpg"
const val COVER_IMAGE_URL_MAPPED = "https://img/2.jpg:mapped"

class MusicServiceTest {

    private val spotifyClient = mockk<SpotifyClient>()
    private val imageCacheClient = mockk<ImageCacheClient>()
    private val musicService = MusicService(spotifyClient, imageCacheClient)
    
    @Test
    fun `search maps spotify tracks to media items`() = runTest {
        val track = TrackDto(
            id = "track1",
            name = "track1",
            artists = listOf(ArtistDto(id = "artist1", name = "artist1")),
            album = AlbumDto(
                id = "album1",
                name = "album1",
                images = listOf(ImageDto(height = 100, url = COVER_IMAGE_URL, width = 100))
            )
        )

        val spotifyResponse = SpotifyTracksSearchResponse(
            tracks = TracksItems(items = listOf(track), total = 1)
        )

        coEvery { spotifyClient.searchTracks("query", 20, 0) } returns spotifyResponse
        every { imageCacheClient.getImageUrl(ImageSource.SPOTIFY, COVER_IMAGE_URL) } returns COVER_IMAGE_URL_MAPPED

        val result = musicService.search("query", limit = 20, offset = 0)

        assertEquals(1, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)

        val item = result.items.first()
        assertEquals("spotify:track:track1", item.id)
        assertEquals("track1", item.title)
        assertEquals(MediaType.MUSIC, item.type)
        assertEquals(COVER_IMAGE_URL_MAPPED, item.imageUrl)
        assertEquals("artist1", item.description)
        assertEquals("spotify", item.source)
    }
}
