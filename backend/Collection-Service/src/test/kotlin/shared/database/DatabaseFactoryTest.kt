package com.collektar.shared.database

import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

class DatabaseFactoryTest {

    @Test
    fun `database factory execution`() {
        System.setProperty("DB_URL", "jdbc:h2:mem:coverage;DB_CLOSE_DELAY=-1;")
        System.setProperty("DB_USER", "sa")
        System.setProperty("DB_PASSWORD", "")

        val db = DatabaseFactory.create()

        assertNotNull(db)
    }
}