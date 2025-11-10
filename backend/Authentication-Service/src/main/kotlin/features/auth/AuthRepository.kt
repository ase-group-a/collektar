package com.collektar.features.auth

import com.collektar.shared.database.Tables.RefreshTokens
import com.collektar.shared.database.Tables.Users
import com.collektar.shared.repository.BaseRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.*

class AuthRepository(database: Database) : BaseRepository(database), IAuthRepository {
    override suspend fun <T> transaction(block: suspend () -> T): T = dbTransaction(block)

    override suspend fun createUser(
        userId: UUID,
        username: String,
        email: String,
        displayName: String,
        passwordHash: String
    ): AuthModel = dbQuery {
        val now = System.currentTimeMillis()
        val id: UUID = Users.insert {
            it[id] = userId
            it[Users.username] = username
            it[Users.email] = email
            it[Users.displayName] = displayName
            it[Users.passwordHash] = passwordHash
            it[createdAt] = now
            it[updatedAt] = now
        }[Users.id]

        AuthModel(
            id = id,
            username = username, email = email,
            displayName = displayName,
            passwordHash = passwordHash
        )
    }

    override suspend fun findByUsername(username: String): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.username eq username }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq userId }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun usernameExists(username: String): Boolean = dbQuery {
        Users
            .selectAll()
            .where { Users.username eq username }
            .count() > 0
    }

    override suspend fun emailExists(email: String): Boolean = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .count() > 0
    }

    override suspend fun saveRefreshToken(
        userId: UUID,
        token: String,
        expiresAt: Instant,
        issuedAt: Instant
    ): Unit = dbQuery {
        RefreshTokens
            .insert {
                it[RefreshTokens.id] = UUID.randomUUID()
                it[RefreshTokens.userId] = userId
                it[RefreshTokens.token] = token
                it[RefreshTokens.expiresAt] = expiresAt.toEpochMilli()
                it[RefreshTokens.issuedAt] = issuedAt.toEpochMilli()
            }
    }

    private fun ResultRow.toAuthModel() = AuthModel(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        displayName = this[Users.displayName],
        passwordHash = this[Users.passwordHash],
    )
}