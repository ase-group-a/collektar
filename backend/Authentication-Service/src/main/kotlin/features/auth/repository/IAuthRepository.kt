package com.collektar.features.auth.repository

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

    suspend fun saveRefreshToken(userId: UUID, tokenHash: String, expiresAt: Instant, issuedAt: Instant)
    suspend fun findRefreshToken(tokenHash: String): StoredRefreshToken?
    suspend fun updateLastUsed(tokenHash: String)
    suspend fun revokeRefreshToken(tokenHash: String)
    suspend fun revokeAllUserTokens(userId: UUID)
}