package com.collektar.shared.security.tokenservice

import java.time.Instant
import java.util.*

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

data class TokenClaims(
    val userId: UUID,
    val email: String
)

data class PasswordResetToken(
    val token: String,
    val expiresAt: Instant
)

data class PasswordResetTokenClaims(
    val userId: UUID,
    val tokenId: UUID
)

data class PasswordResetTokenData(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val expiresAt: Instant,
    val usedAt: Instant?,
    val createdAt: Instant
)
