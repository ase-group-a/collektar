package com.collektar.shared.producer

import com.collektar.config.RabbitMQConfig
import com.collektar.dto.EmailMessage
import com.collektar.shared.producer.connectionmanager.RabbitMQConnection
import com.rabbitmq.client.MessageProperties
import kotlinx.serialization.json.Json

class EmailPublisher(
    private val connection: RabbitMQConnection,
    private val config: RabbitMQConfig,
    private val json: Json = Json { ignoreUnknownKeys = false }
) : IEmailPublisher {
    override fun publish(emailMessage: EmailMessage): Result<Unit> {
        return try {
            val messageJson = json.encodeToString(emailMessage)
            val messageBytes = messageJson.toByteArray()

            val channel = connection.channel()

            channel.basicPublish(
                "",
                config.queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                messageBytes
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
