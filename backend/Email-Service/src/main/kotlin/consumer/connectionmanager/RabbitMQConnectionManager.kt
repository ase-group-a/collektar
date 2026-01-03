package com.collektar.consumer.connectionmanager

import com.collektar.config.RabbitMQConfig
import com.collektar.consumer.connectionfactory.IRabbitMQConnectionFactory
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection

class RabbitMQConnectionManager(
    private val config: RabbitMQConfig,
    private val connectionFactory: IRabbitMQConnectionFactory
): IRabbitMQConnectionManager {
    private var connection: Connection? = null
    private var channel: Channel? = null

    override fun connect(): Channel {
        channel
            ?.takeIf { it.isOpen }
            ?.let { return it }

        val newConnection = connectionFactory.createConnection()
        val newChannel = newConnection.createChannel().apply {
            queueDeclare(config.queueName, true, false, false, null)
            basicQos(1)
        }

        connection = newConnection
        channel = newChannel
        return newChannel
    }

    override fun close() {
        channel?.close()
        connection?.close()
    }
}