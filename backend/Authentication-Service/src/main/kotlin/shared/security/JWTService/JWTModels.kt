package com.collektar.shared.security.JWTService

import java.time.Instant
import java.util.*

data class AccessToken(
    val token: String,
    val expiresAt: Instant,
    val issuedAt: Instant,
    val userId: UUID,
    val email: String
)

data class RefreshToken(
    val token: String,
    val expiresAt: Instant,
    val issuedAt: Instant,
    val userId: UUID
)
