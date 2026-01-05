package imagecache

import com.collektar.imagecache.ImageCacheClient
import com.collektar.imagecache.ImageSource
import com.collektar.service.ImageCacheService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

const val IMAGE_SOURCE = "igdb"
const val IMAGE_ID = "coag8n"
val EXAMPLE_IMAGE_DATA = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10) // Example binary data

class ImageCacheServiceTest {
    private val imageCacheClient = mockk<ImageCacheClient>()
    private val imageCacheService = ImageCacheService(imageCacheClient)
    
    @Test
    fun `ImageCacheService requires both source and id`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { imageCacheService.getImage(null, null) }
        }

        assertThrows<IllegalArgumentException> {
            runBlocking { imageCacheService.getImage(IMAGE_SOURCE, null) }
        }

        assertThrows<IllegalArgumentException> {
            runBlocking { imageCacheService.getImage(null, IMAGE_ID) }
        }
    }
    
    @Test
    fun `ImageCacheService correctly maps source and returns data`() {
        coEvery { imageCacheClient.getImage(ImageSource.IGDB, IMAGE_ID) } returns EXAMPLE_IMAGE_DATA
        
        val image = runBlocking { imageCacheService.getImage(IMAGE_SOURCE, IMAGE_ID) }
        assertEquals(EXAMPLE_IMAGE_DATA, image)
    }
}