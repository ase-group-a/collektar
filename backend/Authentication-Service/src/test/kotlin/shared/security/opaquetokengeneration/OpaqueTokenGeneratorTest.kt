package com.collektar.shared.security.opaquetokengeneration

import com.collektar.config.OpaqueTokenConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class OpaqueTokenGeneratorTest {
    private lateinit var config: OpaqueTokenConfig
    private lateinit var tokenGenerator: OpaqueTokenGenerator
    private lateinit var userId: UUID

    @BeforeEach
    fun setup() {
        config = OpaqueTokenConfig()
        tokenGenerator = OpaqueTokenGenerator(config)
        userId = UUID.randomUUID()
    }

    @Test
    fun shouldCreateTokenWithCorrectUserId() {
        val token = tokenGenerator.generate(userId)

        assertEquals(userId, token.userId)
    }

    @Test
    fun shouldCreateNonEmptyTokenString() {
        val token = tokenGenerator.generate(userId)

        assertFalse(token.token.isEmpty())
    }

    @Test
    fun shouldCreateUniqueTokens() {
        val token1 = tokenGenerator.generate(userId)
        val token2 = tokenGenerator.generate(userId)

        assertNotEquals(token1.token, token2.token)
    }

    @Test
    fun shouldSetExpiresAtCorrectly() {
        val beforeGeneration = Instant.now()

        val token = tokenGenerator.generate(userId)

        val afterGeneration = Instant.now()
        val expectedExpiresAt = beforeGeneration.plusMillis(config.refreshTokenValidityMS)

        assertTrue(token.expiresAt.isAfter(expectedExpiresAt.minusSeconds(1)))
        assertTrue(token.expiresAt.isBefore(afterGeneration.plusMillis(config.refreshTokenValidityMS + 1000)))
    }

    @Test
    fun shouldSetIssuedAtToCurrentTime() {
        val before = Instant.now()

        val token = tokenGenerator.generate(userId)

        val after = Instant.now()
        assertTrue(token.issuedAt.isAfter(before.minusSeconds(1)))
        assertTrue(token.issuedAt.isBefore(after.plusSeconds(1)))
    }
}