package com.collektar.features.auth.repository

import java.time.Instant
import java.util.*

data class AuthModel(
    val id: UUID,
    val username: String,
    val email: String,
    val displayName: String,
    val passwordHash: String
)

data class StoredRefreshToken(
    val tokenHash: String,
    val userId: UUID,
    val expiresAt: Instant,
    val issuedAt: Instant,
)
