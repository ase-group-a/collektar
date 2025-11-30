package integration.igdb

const val GAME_ID = 233L
const val COVER_ID = 77288L
const val COVER_IMAGE_ID = "co1nmw"
const val GAME_NAME = "Half-Life 2"
const val GAME_SUMMARY =
    "1998. HALF-LIFE sends a shock through the game industry with its combination of pounding ..." // Shortened

// generated using the following query options:
// fields id, name, summary, cover.image_id; limit 1; offset 0; search "Half-Life 2";
// JSON array brackets have been removed, as deserialization of arrays will not be tested here
const val EXAMPLE_GAME_JSON = """
{
    "id": $GAME_ID,
    "cover": {
        "id": $COVER_ID,
        "image_id": "$COVER_IMAGE_ID"
    },
    "name": "$GAME_NAME",
    "summary": "$GAME_SUMMARY"
}"""

// Same as previous, but with cover and summary missing, as this is possible for some games
const val EXAMPLE_GAME_NULL_JSON = """
{
    "id": $GAME_ID,
    "name": "$GAME_NAME"
}"""

const val CLIENT_ID = "igdb_client_id"
const val CLIENT_SECRET = "igdb_client_secret"
const val TOKEN = "igdb_token"
const val BASE_URL = "https://api.igdb.com/v4"
const val GAMES_URL = "$BASE_URL/games"
const val TOKEN_URL = "https://id.twitch.tv/oauth2/token"
const val AUTH_HEADER_VALUE = "Bearer $TOKEN"
const val QUERY = "Some search query"
const val LIMIT = 5
const val OFFSET = 0

// Raw IGDB response with the following parameters:
// fields id, name, summary, cover.image_id; limit 3; offset 1; search "Half-Life 2";
const val EXAMPLE_IGDB_RESPONSE_COUNT = 3
const val EXAMPLE_IGDB_RESPONSE_JSON = """
[
    {
        "id": 271414,
        "cover": {
            "id": 336850,
            "image_id": "co77wy"
        },
        "name": "Half-Life 2: MMod - Half-Life 2: Episode One",
        "summary": "This mod allows Half-Life 2: Episode One to be run under MMod.\n\nThe goal of Half-Life 2: MMod is to enhance and expand gunplay, combat mechanics and the immersion factor by giving the Player more options and combat opportunities as well as refine how the Player handles his arsenal. Half-Life 2: MMod also offers minor AI enhancements, extended abilities for combine soldiers ( dynamic jumping, firing smg1 underbarrel grenades, shotgunner double blast, etc. ), multiple bug fixes, enhanced visuals, VFX re-design, sound redesign and much much more, while keeping nearly every new feature in the mod totally optional."
    },
    {
        "id": 271415,
        "cover": {
            "id": 336898,
            "image_id": "co77ya"
        },
        "name": "Half-Life 2: MMod - Half-Life 2: Episode Two",
        "summary": "This mod allows Episode Two to be run under MMod.\n\nThe goal of Half-Life 2: MMod is to enhance and expand gunplay, combat mechanics and the immersion factor by giving the Player more options and combat opportunities as well as refine how the Player handles his arsenal. Half-Life 2: MMod also offers minor AI enhancements, extended abilities for combine soldiers ( dynamic jumping, firing smg1 underbarrel grenades, shotgunner double blast, etc. ), multiple bug fixes, enhanced visuals, VFX re-design, sound redesign and much much more, while keeping nearly every new feature in the mod totally optional."
    },
    {
        "id": 233,
        "cover": {
            "id": 77288,
            "image_id": "co1nmw"
        },
        "name": "Half-Life 2",
        "summary": "1998. HALF-LIFE sends a shock through the game industry with its combination of pounding action and continuous, immersive storytelling.\n\nNOW. By taking the suspense, challenge and visceral charge of the original, and adding startling new realism and responsiveness, Half-Life 2 opens the door to a world where the player's presence affects everything around them, from the physical environment to the behaviors even the emotions of both friends and enemies."
    }
]"""

// Value of X-Count HTTP header provided by IGDB, which indicates the total count of found elements.
const val EXAMPLE_IGDB_RESPONSE_X_COUNT = 44