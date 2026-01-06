package integration.bgg

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class BggClientTest {

    class FakeBggClient : BggClient {
        override suspend fun searchBoardGames(query: String, limit: Int, offset: Int): SearchResult {
            return SearchResult(
                total = 100,
                limit = limit,
                offset = offset,
                items = listOf(
                    MediaItem(
                        id = "bgg:13",
                        title = "Catan",
                        type = MediaType.BOARDGAME,
                        imageUrl = "https://example.com/catan.jpg",
                        description = null,
                        source = "BGG"
                    )
                )
            )
        }

        override suspend fun hotBoardGames(limit: Int, offset: Int): SearchResult {
            return SearchResult(
                total = 50,
                limit = limit,
                offset = offset,
                items = listOf(
                    MediaItem(
                        id = "bgg:174430",
                        title = "Gloomhaven",
                        type = MediaType.BOARDGAME,
                        imageUrl = "https://example.com/gloomhaven.jpg",
                        description = null,
                        source = "BGG"
                    )
                )
            )
        }

        override suspend fun getBoardGames(ids: List<Long>): List<MediaItem> {
            return ids.map { id ->
                MediaItem(
                    id = "bgg:$id",
                    title = "Game $id",
                    type = MediaType.BOARDGAME,
                    imageUrl = null,
                    description = null,
                    source = "BGG"
                )
            }
        }
    }

    @Test
    fun `BggClient searchBoardGames with default parameters`() = runBlocking {
        val client: BggClient = FakeBggClient()
        val result = client.searchBoardGames("catan", 20, 0)

        assertEquals(100, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)
        assertEquals("bgg:13", result.items[0].id)
        assertEquals("Catan", result.items[0].title)
    }

    @Test
    fun `BggClient hotBoardGames returns hot games`() = runBlocking {
        val client: BggClient = FakeBggClient()
        val result = client.hotBoardGames(20, 0)

        assertEquals(50, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals("bgg:174430", result.items[0].id)
        assertEquals("Gloomhaven", result.items[0].title)
    }

    @Test
    fun `BggClient getBoardGames returns correct items`() = runBlocking {
        val client: BggClient = FakeBggClient()
        val result = client.getBoardGames(listOf(1L, 2L, 3L))

        assertEquals(3, result.size)
        assertEquals("bgg:1", result[0].id)
        assertEquals("bgg:2", result[1].id)
        assertEquals("bgg:3", result[2].id)
    }
}