package integration.spotify

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SpotifyModelsTest {

    private val json = Json { encodeDefaults = true }

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

    @Test
    fun `TracksItems serializes with items`() {
        val track = TrackDto("track1", "track1", listOf(ArtistDto("arist1", "artist1")), AlbumDto("album1", "album1"))
        val tracksItems = TracksItems(listOf(track), total = 1)
        val str = json.encodeToString(tracksItems)
        val decoded = json.decodeFromString<TracksItems>(str)

        assertEquals(1, decoded.items.size)
        assertEquals("track1", decoded.items[0].id)
        assertEquals(1, decoded.total)
    }

    @Test
    fun `PlaylistTrackItemDto with track serializes`() {
        val track = TrackDto("track2", "track2")
        val item = PlaylistTrackItemDto(track)
        val str = json.encodeToString(item)
        val decoded = json.decodeFromString<PlaylistTrackItemDto>(str)

        assertNotNull(decoded.track)
        assertEquals("track2", decoded.track.id)
    }

    @Test
    fun `PlaylistTracksResponse serializes with items`() {
        val track = TrackDto("track3", "track3")
        val item = PlaylistTrackItemDto(track)
        val response = PlaylistTracksResponse(listOf(item), total = 1)

        val str = json.encodeToString(response)
        val decoded = json.decodeFromString<PlaylistTracksResponse>(str)

        assertEquals(1, decoded.items.size)
        assertEquals("track3", decoded.items[0].track!!.id)
        assertEquals(1, decoded.total)
    }
}