package com.collektar.shared.security.opaquetokengeneration

import com.collektar.config.OpaqueTokenConfig
import com.collektar.shared.security.jwt.RefreshToken
import java.security.SecureRandom
import java.time.Instant
import java.util.*

class OpaqueTokenGenerator(private val config: OpaqueTokenConfig) : IOpaqueTokenGenerator {
    private val secureRandom = SecureRandom()

    override fun generate(userId: UUID): RefreshToken {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        val expiresAt = Date(System.currentTimeMillis() + config.refreshTokenValidityMS)

        return RefreshToken(
            token = token,
            expiresAt = expiresAt.toInstant(),
            issuedAt = Instant.now(),
            userId = userId
        )
    }

    override fun generateRaw(byteLength: Int): RawPasswordResetToken {
        val bytes = ByteArray(byteLength)
        secureRandom.nextBytes(bytes)
        val rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

        return RawPasswordResetToken(
            rawToken = rawToken,
            validityInMinutes = config.passwordResetTokenValidityMinutes
        )
    }
}