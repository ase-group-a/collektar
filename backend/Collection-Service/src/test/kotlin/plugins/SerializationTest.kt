package com.collektar.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SerializationTest {

    @Test
    fun `configureSerialization responds with JSON`() = testApplication {
        application {
            configureSerialization()
        }

        val client = createClient {}
        val response = client.get("/json/kotlinx-serialization")

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()

        assertTrue(body.contains("hello"))
        assertTrue(body.contains("world"))
    }
}