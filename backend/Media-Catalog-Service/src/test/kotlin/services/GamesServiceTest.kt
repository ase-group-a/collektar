package services

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import com.collektar.integration.igdb.CoverDto
import com.collektar.integration.igdb.GameDto
import com.collektar.integration.igdb.IGDBClient
import com.collektar.integration.igdb.IGDBGamesResponse
import domain.MediaType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import service.GamesService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// Values for testing
const val QUERY = "query"
const val OFFSET = 0
const val LIMIT = 20
const val TOTAL = 50
const val GAME_ID = 12345L
const val GAME_NAME = "Half-Life 3"
const val GAME_SUMMARY = "Future game release"
const val COVER_ID = 6789L
const val COVER_IMAGE_ID = "a1b2c3d4"
const val COVER_IMAGE_ID_MAPPED = "http://collektar.com/api/media/images?source=igdb&id=a1b2c3d4"

const val IGDB_SOURCE_NAME = "igdb"

class GamesServiceTest {
    private val igdbClient = mockk<IGDBClient>()
    private val imageCacheClient = mockk<ImageCacheClient>()
    private val gamesService = GamesService(igdbClient, imageCacheClient)
    
    @Test
    fun `search maps games correctly media items`() {
        val response = IGDBGamesResponse(
            items = listOf(
                GameDto(
                    GAME_ID,
                    CoverDto (
                        COVER_ID,
                        COVER_IMAGE_ID,
                    ),
                    GAME_NAME,
                    GAME_SUMMARY
                )
            ),
            total = TOTAL
        )
        
        coEvery { igdbClient.searchGames(QUERY, LIMIT, OFFSET) } returns response
        every { imageCacheClient.getImageUrl(ImageSource.IGDB, COVER_IMAGE_ID) } returns COVER_IMAGE_ID_MAPPED
        
        val res = runBlocking { gamesService.search(QUERY, LIMIT, OFFSET) }
        
        assertEquals(TOTAL, res.total)
        assertEquals(response.items.count(), res.items.count())
        
        val item = res.items.first()
        assertEquals("$IGDB_SOURCE_NAME:$GAME_ID", item.id )
        assertEquals(GAME_NAME,item.title)
        assertEquals(MediaType.GAME, item.type)
        assertNotNull(item.imageUrl)
        assertEquals(COVER_IMAGE_ID_MAPPED,item.imageUrl)
        assertEquals(GAME_SUMMARY, item.description)
        assertEquals(IGDB_SOURCE_NAME, item.source)
    }
}