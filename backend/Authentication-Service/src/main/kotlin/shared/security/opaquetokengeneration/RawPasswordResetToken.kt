package com.collektar.shared.security.opaquetokengeneration

data class RawPasswordResetToken(
    val rawToken: String,
    val validityInMinutes: Int,
)