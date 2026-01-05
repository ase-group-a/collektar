package integration.bgg

import domain.MediaItem
import domain.SearchResult

interface BggClient {
    suspend fun searchBoardGames(query: String, limit: Int, offset: Int): SearchResult
    suspend fun hotBoardGames(limit: Int, offset: Int): SearchResult

    // Batch details fetch (for images etc.)
    suspend fun getBoardGames(ids: List<Long>): List<MediaItem>
}
