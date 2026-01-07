package com.collektar.consumer.rabbitmq

import com.collektar.consumer.processor.IMessageProcessor
import com.collektar.consumer.processor.ProcessingResult
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import com.rabbitmq.client.AMQP.BasicProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ChannelConsumer(
    private val channel: Channel,
    private val processor: IMessageProcessor,
    private val scope: CoroutineScope
) : DefaultConsumer(channel)  {
    override fun handleDelivery(consumerTag: String?, envelope: Envelope, properties: BasicProperties?, body: ByteArray) {
        val deliveryTag = envelope.deliveryTag

        scope.launch {
            try {
                val result = processor.process(body)
                when (result) {
                    is ProcessingResult.Success -> { channel.basicAck(deliveryTag, false) }
                    is ProcessingResult.RetryableFailure -> { channel.basicNack(deliveryTag, false, true) }
                    is ProcessingResult.PermanentFailure -> { channel.basicNack(deliveryTag, false, false) }
                }
            } catch (e: Exception) {
                channel.basicNack(deliveryTag, false, true)
            }
        }
    }
}