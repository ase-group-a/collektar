package com.collektar.shared.security.tokenhasher

import com.collektar.config.TokenHasherConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class HmacTokenHasherTest {
    private lateinit var config: TokenHasherConfig
    private lateinit var tokenHasher: HmacTokenHasher

    @BeforeEach
    fun setup() {
        config = TokenHasherConfig(
            algorithm = "HmacSHA256",
            secret = "test-secret"
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