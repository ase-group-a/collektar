package com.collektar.consumer

import com.collektar.config.RabbitMQConfig
import com.collektar.consumer.processor.IMessageProcessor
import com.collektar.consumer.rabbitmq.ChannelConsumer
import com.collektar.consumer.rabbitmq.RabbitMQConnection
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*


class EmailConsumer (
    private val connection: RabbitMQConnection,
    private val processor: IMessageProcessor,
    private val config: RabbitMQConfig,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IConsumer, Closeable {
    private val scope = CoroutineScope(dispatcher.limitedParallelism(4) + SupervisorJob())

    override fun start() {
        val channel = connection.channel()
        val consumer = ChannelConsumer(channel, processor, scope)

        channel.basicConsume(config.queueName, false, consumer)
    }

    override fun close() {
        scope.cancel()
        connection.close()
    }
}