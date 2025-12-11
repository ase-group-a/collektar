package com.collektar.config

data class EmailProviderConfig(
    val username: String,
    val password: String,
    val host: String,
    val port: String,
    val fromEmail: String,
    val fromName: String,
) : BaseConfig() {
    companion object {
        fun fromEnv(): EmailProviderConfig {
            return EmailProviderConfig(
                username = env("SMTP_USERNAME"),
                password = env("SMTP_PASSWORD"),
                host = env("SMTP_HOST"),
                port = env("SMTP_PORT"),
                fromEmail = env("SMTP_FROM_EMAIL"),
                fromName = env("SMTP_FROM_NAME"),
            )
        }
    }
}
