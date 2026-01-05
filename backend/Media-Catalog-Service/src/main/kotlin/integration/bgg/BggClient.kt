package integration.bgg

import domain.MediaItem
import domain.SearchResult

interface BggClient {
    suspend fun searchBoardGames(
        query: String,
        limit: Int = 20,
        offset: Int = 0
    ): SearchResult

    suspend fun hotBoardGames(
        limit: Int = 20,
        offset: Int = 0
    ): SearchResult

    suspend fun getBoardGames(ids: List<Long>): List<MediaItem>
}