package com.collektar.shared.security.tokenhasher

import com.collektar.config.TokenHasherConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals

class HmacTokenHasherTest {
    private lateinit var config: TokenHasherConfig
    private lateinit var tokenHasher: HmacTokenHasher
    private lateinit var secretKey: SecretKeySpec

    @BeforeEach
    fun setup() {
        val secret = "test-secret"
        val secretBytes = Base64.getEncoder().encode(secret.toByteArray())
        secretKey = SecretKeySpec(Base64.getDecoder().decode(secretBytes), "HmacSHA256")

        config = TokenHasherConfig(
            secret = secretKey,
        )
        tokenHasher = HmacTokenHasher(config)
    }

    @Test
    fun shouldCreateDeterministicHash() {
        val token = "refresh_token"
        val hash1 = tokenHasher.hash(token)
        val hash2 = tokenHasher.hash(token)

        assertEquals(hash1, hash2)
    }
}