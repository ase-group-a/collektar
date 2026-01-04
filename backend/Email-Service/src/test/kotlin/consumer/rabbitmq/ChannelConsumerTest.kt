package com.collektar.consumer.rabbitmq

import com.collektar.consumer.processor.IMessageProcessor
import com.collektar.consumer.processor.ProcessingResult
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Envelope
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChannelConsumerTest {
    private lateinit var mockChannel: Channel
    private lateinit var mockProcessor: IMessageProcessor
    private lateinit var testScope: TestScope
    private lateinit var channelConsumer: ChannelConsumer

    private val defaultDeliveryTag: Long = 123
    private val defaultBody = "test message".toByteArray()

    @BeforeEach
    fun setup() {
        mockChannel = mockk(relaxed = true)
        mockProcessor = mockk()
        testScope = TestScope()
        channelConsumer = ChannelConsumer(mockChannel, mockProcessor, testScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldAckMessageOnSuccess() = testScope.runTest {
        coEvery { mockProcessor.process(defaultBody) } returns ProcessingResult.Success

        val envelope = createEnvelope(defaultDeliveryTag)
        channelConsumer.handleDelivery("consumerTag", envelope, null, defaultBody)

        advanceUntilIdle()

        verify { mockChannel.basicAck(defaultDeliveryTag, false) }
        verify(exactly = 0) { mockChannel.basicNack(any(), any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNackWithRequeueOnRetryableFailure() = testScope.runTest {
        val error = RuntimeException("Temporary error")
        coEvery { mockProcessor.process(defaultBody) } returns ProcessingResult.RetryableFailure(error)

        val envelope = createEnvelope(defaultDeliveryTag)
        channelConsumer.handleDelivery("consumerTag", envelope, null, defaultBody)

        advanceUntilIdle()

        verify { mockChannel.basicNack(defaultDeliveryTag, false, true) }
        verify(exactly = 0) { mockChannel.basicAck(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNackWithoutRequeueOnPermanentFailure() = testScope.runTest {
        val error = RuntimeException("Permanent error")
        coEvery { mockProcessor.process(defaultBody) } returns ProcessingResult.PermanentFailure(error)

        val envelope = createEnvelope(defaultDeliveryTag)
        channelConsumer.handleDelivery("consumerTag", envelope, null, defaultBody)

        advanceUntilIdle()

        verify { mockChannel.basicNack(defaultDeliveryTag, false, false) }
        verify(exactly = 0) { mockChannel.basicAck(any(), any()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldNackWithRequeueOnUnexpectedException() = testScope.runTest {
        coEvery { mockProcessor.process(any()) } coAnswers {
            throw RuntimeException("Unexpected error")
        }

        val envelope = createEnvelope(defaultDeliveryTag)
        channelConsumer.handleDelivery("consumerTag", envelope, null, defaultBody)

        advanceUntilIdle()

        verify { mockChannel.basicNack(defaultDeliveryTag, false, true) }
        verify(exactly = 0) { mockChannel.basicAck(any(), any()) }
    }

    private fun createEnvelope(deliveryTag: Long): Envelope {
        return mockk {
            every { this@mockk.deliveryTag } returns deliveryTag
        }
    }
}