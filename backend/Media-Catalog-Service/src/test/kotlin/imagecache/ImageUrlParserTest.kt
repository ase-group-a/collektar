package imagecache

import com.collektar.imagecache.ImageSource
import com.collektar.imagecache.ImageUrlParser.extractGoogleBooksImageId
import com.collektar.imagecache.ImageUrlParser.extractImageId
import com.collektar.imagecache.ImageUrlParser.extractSpotifyImageId
import com.collektar.imagecache.ImageUrlParser.extractTMDBImageId
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

const val SPOTIFY_IMAGE_ID = "ab67616d0000b273d2eb391e0b3c3bc21c127db6"
const val SPOTIFY_IMAGE_IDENTIFIER = "https://i.scdn.co/image/$SPOTIFY_IMAGE_ID"
const val TMDB_IMAGE_ID = "g96wHxU7EnoIFwemb2RgohIXrgW"
const val TMDB_IMAGE_IDENTIFIER = "/$TMDB_IMAGE_ID.jpg"
const val GOOGLE_BOOKS_IMAGE_ID = "RxTN7rgf19gC"
const val GOOGLE_BOOKS_IMAGE_IDENTIFIER = "http://books.google.com/books/content?id=$GOOGLE_BOOKS_IMAGE_ID&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
const val GOOGLE_BOOKS_IMAGE_IDENTIFIER_WRONG_FORMAT = "http://books.google.com/books/content&printsec=frontcover&img=1&zoom=1&edge=curl&source=gbs_api"
const val IGDB_IMAGE_ID = "coag8n" // IGDB API provides the ID directly, there is no further extraction necessary.

class ImageUrlParserTest {
    
    @Test
    fun `extractImageId correctly uses the right extraction function`() {
        var id = extractImageId(ImageSource.SPOTIFY, SPOTIFY_IMAGE_IDENTIFIER)
        assertEquals(SPOTIFY_IMAGE_ID, id)

        id = extractImageId(ImageSource.TMBD, TMDB_IMAGE_IDENTIFIER)
        assertEquals(TMDB_IMAGE_ID, id)

        id = extractImageId(ImageSource.GOOGLE_BOOKS, GOOGLE_BOOKS_IMAGE_IDENTIFIER)
        assertEquals(GOOGLE_BOOKS_IMAGE_ID, id)

        id = extractImageId(ImageSource.IGDB, IGDB_IMAGE_ID)
        assertEquals(IGDB_IMAGE_ID, id)
    }
    
    @Test
    fun `extractSpotifyImageId correctly extracts the id`() {
        val id = extractSpotifyImageId(SPOTIFY_IMAGE_IDENTIFIER)
        assertEquals(SPOTIFY_IMAGE_ID, id)
    }
    
    @Test
    fun `extractTMDBImageId  correctly extracts the id`() {
        val id = extractTMDBImageId(TMDB_IMAGE_IDENTIFIER)
        assertEquals(TMDB_IMAGE_ID, id)
    }
    
    @Test
    fun `extractGoogleBooksImageId correctly extracts the id` () {
        val id = extractGoogleBooksImageId(GOOGLE_BOOKS_IMAGE_IDENTIFIER)
        assertEquals(GOOGLE_BOOKS_IMAGE_ID, id)
    }

    @Test
    fun `extractGoogleBooksImageId has wrong format` () {
        assertThrows<IllegalArgumentException> {
            extractGoogleBooksImageId(GOOGLE_BOOKS_IMAGE_IDENTIFIER_WRONG_FORMAT)
        }
    }
}