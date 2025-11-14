package integration.spotify

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long
)

@Serializable
data class ImageDto(val height: Int? = null, val url: String, val width: Int? = null)

@Serializable
data class ArtistDto(val id: String, val name: String)

@Serializable
data class AlbumDto(val id: String, val name: String, val images: List<ImageDto> = emptyList())

@Serializable
data class TrackDto(
    val id: String,
    val name: String,
    val artists: List<ArtistDto> = emptyList(),
    val album: AlbumDto? = null
)

@Serializable
data class TracksItems(val items: List<TrackDto> = emptyList(), val total: Int = 0)

@Serializable
data class SpotifyTracksSearchResponse(
    val tracks: TracksItems? = null
)

@Serializable
data class PlaylistTrackItemDto(
    val track: TrackDto? = null
)

@Serializable
data class PlaylistTracksResponse(
    val items: List<PlaylistTrackItemDto> = emptyList(),
    val total: Int = 0
)