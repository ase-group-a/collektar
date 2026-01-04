package com.collektar.plugins

import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HTTPTest {

    @Test
    fun `configureHTTP installs DefaultHeaders and SwaggerUI`() = testApplication {
        application { configureHTTP() }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get("/openapi")
        assertEquals(HttpStatusCode.OK, response.status)

        assertTrue(response.headers.contains("X-Engine"))
    }
}