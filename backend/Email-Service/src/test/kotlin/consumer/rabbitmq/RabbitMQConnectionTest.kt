package com.collektar.consumer.rabbitmq

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
import org.junit.jupiter.api.assertNotNull
import kotlin.test.assertEquals

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
    private val defaultQueueName = "test-queue"

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
        verify { mockFactory.networkRecoveryInterval = 5000 }
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
        assertEquals(mockChannel, firstResult)
        val secondResult = rabbitMQConnection.channel()
        val thirdResult = rabbitMQConnection.channel()

        verify(exactly = 1) { mockFactory.newConnection() }
        verify(exactly = 1) { mockConnection.createChannel() }
        verify(atLeast = 2) { mockChannel.isOpen }
    }

    @Test
    fun shouldReturnExistingChannelWhenNotNullAndOpen() {
        every { mockFactory.newConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        rabbitMQConnection = RabbitMQConnection(config, mockFactory)

        val firstCall = rabbitMQConnection.channel()
        val secondCall = rabbitMQConnection.channel()
        val thirdCall = rabbitMQConnection.channel()

        assertEquals(mockChannel, firstCall)
        assertEquals(mockChannel, secondCall)
        assertEquals(mockChannel, thirdCall)
        verify(exactly = 1) { mockFactory.newConnection() }
        verify(exactly = 1) { mockConnection.createChannel() }
        verify(exactly = 2) { mockChannel.isOpen }
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
    fun shouldRecreateChannelAndConnectionWhenExistingChannelIsNotOP() {
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
}

