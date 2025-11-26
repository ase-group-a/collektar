package integration.igdb

import com.collektar.integration.igdb.GameDto
import domain.MediaItem
import domain.MediaType

object IGDBMapper {
    fun gameToMediaItem(game: GameDto): MediaItem {
        
        return MediaItem(
            id = "igdbb:${game.id}",
            title = game.name,
            type = MediaType.GAME,
            imageUrl = "https://images.igdb.com/igdb/image/upload/t_cover_big/${game.cover?.image_id}.webp",
            description = game.summary,
            source = "igdb"
        )
    }
}