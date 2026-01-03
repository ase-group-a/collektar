package com.collektar.consumer.connectionmanager

import com.rabbitmq.client.Channel

interface IRabbitMQConnectionManager {
    fun connect(): Channel
    fun close()
}