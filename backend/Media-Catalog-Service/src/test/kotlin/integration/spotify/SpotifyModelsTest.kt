package integration.spotify

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpotifyModelsTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `SpotifyTokenResponse serializes and deserializes correctly`() {
        val token = SpotifyTokenResponse("test", "Bearer", 3600)
        val str = json.encodeToString(token)
        val decoded = json.decodeFromString<SpotifyTokenResponse>(str)

        assertEquals(token.accessToken, decoded.accessToken)
        assertEquals(token.tokenType, decoded.tokenType)
        assertEquals(token.expiresIn, decoded.expiresIn)
    }

    @Test
    fun `TrackDto with default values serializes`() {
        val track = TrackDto(id = "track1", name = "track1")
        val str = json.encodeToString(track)
        val decoded = json.decodeFromString<TrackDto>(str)

        assertEquals(track.id, decoded.id)
        assertEquals(track.name, decoded.name)
        assertNotNull(decoded.artists)
        assertEquals(0, decoded.artists.size)
        assertEquals(null, decoded.album)
    }

    @Test
    fun `SpotifyTracksSearchResponse default tracks is not null`() {
        val response = SpotifyTracksSearchResponse()
        assertNotNull(response.tracks)
        assertEquals(0, response.tracks.items.size)
        assertEquals(0, response.tracks.total)
    }

    @Test
    fun `PlaylistTracksResponse default items is empty`() {
        val response = PlaylistTracksResponse()
        assertNotNull(response.items)
        assertEquals(0, response.items.size)
        assertEquals(0, response.total)
    }

    @Test
    fun `AlbumDto with images serializes correctly`() {
        val album = AlbumDto("album1", "album1", listOf(ImageDto(100, "url1"), ImageDto(200, "url2")))
        val str = json.encodeToString(album)
        val decoded = json.decodeFromString<AlbumDto>(str)

        assertEquals(album.id, decoded.id)
        assertEquals(album.name, decoded.name)
        assertEquals(2, decoded.images.size)
        assertEquals(100, decoded.images[0].height)
        assertEquals("url1", decoded.images[0].url)
    }

    @Test
    fun `ArtistDto serializes correctly`() {
        val artist = ArtistDto("artist1", "artist1")
        val str = json.encodeToString(artist)
        val decoded = json.decodeFromString<ArtistDto>(str)

        assertEquals(artist.id, decoded.id)
        assertEquals(artist.name, decoded.name)
    }
}
