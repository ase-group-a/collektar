package integration.igdb

import com.collektar.integration.igdb.GameDto
import domain.MediaItem
import domain.MediaType

object IGDBMapper {
    fun gameToMediaItem(game: GameDto): MediaItem {
        
        return MediaItem(
            id = "igdb:${game.id}",
            title = game.name,
            type = MediaType.GAME,
            imageUrl = if (game.cover != null) "https://images.igdb.com/igdb/image/upload/t_cover_big/${game.cover.image_id}.webp" else null,
            description = game.summary,
            source = "igdb"
        )
    }
}