package com.collektar.shared.errors

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusPageTest {

    @Test
    fun `IllegalArgumentException returns 400 with message`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { encodeDefaults = true })
            }
            configureStatusPages()
            routing {
                get("/illegal") { throw IllegalArgumentException("Invalid input") }
            }
        }

        val response: HttpResponse = client.get("/illegal") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals("""{"message":"Invalid input"}""", response.bodyAsText())
    }

    @Test
    fun `NoSuchElementException returns 404 with message`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { encodeDefaults = true })
            }
            configureStatusPages()
            routing {
                get("/nosuch") { throw NoSuchElementException("Not found element") }
            }
        }

        val response: HttpResponse = client.get("/nosuch") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals("""{"message":"Not found element"}""", response.bodyAsText())
    }

    @Test
    fun `SecurityException returns 403 with message`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { encodeDefaults = true })
            }
            configureStatusPages()
            routing {
                get("/forbidden") { throw SecurityException("Access denied") }
            }
        }

        val response: HttpResponse = client.get("/forbidden") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
        assertEquals("""{"message":"Access denied"}""", response.bodyAsText())
    }

    @Test
    fun `Unhandled Throwable returns 500 with generic message`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { encodeDefaults = true })
            }
            configureStatusPages()
            routing {
                get("/error") { throw RuntimeException("something went wrong") }
            }
        }

        val response: HttpResponse = client.get("/error") {
            header(HttpHeaders.Accept, ContentType.Application.Json)
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals("""{"message":"Internal server error"}""", response.bodyAsText())
    }
}