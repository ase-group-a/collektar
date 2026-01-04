package com.collektar.config

import com.ase.shared.utility.RSAKeyLoader
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

data class JWTConfig(
    val privateKey: RSAPrivateKey,
    val publicKey: RSAPublicKey,
    val issuer: String,
    val audience: String,
    val realm: String,
    val accessTokenValidityMS: Long = 3600000,
    val refreshTokenValidityMS: Long = 3600000,
) {
    companion object {
        fun fromEnv(): JWTConfig {
            return JWTConfig(
                privateKey = RSAKeyLoader.loadPrivateKey(env("JWT_PRIVATE_KEY_PATH")),
                publicKey = RSAKeyLoader.loadPublicKey(env("JWT_PUBLIC_KEY_PATH")),
                issuer = env("JWT_ISSUER"),
                audience = env("JWT_AUDIENCE"),
                realm = env("JWT_REALM"),
                accessTokenValidityMS = env("JWT_ACCESS_TOKEN_VALIDITY").toLong(),
                refreshTokenValidityMS = env("JWT_REFRESH_TOKEN_VALIDITY").toLong(),
            )
        }

        private fun env(name: String): String {
            return System.getenv(name) ?: error("Missing environment variable: $name")
        }
    }
}

