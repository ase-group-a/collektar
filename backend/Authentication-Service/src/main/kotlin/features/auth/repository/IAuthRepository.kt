package com.collektar.features.auth.repository

import com.collektar.shared.security.tokenservice.PasswordResetTokenData
import java.time.Instant
import java.util.*

interface IAuthRepository {
    suspend fun createUser(
        userId: UUID,
        username: String,
        email: String,
        displayName: String,
        passwordHash: String
    ): AuthModel?

    suspend fun findByUsername(username: String): AuthModel?
    suspend fun findByEmail(email: String): AuthModel?
    suspend fun findByUserId(userId: UUID): AuthModel?
    suspend fun usernameExists(username: String): Boolean
    suspend fun emailExists(email: String): Boolean
    suspend fun saveRefreshToken(userId: UUID, tokenHash: String, expiresAt: Instant, issuedAt: Instant)
    suspend fun findRefreshToken(tokenHash: String): StoredRefreshToken?
    suspend fun updateLastUsed(tokenHash: String)
    suspend fun revokeRefreshToken(tokenHash: String)
    suspend fun revokeAllUserTokens(userId: UUID)
    suspend fun deleteUser(userId: UUID)
    suspend fun updatePassword(userId: UUID, newPasswordHash: String)
    suspend fun savePasswordResetToken(userId: UUID, tokenHash: String, expiresAt: Instant): UUID
    suspend fun findPasswordResetToken(tokenHash: String): PasswordResetTokenData?
    suspend fun markPasswordResetTokenAsUsed(tokenId: UUID)
    suspend fun deletePasswordResetTokensOfUser(userId: UUID)
}