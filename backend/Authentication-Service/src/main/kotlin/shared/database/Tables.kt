package com.collektar.shared.database

import org.jetbrains.exposed.sql.Table

object Tables {
    object Users : Table("users") {
        val id = uuid("uuid").uniqueIndex()
        val username = varchar("username", length = 50).uniqueIndex()
        val email = varchar("email", length = 100).uniqueIndex()
        val displayName = varchar("display_name", length = 100)
        val passwordHash = varchar("password_hash", length = 255)
        val createdAt = long("created_at")
        val updatedAt = long("updated_at")

        override val primaryKey = PrimaryKey(id)
    }

    object RefreshTokens : Table("refresh_tokens") {
        val id = uuid("id").uniqueIndex()
        val userId = reference("user_id", Users.id)
        val token = varchar("token", 1000).uniqueIndex()
        val expiresAt = long("expires_at")
        val issuedAt = long("issuedAt").clientDefault { System.currentTimeMillis() }

        override val primaryKey = PrimaryKey(id)
    }
}