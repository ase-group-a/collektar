package com.collektar.shared.security.passwordhasher

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BCryptHasherTest {
    private lateinit var passwordHasher: BCryptHasher

    @BeforeEach
    fun setup() {
        passwordHasher = BCryptHasher()
    }

    @Test
    fun shouldCreateValidBcryptHash() {
        val password = "MySecurePassword123!"

        val hash = passwordHasher.hash(password)

        assertNotNull(hash)
        assertTrue(hash.startsWith("$2a$"))
        assertTrue(hash.length >= 59)
    }

    @Test
    fun shouldCreateDifferentHashForSameInput() {
        val password = "SamePassword123"

        val hash1 = passwordHasher.hash(password)
        val hash2 = passwordHasher.hash(password)

        assertNotEquals(hash1, hash2)
    }

    @Test
    fun shouldReturnTrueForCorrectPassword() {
        val password = "CorrectPassword456"
        val hash = passwordHasher.hash(password)

        val result = passwordHasher.verify(password, hash)

        assertTrue(result)
    }

    @Test
    fun shouldReturnFalseForIncorrectPassword() {
        val correctPassword = "CorrectPassword789"
        val incorrectPassword = "WrongPassword789"
        val hash = passwordHasher.hash(correctPassword)

        val result = passwordHasher.verify(incorrectPassword, hash)

        assertFalse(result)
    }
}