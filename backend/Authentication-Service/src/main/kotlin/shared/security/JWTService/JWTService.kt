package com.collektar.shared.security.JWTService


import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.collektar.config.JWTConfig
import java.util.*

class JWTService(private val config: JWTConfig) : IJWTService {
    private val algorithm = Algorithm.RSA256(config.publicKey, config.privateKey)
    private val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()

    override fun generateAccessToken(userId: UUID, email: String): AccessToken {
        val expiresAt = Date(System.currentTimeMillis() + config.accessTokenValidityMS)
        val issuedAt = Date()
        val token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId.toString())
            .withClaim("email", email)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)

        return AccessToken(
            token = token,
            expiresAt = expiresAt.toInstant(),
            issuedAt = issuedAt.toInstant(),
            userId = userId,
            email = email
        )
    }

    override fun generateRefreshToken(userId: UUID): RefreshToken {
        val issuedAt = Date()
        val expiresAt = Date(System.currentTimeMillis() + config.refreshTokenValidityMS)
        val token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.toString())
            .withClaim("type", "refresh")
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)

        return RefreshToken(
            token = token,
            expiresAt = expiresAt.toInstant(),
            issuedAt = issuedAt.toInstant(),
            userId = userId,
        )
    }

    override fun verify(token: String): DecodedJWT? {
        return try {
            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }
}