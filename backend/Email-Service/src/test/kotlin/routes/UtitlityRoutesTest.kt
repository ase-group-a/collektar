package com.collektar.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilityRoutesTest {

    @Test
    fun shouldReturnOKForHealthCheck() = testApplication {
        application {
            configureTestRouting()
        }

        val result = client.get("/health")

        assertEquals(HttpStatusCode.OK, result.status)
        assertEquals("OK", result.bodyAsText())
    }

    private fun io.ktor.server.application.Application.configureTestRouting() {
        routing {
            utilityRoutes()
        }
    }
}