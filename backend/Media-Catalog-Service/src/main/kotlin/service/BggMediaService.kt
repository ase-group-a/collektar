package service

import domain.SearchResult
import integration.bgg.BggClient

class BggMediaService(
    private val client: BggClient
) {
    suspend fun search(
        query: String?,
        limit: Int = 20,
        offset: Int = 0
    ): SearchResult {
        if (query.isNullOrBlank()) {
            return SearchResult(
                total = 0,
                limit = limit,
                offset = offset,
                items = emptyList()
            )
        }
        return client.searchBoardGames(query, limit, offset)
    }
}
