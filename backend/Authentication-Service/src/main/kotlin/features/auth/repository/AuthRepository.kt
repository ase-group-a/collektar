package com.collektar.features.auth.repository

import com.collektar.shared.database.Tables.PasswordResetTokens
import com.collektar.shared.database.Tables.RefreshTokens
import com.collektar.shared.database.Tables.Users
import com.collektar.shared.repository.BaseRepository
import com.collektar.shared.security.tokenservice.PasswordResetTokenData
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class AuthRepository(database: Database) : BaseRepository(database), IAuthRepository {
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
        tokenHash: String,
        expiresAt: Instant,
        issuedAt: Instant
    ): Unit = dbQuery {
        RefreshTokens
            .insert {
                it[id] = UUID.randomUUID()
                it[RefreshTokens.userId] = userId
                it[this.tokenHash] = tokenHash
                it[RefreshTokens.expiresAt] = expiresAt.toEpochMilli()
                it[RefreshTokens.issuedAt] = issuedAt.toEpochMilli()
                it[lastUsedAt] = System.currentTimeMillis()
            }
    }

    override suspend fun findRefreshToken(tokenHash: String): StoredRefreshToken? = dbQuery {
        RefreshTokens
            .selectAll()
            .where { RefreshTokens.tokenHash eq tokenHash }
            .map { it.toStoredRefreshToken() }
            .singleOrNull()
    }

    override suspend fun revokeRefreshToken(tokenHash: String): Unit = dbQuery {
        RefreshTokens.deleteWhere { RefreshTokens.tokenHash eq tokenHash }
    }

    override suspend fun revokeAllUserTokens(userId: UUID): Unit = dbQuery {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
    }

    override suspend fun updateLastUsed(tokenHash: String): Unit = dbQuery {
        RefreshTokens.update({
            RefreshTokens.tokenHash eq tokenHash
        }) {
            it[lastUsedAt] = System.currentTimeMillis()
        }
    }

    override suspend fun deleteUser(userId: UUID): Unit = dbQuery {
        PasswordResetTokens.deleteWhere { PasswordResetTokens.userId eq userId }
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
        Users.deleteWhere { id eq userId }
    }

    override suspend fun updatePassword(userId: UUID, newPasswordHash: String): Unit = dbQuery {
        Users.update({
            Users.id eq userId
        }) {
            it[passwordHash] = newPasswordHash
            it[updatedAt] = Instant.now().toEpochMilli()
        }
    }

    override suspend fun savePasswordResetToken(userId: UUID, tokenHash: String, expiresAt: Instant): UUID = dbQuery {
        val tokenId = UUID.randomUUID()
        val now = Instant.now().toEpochMilli()

        PasswordResetTokens.insert {
            it[id] = tokenId
            it[PasswordResetTokens.userId] = userId
            it[PasswordResetTokens.tokenHash] = tokenHash
            it[PasswordResetTokens.expiresAt] = expiresAt.toEpochMilli()
            it[usedAt] = null
            it[createdAt] = now
        }

        tokenId
    }

    override suspend fun findPasswordResetToken(tokenHash: String): PasswordResetTokenData? = dbQuery {
        val now = Instant.now()
        PasswordResetTokens
            .selectAll()
            .where {
                (PasswordResetTokens.tokenHash eq tokenHash) and (PasswordResetTokens.expiresAt greater now.toEpochMilli()) and (PasswordResetTokens.usedAt.isNull())
            }
            .map {
                PasswordResetTokenData(
                    id = it[PasswordResetTokens.id],
                    userId = it[PasswordResetTokens.userId],
                    tokenHash = it[PasswordResetTokens.tokenHash],
                    expiresAt = Instant.ofEpochMilli(it[PasswordResetTokens.expiresAt]),
                    usedAt = it[PasswordResetTokens.usedAt]?.let { usedAt -> Instant.ofEpochMilli(usedAt) },
                    createdAt = Instant.ofEpochMilli(it[PasswordResetTokens.createdAt])
                )
            }
            .singleOrNull()
    }

    override suspend fun markPasswordResetTokenAsUsed(tokenId: UUID): Unit = dbQuery {
        PasswordResetTokens.update({
            PasswordResetTokens.id eq tokenId
        }) {
            it[usedAt] = Instant.now().toEpochMilli()
        }
    }

    override suspend fun deletePasswordResetTokensOfUser(userId: UUID): Unit = dbQuery {
        PasswordResetTokens.deleteWhere { (PasswordResetTokens.userId eq userId) }
    }

    private fun ResultRow.toAuthModel() = AuthModel(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        displayName = this[Users.displayName],
        passwordHash = this[Users.passwordHash],
    )

    private fun ResultRow.toStoredRefreshToken() = StoredRefreshToken(
        tokenHash = this[RefreshTokens.tokenHash],
        userId = this[RefreshTokens.userId],
        expiresAt = Instant.ofEpochMilli(this[RefreshTokens.expiresAt]),
        issuedAt = Instant.ofEpochMilli(this[RefreshTokens.issuedAt])
    )
}