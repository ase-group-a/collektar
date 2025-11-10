package com.ase.shared.utility

import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

object RSAKeyLoader {
    fun loadPrivateKey(pemPath: String): RSAPrivateKey {
        val keySpec = loadKeySpec(pemPath)
        return KeyFactory.getInstance("RSA").generatePrivate(PKCS8EncodedKeySpec(keySpec)) as RSAPrivateKey
    }

    fun loadPublicKey(pemPath: String): RSAPublicKey {
        val keySpec = loadKeySpec(pemPath)
        return KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(keySpec)) as RSAPublicKey
    }

    private fun loadKeySpec(pemPath: String): ByteArray {
        val pem = Files.readString(Paths.get(pemPath)) ?: error("$pemPath is missing.")
        val clean = cleanKey(pem)
        return Base64.getDecoder().decode(clean)
    }

    private fun cleanKey(pem: String): String {
        return pem
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")
    }
}