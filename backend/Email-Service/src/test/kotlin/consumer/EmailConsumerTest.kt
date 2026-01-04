package com.collektar.consumer

import com.collektar.config.RabbitMQConfig
import com.collektar.consumer.processor.IMessageProcessor
import com.collektar.consumer.rabbitmq.RabbitMQConnection
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Consumer
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmailConsumerTest {
    private lateinit var mockConnection: RabbitMQConnection
    private lateinit var mockProcessor: IMessageProcessor
    private lateinit var mockChannel: Channel
    private lateinit var config: RabbitMQConfig
    private lateinit var emailConsumer: EmailConsumer

    private val testDispatcher = StandardTestDispatcher()
    private val defaultQueueName = "test-queue"

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        config = RabbitMQConfig(
            host = "localhost",
            port = 5672,
            user = "guest",
            password = "guest",
            queueName = defaultQueueName
        )

        mockConnection = mockk(relaxed = true)
        mockProcessor = mockk(relaxed = true)
        mockChannel = mockk(relaxed = true)

        every { mockConnection.channel() } returns mockChannel
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun shouldGetChannelFromConnectionWhenStarted() {
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config, testDispatcher)

        emailConsumer.start()

        verify { mockConnection.channel() }
    }

    @Test
    fun shouldConsumeFromConfiguredQueueWhenStarted() {
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config, testDispatcher)

        emailConsumer.start()

        verify { mockChannel.basicConsume(eq(defaultQueueName), any(), any<Consumer>()) }
    }

    @Test
    fun shouldCloseConnectionWhenClosed() {
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config, testDispatcher)
        emailConsumer.start()

        emailConsumer.close()

        verify { mockConnection.close() }
    }

    @Test
    fun shouldNotThrowWhenClosedWithoutStarting() {
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config, testDispatcher)

        emailConsumer.close()

        verify { mockConnection.close() }
    }

    @Test
    fun shouldUseDefaultDispatcherWhenNotProvided() {
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config)

        emailConsumer.start()

        verify { mockChannel.basicConsume(eq(defaultQueueName), eq(false), any<Consumer>()) }

        emailConsumer.close()
    }

    @Test
    fun shouldUseProvidedDispatcher() {
        val customDispatcher = StandardTestDispatcher()
        emailConsumer = EmailConsumer(mockConnection, mockProcessor, config, customDispatcher)

        emailConsumer.start()

        verify { mockChannel.basicConsume(eq(defaultQueueName), eq(false), any<Consumer>()) }

        emailConsumer.close()
    }

}