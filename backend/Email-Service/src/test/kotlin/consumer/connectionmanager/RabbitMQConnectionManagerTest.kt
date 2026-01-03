package com.collektar.consumer.connectionmanager

import com.collektar.config.RabbitMQConfig
import com.collektar.consumer.connectionfactory.IRabbitMQConnectionFactory
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals


class RabbitMQConnectionManagerTest {
    private lateinit var config: RabbitMQConfig
    private lateinit var connectionFactory: IRabbitMQConnectionFactory
    private lateinit var mockConnection: Connection
    private lateinit var mockChannel: Channel
    private lateinit var connectionManager: RabbitMQConnectionManager

    @BeforeEach
    fun setup() {
        config = RabbitMQConfig(
            host = "localhost",
            port = 5672,
            user = "guest",
            password = "guest",
            queueName = "test-queue"
        )
        connectionFactory = mockk(relaxed = true)
        mockConnection = mockk(relaxed = true)
        mockChannel = mockk(relaxed = true)
        connectionManager = RabbitMQConnectionManager(config, connectionFactory)
    }

    @Test
    fun shouldCreateNewConnectionAndChannelWhenNoneExists() {
        every { connectionFactory.createConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        val result = connectionManager.connect()

        assertEquals(mockChannel, result)
        verify { connectionFactory.createConnection() }
        verify { mockConnection.createChannel() }
        verify { mockChannel.queueDeclare("test-queue", true, false, false, null) }
        verify { mockChannel.basicQos(1) }
    }

    @Test
    fun shouldReuseExistingChannelWHenIsOpen() {
        every { connectionFactory.createConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        connectionManager.connect()
        val result = connectionManager.connect()

        assertEquals(mockChannel, result)
        verify(exactly = 1) { connectionFactory.createConnection() }
        verify(exactly = 1) { mockConnection.createChannel() }
    }

    @Test
    fun shouldCreateNewChannelWhenExistingChannelIsClosed() {
        val firstChannel = mockk<Channel>(relaxed = true)
        val secondChannel = mockk<Channel>(relaxed = true)

        every { connectionFactory.createConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns firstChannel andThen secondChannel
        every { firstChannel.isOpen } returns false
        every { secondChannel.isOpen } returns true

        connectionManager.connect()
        val result = connectionManager.connect()

        assertEquals(secondChannel, result)
        verify(exactly = 2) { connectionFactory.createConnection() }
        verify(exactly = 2) { mockConnection.createChannel() }
        verify { secondChannel.queueDeclare("test-queue", true, false, false, null) }
        verify { secondChannel.basicQos(1) }
    }

    @Test
    fun shouldCloseChannelAndConnectionWhenClose() {
        every { connectionFactory.createConnection() } returns mockConnection
        every { mockConnection.createChannel() } returns mockChannel
        every { mockChannel.isOpen } returns true

        connectionManager.connect()

        connectionManager.close()

        verify { mockChannel.close() }
        verify { mockConnection.close() }
    }

    @Test
    fun shouldNotThrowOnCloseWhenConnectionAndChannelIsNull() {
        connectionManager.close()

        verify(exactly = 0) { mockChannel.close() }
        verify(exactly = 0) { mockConnection.close() }
    }
}