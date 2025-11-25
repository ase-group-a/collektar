package com.collektar.shared.security.jwt

import com.collektar.config.JWTConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*
import kotlin.test.assertTrue

class JWTServiceTest {
    private lateinit var config: JWTConfig
    private lateinit var jwtService: JWTService
    private lateinit var privateKey: RSAPrivateKey
    private lateinit var publicKey: RSAPublicKey


    @BeforeEach
    fun setup() {
        val keypairGen = KeyPairGenerator.getInstance("RSA")
        keypairGen.initialize(2048)
        val keyPair = keypairGen.genKeyPair()
        privateKey = keyPair.private as RSAPrivateKey
        publicKey = keyPair.public as RSAPublicKey

        config = JWTConfig(
            privateKey = privateKey,
            publicKey = publicKey,
            issuer = "test-issuer",
            audience = "test-audience",
            realm = "test-realm",
            accessTokenValidityMS = 3600000L
        )

        jwtService = JWTService(config)
    }

    @Test
    fun shouldSetCorrectClaims() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val token = jwtService.generateAccessToken(userId, email)

        assertNotNull(token)
        assertTrue(token.token.isNotEmpty())
        assertEquals(userId, token.userId)
        assertEquals(email, token.email)
    }

    @Test
    fun shouldSetCorrectExpiration() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val before = Instant.now()

        val token = jwtService.generateAccessToken(userId, email)

        val after = Instant.now()
        val expectedExpiration = before.plusMillis(config.accessTokenValidityMS)

        assertTrue(token.expiresAt.isAfter(expectedExpiration.minusSeconds(1)))
        assertTrue(token.expiresAt.isBefore(after.plusMillis(config.accessTokenValidityMS + 1000)))
    }

    @Test
    fun shouldSetCorrectIssuedAt() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val before = Instant.now()

        val token = jwtService.generateAccessToken(userId, email)

        val after = Instant.now()

        assertTrue(token.issuedAt.isAfter(before.minusSeconds(1)))
        assertTrue(token.issuedAt.isBefore(after.plusSeconds(1)))
    }

    @Test
    fun shouldCreateDifferentTokenForSameUser() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val token1 = jwtService.generateAccessToken(userId, email)
        Thread.sleep(1000)
        val token2 = jwtService.generateAccessToken(userId, email)

        assertNotEquals(token1.token, token2.token)
    }

    @Test
    fun shouldReturnDecodedTokenIfValid() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"
        val accessToken = jwtService.generateAccessToken(userId, email)

        val decoded = jwtService.verify(accessToken.token)

        assertNotNull(decoded)
        assertEquals(config.issuer, decoded.issuer)
        assertEquals(config.audience, decoded.audience.first())
        assertEquals(userId.toString(), decoded.getClaim("userId").asString())
        assertEquals(email, decoded.getClaim("email").asString())
        assertEquals("access", decoded.getClaim("type").asString())
    }

    @Test
    fun shouldReturnNullForExpiredToken() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val expiredConfig = JWTConfig(
            publicKey = publicKey,
            privateKey = privateKey,
            issuer = config.issuer,
            audience = config.audience,
            realm = config.realm,
            accessTokenValidityMS = -100000L
        )

        val expiredJwtService = JWTService(expiredConfig)
        val expiredToken = expiredJwtService.generateAccessToken(userId, email)

        val decoded = jwtService.verify(expiredToken.token)

        assertNull(decoded)
    }

    @Test
    fun shouldReturnNullIfSigIsInvalid() {
        val userId = UUID.randomUUID()
        val email = "test@example.com"

        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val differentKeyPair = keyPairGenerator.generateKeyPair()

        val differentConfig = JWTConfig(
            publicKey = differentKeyPair.public as RSAPublicKey,
            privateKey = differentKeyPair.private as RSAPrivateKey,
            issuer = config.issuer,
            audience = config.audience,
            realm = config.realm,
            accessTokenValidityMS = 3600000L
        )
        val differentJwtService = JWTService(differentConfig)
        val tokenWithDifferentKey = differentJwtService.generateAccessToken(userId, email)

        val decoded = jwtService.verify(tokenWithDifferentKey.token)

        assertNull(decoded)
    }

    @Test
    fun shouldReturnNullForMalformedToken() {
        val malformedToken = "not.a.valid.jwt.token"

        val decoded = jwtService.verify(malformedToken)

        assertNull(decoded)
    }

    @Test
    fun shouldReturnNullForEmptyToken() {
        val emptyToken = ""

        val decoded = jwtService.verify(emptyToken)

        assertNull(decoded)
    }
}














