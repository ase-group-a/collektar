package com.collektar.features.auth

import java.time.Instant
import java.util.*

interface IAuthRepository {
    suspend fun <T> transaction(block: suspend () -> T): T
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
    suspend fun saveRefreshToken(userId: UUID, token: String, expiresAt: Instant, issuedAt: Instant)
}