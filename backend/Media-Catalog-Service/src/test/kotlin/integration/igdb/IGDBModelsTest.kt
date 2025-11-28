package integration.igdb

import com.collektar.integration.igdb.CoverDto
import com.collektar.integration.igdb.GameDto
import com.collektar.integration.igdb.IGDBGamesResponse
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

const val GAME_ID = 233L
const val COVER_ID = 77288L
const val COVER_IMAGE_ID = "co1nmw"
const val GAME_NAME = "Half-Life 2"
const val GAME_SUMMARY = "1998. HALF-LIFE sends a shock through the game industry with its combination of pounding ..." // Shortened

class IGDBModelsTest {
    
    // generated using the following query options:
    // fields id, name, summary, cover.image_id; limit 1; offset 0; search "Half-Life 2";
    // JSON array brackets have been removed, as deserialization of arrays will not be tested here
    private val exampleResponseJson = """
            {
                "id": $GAME_ID,
                "cover": {
                    "id": $COVER_ID,
                    "image_id": "$COVER_IMAGE_ID"
                },
                "name": "$GAME_NAME",
                "summary": "$GAME_SUMMARY"
            }
    """.trimIndent()

    // Same as previous, but with cover and summary missing, as this is possible for some games
    private val exampleResponseMissingValuesJson = """
            {
                "id": $GAME_ID,
                "name": "$GAME_NAME"
            }
    """.trimIndent()

    private val json = Json { encodeDefaults = true }

    @Test
    fun `GameDto deserializes correctly`() {
        val deserialized = json.decodeFromString<GameDto>(exampleResponseJson)
        
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
        val deserialized = json.decodeFromString<GameDto>(exampleResponseMissingValuesJson)
        
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
            listOf(gameDto), 1
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
            listOf(gameDto), 1
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