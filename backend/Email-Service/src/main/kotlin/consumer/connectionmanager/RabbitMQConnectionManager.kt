package com.collektar.consumer.connectionmanager

import com.collektar.config.RabbitMQConfig
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class RabbitMQConnectionManager(
    private val config: RabbitMQConfig
) {
    private var connection: Connection? = null
    private var channel: Channel? = null

    fun connect(): Channel {
        channel
            ?.takeIf { it.isOpen }
            ?.let { return it }

        val factory = createConnectionFactory()
        val newConnection = factory.newConnection()
        val newChannel = newConnection.createChannel().apply {
            queueDeclare(config.queueName, true, false, false, null)
            basicQos(1)
        }

        connection = newConnection
        channel = newChannel
        return newChannel
    }

    private fun createConnectionFactory() = ConnectionFactory().apply {
        host = config.host
        port = config.port
        username = config.user
        password = config.password
        isAutomaticRecoveryEnabled = true
        networkRecoveryInterval = 10000
    }

    fun close() {
        channel?.close()
        connection?.close()
    }
}