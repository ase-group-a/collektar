package com.collektar.shared.security.tokenservice

import java.util.*

interface ITokenService {
    suspend fun generateTokens(userId: UUID, email: String): TokenPair
    suspend fun validateAndRefresh(token: String): TokenPair
    suspend fun validateAccessToken(token: String): TokenClaims
    suspend fun revokeRefreshToken(token: String)
    suspend fun generatePasswordResetToken(userId: UUID): PasswordResetToken
    suspend fun validatePasswordResetToken(token: String): PasswordResetTokenClaims
    suspend fun consumePasswordResetToken(token: String): PasswordResetTokenClaims
    suspend fun invalidatePasswordResetTokens(userId: UUID)
}