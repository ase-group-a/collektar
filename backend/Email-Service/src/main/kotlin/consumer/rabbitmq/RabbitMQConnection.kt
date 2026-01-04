package com.collektar.consumer.rabbitmq

import com.collektar.config.RabbitMQConfig
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.ktor.utils.io.core.*

class RabbitMQConnection(
    private val config: RabbitMQConfig,
    private val factory: ConnectionFactory = ConnectionFactory()
) : Closeable {
    private var connection: Connection? = null
    private var channel: Channel? = null

    fun channel(): Channel {
        channel?.takeIf { it.isOpen }?.let { return it }

        runCatching { channel?.close() }
        runCatching { connection?.close() }

        channel = null
        connection = null

        factory.apply {
            host = config.host
            port = config.port
            username = config.user
            password = config.password
            isAutomaticRecoveryEnabled = true
            networkRecoveryInterval = 5000
        }

        connection = factory.newConnection()
        channel = connection!!.createChannel().apply {
            queueDeclare(config.queueName, true, false, false, null)
            basicQos(1)
        }

        return channel!!
    }

    override fun close() {
        runCatching { channel?.close() }
        runCatching { connection?.close() }
        channel = null
        connection = null
    }
}