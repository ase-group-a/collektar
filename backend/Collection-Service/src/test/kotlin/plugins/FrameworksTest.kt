package com.collektar.plugins

import com.collektar.shared.database.DatabaseFactory
import io.ktor.server.testing.*
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import io.mockk.every
import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test

class FrameworksTest {

    @Test
    fun `configureFrameworks calls DatabaseFactory create`() {
        mockkObject(DatabaseFactory)

        val mockDatabase = mockk<Database>()
        every { DatabaseFactory.create() } returns mockDatabase

        testApplication {
            application {
                configureFrameworks()
            }
        }

        verify(exactly = 1) { DatabaseFactory.create() }
    }
}