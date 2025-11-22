package com.collektar.shared.security.tokenservice

import java.util.*

interface ITokenService {
    suspend fun generateTokens(userId: UUID, email: String): TokenPair
    suspend fun validateAndRefresh(token: String): TokenPair
    suspend fun validateAccessToken(token: String): TokenClaims
}