package service

import domain.SearchResult
import integration.bgg.BggClient

class BggMediaService(
    private val client: BggClient
) {
    suspend fun search(query: String, limit: Int = 20, offset: Int = 0): SearchResult =
        client.searchBoardGames(query, limit, offset)

    suspend fun hot(limit: Int = 20, offset: Int = 0): SearchResult =
        client.hotBoardGames(limit, offset)
}
