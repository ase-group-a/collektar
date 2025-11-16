package com.collektar.shared.database

import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.PostgisContainerProvider
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

class DatabaseFactoryTest {
    @Test
    fun shouldInititializeDBAndCreateSchemas() {
        val provider = PostgisContainerProvider()
        val postgres = provider.newInstance("18-3.6-alpine") as JdbcDatabaseContainer<*>
        postgres.start()

        EnvironmentVariables(
            mapOf(
                "DB_URL" to postgres.jdbcUrl,
                "DB_USER" to postgres.username,
                "DB_PASSWORD" to postgres.password
            )
        ).execute {
            val db = DatabaseFactory.create()
            transaction(db) {
                assertTrue(Tables.Users.exists())
                assertTrue(Tables.RefreshTokens.exists())
            }
        }

        postgres.stop()
    }
}