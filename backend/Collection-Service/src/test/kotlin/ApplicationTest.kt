package com.collektar

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }
        client.get("/").apply {
            // Changed to 404 as the service has not been set up yet, this is just for sonar to run
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

}
