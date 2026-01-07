package com.collektar.config

data class RabbitMQConfig(
    val host: String,
    val port: Int,
    val user: String,
    val password: String,
    val queueName: String
) : BaseConfig() {
    companion object {
        fun fromEnv(): RabbitMQConfig {
            return RabbitMQConfig(
                host = env("RABBITMQ_HOST"),
                port = env("RABBITMQ_PORT").toInt(),
                user = env("RABBITMQ_USER"),
                password = env("RABBITMQ_PASSWORD"),
                queueName = env("RABBITMQ_QUEUE_EMAIL_TRANSACTIONAL")
            )
        }
    }
}