package com.collektar.consumer.connectionfactory

import com.rabbitmq.client.Connection

interface IRabbitMQConnectionFactory {
    fun createConnection(): Connection
}