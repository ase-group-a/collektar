package com.collektar.shared.security.tokenservice

import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.features.auth.repository.StoredRefreshToken
import com.collektar.shared.errors.AppError
import com.collektar.shared.security.jwt.AccessToken
import com.collektar.shared.security.jwt.IJWTService
import com.collektar.shared.security.jwt.RefreshToken
import com.collektar.shared.security.opaquetokengeneration.IOpaqueTokenGenerator
import com.collektar.shared.security.tokenhasher.IRefreshTokenHasher
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

    override suspend fun revokeRefreshToken(token: String) {
        val tokenHash = tokenHasher.hash(token)
        repository.revokeRefreshToken(tokenHash)
    }

    override suspend fun generatePasswordResetToken(userId: UUID): PasswordResetToken {
        val rawToken = opaqueTokenGenerator.generateRaw()
        val tokenHash = tokenHasher.hash(rawToken)
        val expiresAt = Instant.now().plusSeconds(30 * 60L)

        repository.savePasswordResetToken(
            userId = userId,
            tokenHash = tokenHash,
            expiresAt = expiresAt
        )

        return PasswordResetToken(
            token = rawToken,
            expiresAt = expiresAt,
        )
    }

    override suspend fun validatePasswordResetToken(token: String): PasswordResetTokenClaims {
        val tokenHash = tokenHasher.hash(token)

        val storedToken = repository.findPasswordResetToken(tokenHash)
            ?: throw AppError.BadRequest.InvalidOrExpiredToken()

        if (Instant.now().isAfter(storedToken.expiresAt)) {
            throw AppError.BadRequest.InvalidOrExpiredToken()
        }

        if (storedToken.usedAt != null) {
            throw AppError.BadRequest.InvalidOrExpiredToken()
        }

        return PasswordResetTokenClaims(
            userId = storedToken.userId,
            tokenId = storedToken.id
        )
    }

    override suspend fun consumePasswordResetToken(token: String): PasswordResetTokenClaims {
        val claims = validatePasswordResetToken(token)
        repository.markPasswordResetTokenAsUsed(claims.tokenId)
        return claims
    }

    override suspend fun invalidatePasswordResetTokens(userId: UUID) {
        repository.deletePasswordResetTokensOfUser(userId)
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