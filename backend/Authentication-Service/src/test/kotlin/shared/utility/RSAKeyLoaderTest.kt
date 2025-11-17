package com.collektar.shared.utility

import com.ase.shared.utility.RSAKeyLoader
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyPairGenerator
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RSAKeyLoaderTest {
    @TempDir
    lateinit var tempDir: Path

    private fun generateKeyPair() = KeyPairGenerator.getInstance("RSA")
        .apply { initialize(2048) }
        .generateKeyPair()

    private fun writePemFile(path: Path, header: String, footer: String, keyBytes: ByteArray) {
        val encoded = Base64.getEncoder().encodeToString(keyBytes)
        val pem = "$header\n$encoded\n$footer"
        Files.writeString(path, pem)
    }

    @Test
    fun shouldLoadValidPrivateKey() {
        val keyPair = generateKeyPair()
        val keyFile = tempDir.resolve("private.pem")

        writePemFile(
            keyFile,
            "-----BEGIN PRIVATE KEY-----",
            "-----END PRIVATE KEY-----",
            keyPair.private.encoded
        )

        val loaded = RSAKeyLoader.loadPrivateKey(keyFile.toString())

        assertEquals(loaded, keyPair.private)
    }

    @Test
    fun shouldLoadValidPublicKey() {
        val keyPair = generateKeyPair()
        val keyFile = tempDir.resolve("public.pem")

        writePemFile(
            keyFile,
            "-----BEGIN PUBLIC KEY-----",
            "-----END PUBLIC KEY-----",
            keyPair.public.encoded
        )

        val loaded = RSAKeyLoader.loadPublicKey(keyFile.toString())

        assertEquals(loaded, keyPair.public)
    }

    @Test
    fun shouldThrowThrowIFPrivateKeyDoesntExist() {
        assertFails {
            RSAKeyLoader.loadPublicKey("private_key.pem")
        }
    }

    @Test
    fun shouldThrowThrowIFPublicKeyDoesntExist() {
        assertFails {
            RSAKeyLoader.loadPublicKey("private_key.pem")
        }
    }

    @Test
    fun shouldThrowIfKeyDataInvalid() {
        val keyFile = tempDir.resolve("invalid.pem")

        writePemFile(
            keyFile,
            "-----BEGIN PRIVATE KEY-----",
            "-----END PRIVATE KEY-----",
            "invalid data".toByteArray()
        )

        assertFails {
            RSAKeyLoader.loadPrivateKey(keyFile.toString())
        }
    }
}