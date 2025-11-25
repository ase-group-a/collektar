package com.collektar.shared.utility

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.crypto.KeyGenerator
import kotlin.test.assertEquals
import kotlin.test.assertFails

class TokenHasherSecretLoaderTest {
    @TempDir
    lateinit var tempDir: Path

    private fun generateSecret(): ByteArray {
        val keyGen = KeyGenerator.getInstance("HmacSHA256").apply {
            init(256)
        }
        return keyGen.generateKey().encoded
    }

    private fun writeSecretFile(path: Path, secretBytes: ByteArray) {
        val encoded = Base64.getEncoder().encodeToString(secretBytes)
        Files.writeString(path, encoded)
    }

    @Test
    fun shouldLoadValidSecret() {
        val secretBytes = generateSecret()
        val secretFile = tempDir.resolve("secret.txt")
        writeSecretFile(secretFile, secretBytes)

        val loaded = TokenHasherSecretLoader.loadSecret(
            secretFile.toString(),
            "HmacSHA256"
        )

        assertEquals("HmacSHA256", loaded.algorithm)
        assertEquals(secretBytes.toList(), loaded.encoded.toList())
    }

    @Test
    fun shouldLoadSecretWithDifferentAlgorithm() {
        val secretBytes = generateSecret()
        val secretFile = tempDir.resolve("secret.txt")
        writeSecretFile(secretFile, secretBytes)

        val loaded = TokenHasherSecretLoader.loadSecret(
            secretFile.toString(),
            "HmacSHA512"
        )

        assertEquals("HmacSHA512", loaded.algorithm)
        assertEquals(secretBytes.toList(), loaded.encoded.toList())
    }

    @Test
    fun shouldThrowIfSecretDoesntExist() {
        assertFails {
            TokenHasherSecretLoader.loadSecret(
                "nonexistent_secret.txt",
                "HmacSHA256"
            )
        }
    }

    @Test
    fun shouldThrowIfSecretDataInvalid() {
        val secretFile = tempDir.resolve("invalid.txt")
        Files.writeString(secretFile, "this is not valid base64")

        assertFails {
            TokenHasherSecretLoader.loadSecret(
                secretFile.toString(),
                "HmacSHA256"
            )
        }
    }

    @Test
    fun shouldThrowIfSecretIsEmpty() {
        val secretFile = tempDir.resolve("empty.txt")
        Files.writeString(secretFile, "")

        assertFails {
            TokenHasherSecretLoader.loadSecret(
                secretFile.toString(),
                "HmacSHA256"
            )
        }
    }
}