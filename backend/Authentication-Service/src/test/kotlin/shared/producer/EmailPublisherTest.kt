package com.collektar.shared.producer

import com.collektar.config.RabbitMQConfig
import com.collektar.dto.AccountDeletedEmail
import com.collektar.dto.PasswordChangedEmail
import com.collektar.dto.PasswordResetEmail
import com.collektar.dto.WelcomeEmail
import com.collektar.shared.producer.connectionmanager.RabbitMQConnection
import com.rabbitmq.client.Channel
import com.rabbitmq.client.MessageProperties
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EmailPublisherTest {
    private lateinit var mockConnection: RabbitMQConnection
    private lateinit var mockChannel: Channel
    private lateinit var config: RabbitMQConfig
    private lateinit var json: Json
    private lateinit var emailPublisher: EmailPublisher

    private val queueName = "email-queue"

    @BeforeEach
    fun setup() {
        mockConnection = mockk()
        mockChannel = mockk(relaxed = true)
        config = RabbitMQConfig(
            host = "localhost",
            port = 5672,
            user = "guest",
            password = "guest",
            queueName = queueName
        )
        json = Json { ignoreUnknownKeys = false }
        emailPublisher = EmailPublisher(mockConnection, config, json)

        every { mockConnection.channel() } returns mockChannel
    }

    @Test
    fun shouldPublishWelcomeEmailSuccessfully() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        val result = emailPublisher.publish(email)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                any()
            )
        }
    }

    @Test
    fun shouldPublishPasswordResetEmailSuccessfully() {
        val email = PasswordResetEmail(
            to = "user@example.com",
            displayName = "Test User",
            resetToken = "reset-token-123",
            expiryMinutes = 30
        )

        val result = emailPublisher.publish(email)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                any()
            )
        }
    }

    @Test
    fun shouldPublishPasswordChangedEmailSuccessfully() {
        val email = PasswordChangedEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        val result = emailPublisher.publish(email)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                any()
            )
        }
    }

    @Test
    fun shouldPublishAccountDeletedEmailSuccessfully() {
        val email = AccountDeletedEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        val result = emailPublisher.publish(email)

        assertTrue(result.isSuccess)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                any()
            )
        }
    }

    @Test
    fun shouldReturnFailureWhenChannelThrowsException() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        every { mockConnection.channel() } throws RuntimeException("Connection failed")

        val result = emailPublisher.publish(email)

        assertTrue(result.isFailure)
        assertEquals("Connection failed", result.exceptionOrNull()?.message)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 0) { mockChannel.basicPublish(any(), any(), any(), any()) }
    }

    @Test
    fun shouldReturnFailureWhenBasicPublishThrowsException() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        every {
            mockChannel.basicPublish(any(), any(), any(), any())
        } throws RuntimeException("Publish failed")

        val result = emailPublisher.publish(email)

        assertTrue(result.isFailure)
        assertEquals("Publish failed", result.exceptionOrNull()?.message)
        verify(exactly = 1) { mockConnection.channel() }
        verify(exactly = 1) { mockChannel.basicPublish(any(), any(), any(), any()) }
    }

    @Test
    fun shouldPublishToCorrectQueue() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        emailPublisher.publish(email)

        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                queueName,
                any(),
                any()
            )
        }
    }

    @Test
    fun shouldUseEmptyExchangeForDirectQueuePublishing() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        emailPublisher.publish(email)

        verify(exactly = 1) {
            mockChannel.basicPublish(
                "",
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun shouldUsePersistentMessageProperties() {
        val email = WelcomeEmail(
            to = "user@example.com",
            displayName = "Test User"
        )

        emailPublisher.publish(email)

        verify(exactly = 1) {
            mockChannel.basicPublish(
                any(),
                any(),
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                any()
            )
        }
    }
}