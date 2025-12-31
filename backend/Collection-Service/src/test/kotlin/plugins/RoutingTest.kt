package com.collektar.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class RoutingTest {

    @Test
    fun `configureRouting responds with health OK`() = testApplication {
        application {
            configureRouting()
        }

        val client = createClient {}
        val response = client.get("/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("OK", response.bodyAsText())
    }
}
