package integration.igdb

import com.collektar.integration.igdb.GameDto
import domain.MediaItem
import domain.MediaType

object IGDBMapper {

    const val IGDB_SOURCE_ID = "igdb"
    
    fun gameToMediaItem(game: GameDto, imageIdentifierMapper: (String) -> String): MediaItem {
        return MediaItem(
            id = "${IGDB_SOURCE_ID}:${game.id}",
            title = game.name,
            type = MediaType.GAMES,
            imageUrl = if (game.cover != null) imageIdentifierMapper(game.cover.imageId) else null,
            description = game.summary,
            source = IGDB_SOURCE_ID
        )
    }
}