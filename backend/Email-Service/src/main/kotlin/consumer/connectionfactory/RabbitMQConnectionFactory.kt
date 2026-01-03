package com.collektar.consumer.connectionfactory

import com.collektar.config.RabbitMQConfig
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory

class RabbitMQConnectionFactory(
    private val config: RabbitMQConfig
) : IRabbitMQConnectionFactory {
    override fun createConnection(): Connection {
        return ConnectionFactory().apply {
            host = config.host
            port = config.port
            username = config.user
            password = config.password
            isAutomaticRecoveryEnabled = true
            networkRecoveryInterval = 10000
        }.newConnection()
    }
}