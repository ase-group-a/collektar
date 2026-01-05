package imagecache

import com.collektar.controllers.ImageCacheController
import com.collektar.service.ImageCacheService
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertArrayEquals
import kotlin.test.Test
import kotlin.test.assertEquals

const val SOURCE = "igdb"
const val ID = "coag8n"
const val IMAGE_PATH = "/images?source=$SOURCE&id=$ID"

class ImageCacheControllerTest {
    private val imageCacheService = mockk<ImageCacheService>()
    private val imageCacheController = ImageCacheController(imageCacheService)
    
    private fun Application.configureTestApp() {
        routing {
            imageCacheController.register(this)
        }
    }
    
    @Test
    fun `source and image get correctly mapped`() = testApplication { 
        application {
            configureTestApp()
        }

        coEvery { imageCacheService.getImage(SOURCE, ID) } returns EXAMPLE_IMAGE_DATA
        
        val response = client.get(IMAGE_PATH)
        
        assertEquals(HttpStatusCode.OK, response.status)
        assertArrayEquals(EXAMPLE_IMAGE_DATA, response.bodyAsBytes())
        coVerify(exactly = 1) { imageCacheService.getImage(SOURCE, ID) }
    }
}