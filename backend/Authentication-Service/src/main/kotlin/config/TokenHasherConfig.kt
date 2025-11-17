package com.collektar.config

import com.collektar.shared.utility.TokenHasherSecretLoader
import javax.crypto.spec.SecretKeySpec

data class TokenHasherConfig(
    val secret: SecretKeySpec,
    val algorithm: String = ALGORITHM,
) {
    companion object {
        private const val ALGORITHM = "HmacSHA256"

        fun fromEnv(): TokenHasherConfig {
            return TokenHasherConfig(
                secret = TokenHasherSecretLoader.loadSecret(env("TOKEN_HASHER_SECRET_PATH"), ALGORITHM),
            )
        }

        private fun env(name: String): String {
            return System.getenv(name) ?: error("Missing environment variable: $name")
        }
    }
}
