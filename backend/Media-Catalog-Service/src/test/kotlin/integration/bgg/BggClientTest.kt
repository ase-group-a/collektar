package integration.bgg

import integration.bgg.BggClient
import domain.SearchResult
import domain.MediaItem
import domain.MediaType
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
                        id = "bgg:1",
                        title = "Fake Game",
                        type = MediaType.BOARDGAME,
                        imageUrl = null,
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
                        id = "bgg:2",
                        title = "Hot Game",
                        type = MediaType.BOARDGAME,
                        imageUrl = null,
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
        val result = client.searchBoardGames("test")

        assertEquals(100, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)
        assertEquals("bgg:1", result.items[0].id)
    }

    @Test
    fun `BggClient hotBoardGames with default parameters`() = runBlocking {
        val client: BggClient = FakeBggClient()
        val result = client.hotBoardGames()

        assertEquals(50, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals("bgg:2", result.items[0].id)
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