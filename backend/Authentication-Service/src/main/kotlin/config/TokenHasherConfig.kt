package com.collektar.config

data class TokenHasherConfig(
    val secret: String,
    val algorithm: String = "HmacSHA256"
) {
    companion object {
        fun fromEnv(): TokenHasherConfig {
            return TokenHasherConfig(
                secret = env("TOKEN_HASHER_SECRET")
            )
        }

        private fun env(name: String): String {
            return System.getenv(name) ?: error("Missing environment variable: $name")
        }
    }
}
