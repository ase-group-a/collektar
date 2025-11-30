package integration.igdb

import com.collektar.integration.igdb.GameDto
import domain.MediaItem
import domain.MediaType

object IGDBMapper {

    const val IGDB_IMAGE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/"
    const val IGDB_IMAGE_EXTENSION = ".webp"
    const val IGDB_SOURCE_ID = "igdb"
    
    fun gameToMediaItem(game: GameDto): MediaItem {
        return MediaItem(
            id = "${IGDB_SOURCE_ID}:${game.id}",
            title = game.name,
            type = MediaType.GAME,
            imageUrl = if (game.cover != null) IGDB_IMAGE_URL + game.cover.imageId + IGDB_IMAGE_EXTENSION else null,
            description = game.summary,
            source = IGDB_SOURCE_ID
        )
    }
}