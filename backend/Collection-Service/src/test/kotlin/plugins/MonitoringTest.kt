package com.collektar.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MonitoringTest {

    @Test
    fun `configureMonitoring exposes metrics route`() = testApplication {
        application {
            configureMonitoring()
        }

        val client = createClient {}
        val response = client.get("/metrics-micrometer")

        assertEquals(HttpStatusCode.OK, response.status)

        val body = response.bodyAsText()

        assert(body.contains("# HELP") || body.contains("java"))
    }
}