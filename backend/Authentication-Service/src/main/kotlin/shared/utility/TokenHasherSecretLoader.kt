package com.collektar.shared.utility

import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.crypto.spec.SecretKeySpec

object TokenHasherSecretLoader {
    fun loadSecret(secretPath: String, algorithm: String): SecretKeySpec {
        val secret = loadKeySpec(secretPath)
        return SecretKeySpec(secret, algorithm)
    }

    private fun loadKeySpec(secretPath: String): ByteArray {
        val secret = Files.readString(Paths.get(secretPath))
        return Base64.getDecoder().decode(secret)
    }
}