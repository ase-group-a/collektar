package integration.spotify

import domain.MediaItem
import domain.MediaType

object SpotifyMapper {
    fun trackToMediaItem(track: TrackDto, imageIdentifierMapper: (String) -> String): MediaItem {
        val title = track.name
        val artists = track.artists.joinToString(", ") { it.name }
        val imageUrl = track.album?.images
            ?.maxByOrNull { it.width ?: 0 }
            ?.url

        return MediaItem(
            id = "spotify:track:${track.id}",
            title = title,
            type = MediaType.MUSIC,
            imageUrl = if (imageUrl != null) imageIdentifierMapper(imageUrl) else null,
            description = artists,
            source = "spotify"
        )
    }
}
