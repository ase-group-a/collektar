package integration.igdb

import com.collektar.integration.igdb.CoverDto
import com.collektar.integration.igdb.GameDto
import domain.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class IGDBMapperTest {
    
    @Test
    fun `gameToMediaItem correctly maps all values`() {
        // Build example Objects
        val coverDto = CoverDto(
            COVER_ID, COVER_IMAGE_ID
        )
        val gameDto = GameDto(
            GAME_ID, coverDto, GAME_NAME, GAME_SUMMARY
        )
        
        // Build required values
        val mappedId = "${IGDBMapper.IGDB_SOURCE_ID}:$GAME_ID"
        val mappedCoverUrl = "${IGDBMapper.IGDB_IMAGE_URL}$COVER_IMAGE_ID${IGDBMapper.IGDB_IMAGE_EXTENSION}"
        
        val mappedItem = IGDBMapper.gameToMediaItem(gameDto)

        assertNotNull(mappedItem.description)
        assertNotNull(mappedItem.imageUrl)
        assertEquals(mappedId, mappedItem.id)
        assertEquals(GAME_NAME, mappedItem.title)
        assertEquals(MediaType.GAME, mappedItem.type)
        assertEquals(mappedCoverUrl, mappedItem.imageUrl)
        assertEquals(GAME_SUMMARY, mappedItem.description)
        assertEquals(IGDBMapper.IGDB_SOURCE_ID, mappedItem.source)
    }

    @Test
    fun `gameToMediaItem correctly maps null values`() {
        // Build example Object with null values
        val gameDto = GameDto(
            GAME_ID, null, GAME_NAME, null
        )
        
        val mappedItem = IGDBMapper.gameToMediaItem(gameDto)

        assertNull(mappedItem.description)
        assertNull(mappedItem.imageUrl)
    }
}