package com.collektar.shared.security.tokenservice

import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.features.auth.IAuthRepository
import com.collektar.features.auth.StoredRefreshToken
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.JWTService.AccessToken
import com.collektar.shared.security.JWTService.IJWTService
import com.collektar.shared.security.JWTService.RefreshToken
import com.collektar.shared.security.RefreshTokenHasher.IRefreshTokenHasher
import com.collektar.shared.security.opaquetokengeneration.IOpaqueTokenGenerator
import java.time.Duration
import java.time.Instant
import java.util.*

class TokenService(
    private val jwtService: IJWTService,
    private val tokenHasher: IRefreshTokenHasher,
    private val opaqueTokenGenerator: IOpaqueTokenGenerator,
    private val repository: IAuthRepository,
) : ITokenService {
    override suspend fun generateTokens(userId: UUID, email: String): TokenPair {
        val accessToken: AccessToken = jwtService.generateAccessToken(userId, email)
        val refreshToken: RefreshToken = opaqueTokenGenerator.generate(userId)

        val accessTokenExpiresIn: Long = Duration.between(Instant.now(), accessToken.expiresAt).toMillis()
        val refreshTokenExpiresIn: Long = Duration.between(Instant.now(), refreshToken.expiresAt).toMillis()

        saveRefreshToken(refreshToken)

        return TokenPair(
            accessToken = accessToken.token,
            refreshToken = refreshToken.token,
            accessTokenExpiresIn = accessTokenExpiresIn,
            refreshTokenExpiresIn = refreshTokenExpiresIn
        )
    }

    override suspend fun validateAndRefresh(token: String): TokenPair {
        val (userId: UUID, tokenHash: String) = validateRefreshToken(token)

        repository.revokeRefreshToken(tokenHash)
        val user = repository.findByUserId(
            userId = userId
        ) ?: throw AppError.Unauthorized.InvalidToken()

        return generateTokens(userId = user.id, email = user.email)
    }

    override suspend fun validateAccessToken(token: String): TokenClaims {
        val decodedToken: DecodedJWT = jwtService.verify(
            token
        ) ?: throw AppError.Unauthorized.InvalidToken()

        val tokenType = decodedToken.getClaim("type").asString()
        if (tokenType != "access") {
            throw AppError.Unauthorized.InvalidToken()
        }

        val userIdString = decodedToken.getClaim("userId").asString()
            ?: throw AppError.Unauthorized.InvalidToken()
        val email = decodedToken.getClaim("email").asString()
            ?: throw AppError.Unauthorized.InvalidToken()

        val userId = try {
            UUID.fromString(userIdString)
        } catch (_: Exception) {
            throw AppError.Unauthorized.InvalidToken()
        }

        return TokenClaims(
            userId = userId,
            email = email
        )
    }

    private suspend fun saveRefreshToken(token: RefreshToken) {
        val tokenHash = tokenHasher.hash(token.token)

        repository.saveRefreshToken(
            userId = token.userId,
            tokenHash = tokenHash,
            expiresAt = token.expiresAt,
            issuedAt = token.issuedAt
        )
    }

    private suspend fun validateRefreshToken(token: String): Pair<UUID, String> {
        val tokenHash = tokenHasher.hash(token)

        val storedToken: StoredRefreshToken = repository.findRefreshToken(
            tokenHash = tokenHash
        ) ?: throw AppError.Unauthorized.InvalidToken()

        if (isTokenExpired(storedToken)) {
            repository.revokeRefreshToken(tokenHash)
            throw AppError.Unauthorized.InvalidToken()
        }

        repository.updateLastUsed(tokenHash)

        return storedToken.userId to tokenHash
    }

    private fun isTokenExpired(token: StoredRefreshToken): Boolean {
        return Instant.now().isAfter(token.expiresAt)
    }
}