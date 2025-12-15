package com.collektar.consumer

import com.collektar.config.RabbitMQConfig
import com.collektar.consumer.connectionmanager.RabbitMQConnectionManager
import com.collektar.consumer.processor.IEmailMessageProcessor
import com.collektar.consumer.processor.ProcessingResult
import com.rabbitmq.client.*
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory


class EmailConsumer (
    private val connectionManager: RabbitMQConnectionManager,
    private val messageProcessor: IEmailMessageProcessor,
    private val config: RabbitMQConfig
) : IEmailConsumer {
    private val logger = LoggerFactory.getLogger(EmailConsumer::class.java)
    private val scope = CoroutineScope(
        Dispatchers.IO.limitedParallelism(1) + SupervisorJob()
    )

    override fun start() {
        logger.info("Starting Email Consumer")
        val channel = connectionManager.connect()
        val consumer = createConsumer(channel)
        channel.basicConsume(config.queueName, false, consumer)
        logger.info("Started Email Consumer")
    }

    override fun stop() {
        scope.cancel()
        connectionManager.close()
    }

    private fun createConsumer(channel: Channel) = object : DefaultConsumer(channel) {
        override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?
        ) {

            logger.info("Received Message")
            if (envelope == null) { return }
            if (body == null) { return }
            scope.launch { handleMessage(body, envelope.deliveryTag, channel) }
        }
    }

    private suspend fun handleMessage(body: ByteArray, deliveryTag: Long, channel: Channel) {
        val result = messageProcessor.process(body)
        when (result) {
            is ProcessingResult.Success -> { channel.basicAck(deliveryTag, false) }
            is ProcessingResult.RetryableFailure -> { channel.basicNack(deliveryTag, false, true) }
            is ProcessingResult.PermanentFailure -> { channel.basicNack(deliveryTag, false, false) }
        }
    }
}