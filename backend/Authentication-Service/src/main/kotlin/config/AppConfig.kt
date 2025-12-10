package com.collektar.config

data class AppConfig(
    val isProd: Boolean
) {
    companion object {
        fun fromEnv(): AppConfig {
            val isProd = System.getenv("KTOR_ENVIRONMENT") == "production"
            return AppConfig(
                isProd = isProd
            )
        }
    }
}
