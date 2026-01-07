package com.collektar.shared.producer.connectionmanager

import com.collektar.config.RabbitMQConfig
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RabbitMQConnectionTest {
    private lateinit var config: RabbitMQConfig
    private lateinit var mockFactory: ConnectionFactory
    private lateinit var mockConnection: Connection
    private lateinit var mockChannel: Channel
    private lateinit var rabbitMQConnection: RabbitMQConnection

    private val defaultHost = "localhost"
    private val defaultPort = 5672
    private val defaultUser = "guest"
    private val defaultPassword = "guest"
    private val defaultQueueName = "email-queue"

    @BeforeEach
    fun setup() {
        config = RabbitMQConfig(
            host = defaultHost,
            port = defaultPort,
            user = defaultUser,
            password = defaultPassword,
            queueName = defaultQueueName
        )

        mockFactory = mockk(relaxed = true)
        mockConnection = mockk(relaxed = true)
        mockChannel = mockk(relaxed = true)
    }

    @Test
    fun shouldCreateNewConnectionAndChannelWhenNoneExists() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        val result = rabbitMQConnection.channel()

        assertEquals(mockChannel, result)
        verify { mockFactory.host = defaultHost }
        verify { mockFactory.port = defaultPort }
        verify { mockFactory.username = defaultUser }
        verify { mockFactory.password = defaultPassword }
        verify { mockFactory.isAutomaticRecoveryEnabled = true }
        verify { mockFactory.newConnection() }
        verify { mockConnection.createChannel() }
        verify { mockChannel.queueDeclare(defaultQueueName, true, false, false, null) }
        verify { mockChannel.basicQos(1) }
    }

    @Test
    fun shouldReuseExistingChannelWhenOpen() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        val firstResult = rabbitMQConnection.channel()
        val secondResult = rabbitMQConnection.channel()
        val thirdResult = rabbitMQConnection.channel()

        assertEquals(mockChannel, firstResult)
        assertEquals(mockChannel, secondResult)
        assertEquals(mockChannel, thirdResult)
        verify(exactly = 1) { mockFactory.newConnection() }
        verify(exactly = 1) { mockConnection.createChannel() }
        verify(atLeast = 2) { mockChannel.isOpen }
    }

    @Test
    fun shouldRecreateChannelWhenExistingChannelIsClosed() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel

        every { mockChannel.isOpen } returns true
        rabbitMQConnection = RabbitMQConnection(config, mockFactory)
        rabbitMQConnection.channel()

        every { mockChannel.isOpen } returns false

        val result = rabbitMQConnection.channel()

        assertEquals(mockChannel, result)
        verify(exactly = 2) { mockFactory.newConnection() }
        verify(exactly = 2) { mockConnection.createChannel() }
        verify(exactly = 2) { mockChannel.queueDeclare(defaultQueueName, true, false, false, null) }
        verify(exactly = 2) { mockChannel.basicQos(1) }
        verify(atLeast = 1) { mockChannel.close() }
        verify(atLeast = 1) { mockConnection.close() }
    }

    @Test
    fun shouldCloseChannelAndConnectionWhenClose() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)
        rabbitMQConnection.channel()

        rabbitMQConnection.close()

        verify { mockChannel.close() }
        verify { mockConnection.close() }
    }

    @Test
    fun shouldNotThrowWhenCloseCalledWithNullChannelAndConnection() {
        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        assertDoesNotThrow { rabbitMQConnection.close() }

        verify(exactly = 0) { mockChannel.close() }
        verify(exactly = 0) { mockConnection.close() }
    }

    @Test
    fun shouldSetChannelAndConnectionToNullAfterClose() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)
        rabbitMQConnection.channel()
        rabbitMQConnection.close()

        val newConnection = mockk<Connection>(relaxed = true)
        val newChannel = mockk<Channel>(relaxed = true)
        every { mockFactory.newConnection() } returns newConnection
        every { newConnection.createChannel() } returns newChannel
        every { newChannel.isOpen } returns true

        val result = rabbitMQConnection.channel()

        assertEquals(newChannel, result)
        verify(exactly = 2) { mockFactory.newConnection() }
    }

    @Test
    fun shouldUseDefaultConnectionFactoryWhenNotProvided() {
        val connectionWithDefaultFactory = RabbitMQConnection(config)

        assertNotNull(connectionWithDefaultFactory)
    }

    @Test
    fun shouldHandleMultipleCloseCallsGracefully() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)
        rabbitMQConnection.channel()

        rabbitMQConnection.close()
        rabbitMQConnection.close()
        rabbitMQConnection.close()

        verify(exactly = 1) { mockChannel.close() }
        verify(exactly = 1) { mockConnection.close() }
    }

    @Test
    fun shouldHandleExceptionDuringConnectionCreation() {
        every { mockFactory.newConnection() } throws RuntimeException("Connection failed")

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        try {
            rabbitMQConnection.channel()
        } catch (e: RuntimeException) {
            assertEquals("Connection failed", e.message)
        }

        verify(exactly = 1) { mockFactory.newConnection() }
        verify(exactly = 0) { mockConnection.createChannel() }
    }

    @Test
    fun shouldHandleExceptionDuringChannelCreation() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } throws RuntimeException("Channel creation failed")

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        try {
            rabbitMQConnection.channel()
        } catch (e: RuntimeException) {
            assertEquals("Channel creation failed", e.message)
        }

        verify(exactly = 1) { mockFactory.newConnection() }
        verify(exactly = 1) { mockConnection.createChannel() }
    }
}