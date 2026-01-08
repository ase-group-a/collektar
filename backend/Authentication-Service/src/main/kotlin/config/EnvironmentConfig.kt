package com.collektar.config

data class EnvironmentConfig(
    val appBaseUrl: String
) : BaseConfig() {
    companion object {
        fun fromEnv(): EnvironmentConfig {
            return EnvironmentConfig(
                appBaseUrl = env("DOMAIN")
            )
        }
    }
}
