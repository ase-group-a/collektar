package integration.igdb

import com.collektar.integration.igdb.CoverDto
import com.collektar.integration.igdb.GameDto
import com.collektar.integration.igdb.IGDBGamesResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull


class IGDBModelsTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `GameDto deserializes correctly`() {
        val deserialized = json.decodeFromString<GameDto>(EXAMPLE_GAME_JSON)
        
        assertNotNull(deserialized.cover)
        assertNotNull(deserialized.summary)
        assertEquals(GAME_ID, deserialized.id)
        assertEquals(GAME_NAME, deserialized.name)
        assertEquals(GAME_SUMMARY, deserialized.summary)
        assertEquals(COVER_ID, deserialized.cover.id)
        assertEquals(COVER_IMAGE_ID, deserialized.cover.image_id)
    }

    @Test
    fun `GameDto with null values deserializes correctly`() {
        val deserialized = json.decodeFromString<GameDto>(EXAMPLE_GAME_NULL_JSON)
        
        assertNull(deserialized.cover)
        assertNull(deserialized.summary)
        assertEquals(GAME_ID, deserialized.id)
        assertEquals(GAME_NAME, deserialized.name)
    }

    @Test
    fun `IGDBGamesResponse serializes and deserializes correctly`() {
        // Build example Objects
        val coverDto = CoverDto(
            COVER_ID, COVER_IMAGE_ID
        )
        val gameDto = GameDto(
            GAME_ID, coverDto, GAME_NAME, GAME_SUMMARY
        )
        val igdbGamesResponse = IGDBGamesResponse(
            listOf(gameDto), EXAMPLE_IGDB_RESPONSE_X_COUNT
        )
        
        val serialized = json.encodeToString(igdbGamesResponse)
        val deserialized = json.decodeFromString<IGDBGamesResponse>(serialized)
        
        assertNotNull(deserialized.items)
        assertEquals(igdbGamesResponse.items.size, deserialized.items.size)
        assertEquals(igdbGamesResponse.total, deserialized.total)
        assertEquals(gameDto, deserialized.items[0])
        assertNotNull(deserialized.items[0].cover)
        assertNotNull(deserialized.items[0].summary)
        assertEquals(coverDto, deserialized.items[0].cover)
    }

    @Test
    fun `IGDBGamesResponse with null values serializes and deserializes correctly`() {
        // Build example Objects, without cover and summary
        val gameDto = GameDto(
            GAME_ID, null, GAME_NAME, null
        )
        val igdbGamesResponse = IGDBGamesResponse(
            listOf(gameDto), EXAMPLE_IGDB_RESPONSE_X_COUNT
        )

        val serialized = json.encodeToString(igdbGamesResponse)
        val deserialized = json.decodeFromString<IGDBGamesResponse>(serialized)

        assertNotNull(deserialized.items)
        assertEquals(igdbGamesResponse.items.size, deserialized.items.size)
        assertEquals(igdbGamesResponse.total, deserialized.total)
        assertEquals(gameDto, deserialized.items[0])
        assertNull(deserialized.items[0].cover)
        assertNull(deserialized.items[0].summary)
    }
}