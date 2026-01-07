package imagecache

import com.collektar.imagecache.ImageCacheClientImpl
import com.collektar.imagecache.ImageCacheConfig
import com.collektar.imagecache.ImageSource
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import redis.clients.jedis.RedisClient
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

val IMAGE_RESPONSE = byteArrayOf(1, 2, 3, 4, 5)
val IMAGE_URL = "$IMAGE_CACHE_URL_PREFIX?source=${ImageSource.IGDB.toString().lowercase()}&id=$IMAGE_ID"

class ImageCacheClientImplTest {
    private val mockConfig = mockk<ImageCacheConfig>()
    private val mockRedis = mockk<RedisClient>()
    private var mockHttpClient : HttpClient? = null

    private fun mockHttpClient(handler: MockRequestHandler): HttpClient {
        return HttpClient(MockEngine { request -> handler(request) })
    }
    
    @BeforeTest
    fun setup() {
        every { mockConfig.cacheTTL } returns IMAGE_CACHE_TTL.toLong()
        every { mockConfig.urlPrefix } returns IMAGE_CACHE_URL_PREFIX
        every { mockConfig.spotifyUrlPrefix } returns SPOTIFY_IMAGE_URL
        every { mockConfig.tmdbUrlPrefix } returns TMDB_IMAGE_URL
        every { mockConfig.igdbUrlPrefix } returns IGDB_IMAGE_URL
        every { mockConfig.googleBooksUrlPrefix } returns GOOGLE_BOOKS_IMAGE_URL

        mockHttpClient = mockHttpClient { request ->
            assertEquals("$IGDB_IMAGE_URL$IMAGE_ID.jpg", request.url.toString())

            respond(
                content = IMAGE_RESPONSE, status = HttpStatusCode.OK, headers = headersOf(
                    HttpHeaders.ContentType to listOf(ContentType.Image.JPEG.toString()),
                )
            )
        }
    }
    
    @Test
    fun `Valid source and id should return image`() {
        val client = ImageCacheClientImpl(mockHttpClient!!, mockConfig, mockRedis)
        val redisImageId = "${ImageSource.IGDB}:$IMAGE_ID".toByteArray()
        
        every { mockRedis.get(redisImageId) } returns null
        every { mockRedis.setex(redisImageId, IMAGE_CACHE_TTL.toLong(), IMAGE_RESPONSE) } returns ""
        
        val res = assertDoesNotThrow { runBlocking { client.getImage(ImageSource.IGDB, IMAGE_ID) } }
        
        assertArrayEquals(IMAGE_RESPONSE, res)
    }
    
    @Test
    fun `getImageUrl correctly returns expected url`() {
        val client = ImageCacheClientImpl(mockHttpClient!!, mockConfig, mockRedis)
        
        assertEquals(IMAGE_URL, client.getImageUrl(ImageSource.IGDB, IMAGE_ID))
    }
}