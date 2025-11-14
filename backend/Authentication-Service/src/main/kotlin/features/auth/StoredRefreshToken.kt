package com.collektar.features.auth

import java.time.Instant
import java.util.*

data class StoredRefreshToken(
    val tokenHash: String,
    val userId: UUID,
    val expiresAt: Instant,
    val issuedAt: Instant,
)
