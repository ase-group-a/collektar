package com.collektar.plugins

import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

class SecurityTest {

    @Test
    fun `configureSecurity installs JWT authentication`() = testApplication {
        application {
            configureSecurity()
        }

    }
}