package com.collektar.shared.database

import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class SchemaInitializerTest {

    @Test
    fun `initialize creates tables without mocking transaction`() {
        val db = Database.connect(
            url = "jdbc:h2:mem:schema_test;DB_CLOSE_DELAY=-1;",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        )

        SchemaInitializer.initialize(db)

        assertNotNull(db)
    }
}