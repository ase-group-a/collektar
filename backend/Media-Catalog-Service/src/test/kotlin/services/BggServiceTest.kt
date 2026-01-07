package service

import domain.MediaItem
import domain.MediaType
import domain.SearchResult
import integration.bgg.BggClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class BggServiceTest {

    private val mockClient = mockk<BggClient>()

    @Test
    fun `search delegates to client with correct parameters`() = runBlocking {
        val expectedResult = SearchResult(
            total = 2,
            limit = 10,
            offset = 0,
            items = listOf(
                MediaItem("bgg:13", "Catan", MediaType.BOARDGAMES, null, null, "BGG"),
                MediaItem("bgg:822", "Carcassonne", MediaType.BOARDGAMES, null, null, "BGG")
            )
        )

        coEvery { mockClient.searchBoardGames("catan", 10, 0) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.search("catan", 10, 0)

        assertEquals(2, result.total)
        assertEquals(10, result.limit)
        assertEquals(0, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("Catan", result.items[0].title)
        assertEquals("Carcassonne", result.items[1].title)

        coVerify(exactly = 1) { mockClient.searchBoardGames("catan", 10, 0) }
    }

    @Test
    fun `search with default parameters`() = runBlocking {
        val expectedResult = SearchResult(
            total = 100,
            limit = 20,
            offset = 0,
            items = listOf(
                MediaItem("bgg:174430", "Gloomhaven", MediaType.BOARDGAMES, null, null, "BGG")
            )
        )

        coEvery { mockClient.searchBoardGames("gloomhaven", 20, 0) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.search("gloomhaven")

        assertEquals(100, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)

        coVerify(exactly = 1) { mockClient.searchBoardGames("gloomhaven", 20, 0) }
    }

    @Test
    fun `search with pagination`() = runBlocking {
        val expectedResult = SearchResult(
            total = 150,
            limit = 25,
            offset = 50,
            items = listOf(
                MediaItem("bgg:1", "Game 1", MediaType.BOARDGAMES, null, null, "BGG")
            )
        )

        coEvery { mockClient.searchBoardGames("strategy", 25, 50) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.search("strategy", 25, 50)

        assertEquals(150, result.total)
        assertEquals(25, result.limit)
        assertEquals(50, result.offset)

        coVerify(exactly = 1) { mockClient.searchBoardGames("strategy", 25, 50) }
    }

    @Test
    fun `search returns empty result when no games found`() = runBlocking {
        val expectedResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        coEvery { mockClient.searchBoardGames("nonexistent", 20, 0) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.search("nonexistent")

        assertEquals(0, result.total)
        assertTrue(result.items.isEmpty())

        coVerify(exactly = 1) { mockClient.searchBoardGames("nonexistent", 20, 0) }
    }

    @Test
    fun `hot delegates to client with correct parameters`() = runBlocking {
        val expectedResult = SearchResult(
            total = 50,
            limit = 15,
            offset = 5,
            items = listOf(
                MediaItem("bgg:1", "Hot Game 1", MediaType.BOARDGAMES, null, null, "BGG"),
                MediaItem("bgg:2", "Hot Game 2", MediaType.BOARDGAMES, null, null, "BGG")
            )
        )

        coEvery { mockClient.hotBoardGames(15, 5) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.hot(15, 5)

        assertEquals(50, result.total)
        assertEquals(15, result.limit)
        assertEquals(5, result.offset)
        assertEquals(2, result.items.size)
        assertEquals("Hot Game 1", result.items[0].title)
        assertEquals("Hot Game 2", result.items[1].title)

        coVerify(exactly = 1) { mockClient.hotBoardGames(15, 5) }
    }

    @Test
    fun `hot with default parameters`() = runBlocking {
        val expectedResult = SearchResult(
            total = 50,
            limit = 20,
            offset = 0,
            items = listOf(
                MediaItem("bgg:174430", "Gloomhaven", MediaType.BOARDGAMES, null, null, "BGG")
            )
        )

        coEvery { mockClient.hotBoardGames(20, 0) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.hot()

        assertEquals(50, result.total)
        assertEquals(20, result.limit)
        assertEquals(0, result.offset)
        assertEquals(1, result.items.size)

        coVerify(exactly = 1) { mockClient.hotBoardGames(20, 0) }
    }

    @Test
    fun `hot returns empty result when no hot games`() = runBlocking {
        val expectedResult = SearchResult(
            total = 0,
            limit = 20,
            offset = 0,
            items = emptyList()
        )

        coEvery { mockClient.hotBoardGames(20, 0) } returns expectedResult

        val service = BggMediaService(mockClient)
        val result = service.hot()

        assertEquals(0, result.total)
        assertTrue(result.items.isEmpty())

        coVerify(exactly = 1) { mockClient.hotBoardGames(20, 0) }
    }

    @Test
    fun `search propagates exception from client`() = runBlocking {
        coEvery { mockClient.searchBoardGames(any(), any(), any()) } throws RuntimeException("BGG API error")

        val service = BggMediaService(mockClient)

        val exception = assertFailsWith<RuntimeException> {
            service.search("test")
        }

        assertEquals("BGG API error", exception.message)
    }

    @Test
    fun `hot propagates exception from client`() = runBlocking {
        coEvery { mockClient.hotBoardGames(any(), any()) } throws RuntimeException("BGG API error")

        val service = BggMediaService(mockClient)

        val exception = assertFailsWith<RuntimeException> {
            service.hot()
        }

        assertEquals("BGG API error", exception.message)
    }
}