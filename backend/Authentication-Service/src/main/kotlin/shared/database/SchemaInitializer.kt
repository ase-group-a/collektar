package com.collektar.shared.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object SchemaInitializer {
    fun initialize(database: Database) {
        transaction(database) {
            SchemaUtils.create(
                Tables.Users,
                Tables.RefreshTokens,
            )
        }
    }
}