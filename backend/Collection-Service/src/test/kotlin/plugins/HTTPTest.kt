import com.collektar.plugins.configureHTTP
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*

class ConfigureHttpTest {

    @Test
    fun `default headers are added`() = testApplication {
        application {
            configureHTTP()
            routing {
                get("/test") { }
            }
        }

        val response = client.get("/test")

        assertEquals(
            "Ktor",
            response.headers["X-Engine"]
        )
    }

    @Test
    fun `swagger ui is reachable`() = testApplication {
        application {
            configureHTTP()
        }

        val response = client.get("/openapi")

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `cors allows any host in dev`() = testApplication {
        application {
            configureHTTP()
            routing {
                get("/test") { }
            }
        }

        val response = client.options("/test") {
            header(HttpHeaders.Origin, "http://otherhost.com")
            header(HttpHeaders.AccessControlRequestMethod, "GET")
        }

        assertNotNull(
            response.headers[HttpHeaders.AccessControlAllowOrigin],
            "CORS should allow any host in dev"
        )
    }
}
