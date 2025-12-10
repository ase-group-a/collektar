// src/main/kotlin/integration/bgg/BggClient.kt
package integration.bgg

import domain.MediaItem
import domain.SearchResult

interface BggClient {
    suspend fun searchBoardGames(
        query: String,
        limit: Int = 20,
        offset: Int = 0
    ): SearchResult

    suspend fun getBoardGame(id: Long): MediaItem?
}
