package com.collektar.shared.database

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun create(): Database {
        val db = Database.Companion.connect(
            url = System.getenv("DB_URL"),
            user = System.getenv("DB_USER"),
            password = System.getenv("DB_PASSWORD"),
            driver = "org.postgresql.Driver",
        )
        SchemaInitializer.initialize(db)

        return db
    }
}