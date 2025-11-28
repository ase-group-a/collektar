package com.collektar.shared.security.tokenservice

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