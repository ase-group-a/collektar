package com.collektar.shared.security.tokenservice

import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.features.auth.repository.StoredRefreshToken
import com.collektar.features.user.repository.IUserRepository
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
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository
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

        authRepository.revokeRefreshToken(tokenHash)
        val user = userRepository.findByUserId(
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

        authRepository.saveRefreshToken(
            userId = token.userId,
            tokenHash = tokenHash,
            expiresAt = token.expiresAt,
            issuedAt = token.issuedAt
        )
    }

    private suspend fun validateRefreshToken(token: String): Pair<UUID, String> {
        val tokenHash = tokenHasher.hash(token)

        val storedToken: StoredRefreshToken = authRepository.findRefreshToken(
            tokenHash = tokenHash
        ) ?: throw AppError.Unauthorized.InvalidToken()

        if (isTokenExpired(storedToken)) {
            authRepository.revokeRefreshToken(tokenHash)
            throw AppError.Unauthorized.InvalidToken()
        }

        authRepository.updateLastUsed(tokenHash)

        return storedToken.userId to tokenHash
    }

    private fun isTokenExpired(token: StoredRefreshToken): Boolean {
        return Instant.now().isAfter(token.expiresAt)
    }
}