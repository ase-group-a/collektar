package service

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import integration.bgg.BggClient
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class BggMediaServiceTest {

    class FakeBggClient : BggClient {
        override suspend fun searchBoardGames(query: String, limit: Int, offset: Int): SearchResult {
            return SearchResult(
                total = 1,
                limit = limit,
                offset = offset,
                items = listOf(
                    MediaItem("bgg:1", "Game", MediaType.BOARDGAME, null, null, "BGG")
                )
            )
        }

        override suspend fun hotBoardGames(limit: Int, offset: Int): SearchResult {
            return SearchResult(
                total = 1,
                limit = limit,
                offset = offset,
                items = listOf(
                    MediaItem("bgg:2", "Hot Game", MediaType.BOARDGAME, null, null, "BGG")
                )
            )
        }

        override suspend fun getBoardGames(ids: List<Long>): List<MediaItem> = emptyList()
    }

    @Test
    fun `search delegates to client`() = runBlocking {
        val service = BggMediaService(FakeBggClient())
        val result = service.search("catan", 10, 5)

        assertEquals(1, result.total)
        assertEquals(10, result.limit)
        assertEquals(5, result.offset)
        assertEquals("bgg:1", result.items[0].id)
    }

    @Test
    fun `hot delegates to client`() = runBlocking {
        val service = BggMediaService(FakeBggClient())
        val result = service.hot(15, 10)

        assertEquals(1, result.total)
        assertEquals(15, result.limit)
        assertEquals(10, result.offset)
        assertEquals("bgg:2", result.items[0].id)
    }
}