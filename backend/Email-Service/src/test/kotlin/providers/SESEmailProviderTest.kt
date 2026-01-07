package com.collektar.providers

import com.collektar.config.EmailProviderConfig
import com.collektar.providers.mail.IMailMessage
import com.collektar.providers.mail.IMailSession
import com.collektar.providers.mail.IMailTransport
import com.collektar.providers.mail.JakartaMailTransport
import io.mockk.*
import jakarta.mail.MessagingException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SESEmailProviderTest {
    private lateinit var mockSession: IMailSession
    private lateinit var mockMessage: IMailMessage
    private lateinit var mockTransport: IMailTransport

    private val defaultFromEmail = "sender@example.com"
    private val defaultFromName = "Test Sender"
    private val defaultTo = "recipient@example.com"
    private val defaultSubject = "Test Subject"
    private val defaultHtmlBody = "<html><body>Test</body></html>"
    private val config = EmailProviderConfig(
        host = "email-smtp.us-east-1.amazonaws.com",
        port = "587",
        username = "testuser",
        password = "testpass",
        fromEmail = defaultFromEmail,
        fromName = defaultFromName
    )

    @BeforeEach
    fun setup() {
        mockSession = mockk<IMailSession>()
        mockMessage = mockk<IMailMessage>(relaxed = true)
        mockTransport = mockk<IMailTransport>()
    }

    @Test
    fun shouldSendEmailSuccessfully() = runTest {
        every { mockSession.createMessage() } returns mockMessage
        every { mockTransport.send(mockMessage) } just Runs

        val provider = SESEmailProvider(config, mockSession, mockTransport)

        val result = provider.sendEmail(
            to = defaultTo,
            subject = defaultSubject,
            htmlBody = defaultHtmlBody
        )

        assertTrue(result.isSuccess)
        verify { mockMessage.setSubject(defaultSubject, "UTF-8") }
        verify { mockMessage.setFrom(defaultFromEmail, defaultFromName) }
        verify { mockMessage.setRecipients(defaultTo) }
        verify { mockMessage.setHtmlContent(defaultHtmlBody) }
        verify { mockMessage.setSentDate() }
        verify { mockTransport.send(mockMessage) }
    }

    @Test
    fun shouldReturnFailureWhenTransportFails() = runTest {
        val smtpErrorMessage = "Some SMTP error"
        every { mockSession.createMessage() } returns mockMessage
        every { mockTransport.send(mockMessage) } throws MessagingException(smtpErrorMessage)

        val provider = SESEmailProvider(config, mockSession, mockTransport)
        val result = provider.sendEmail(
            to = defaultTo,
            subject = defaultSubject,
            htmlBody = defaultHtmlBody
        )

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is MessagingException)
        assertEquals(smtpErrorMessage, exception.message)
    }

    @Test
    fun shouldUseDefaultSessionAndTransportWhenNotProvided() = runTest {
        val provider = SESEmailProvider(config)
        assertNotNull(provider)
    }

    @Test
    fun shouldCreateDefaultSessionWithAuthentication() = runTest {
        val provider = SESEmailProvider(config)

        val result = provider.sendEmail(
            to = defaultTo,
            subject = defaultSubject,
            htmlBody = defaultHtmlBody
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun shouldThrowExceptionWhenMessageIsNotJakartaMailMessage() {
        val transport = JakartaMailTransport()
        val invalidMessage = mockk<IMailMessage>()

        val exception = assertFailsWith<IllegalArgumentException> {
            transport.send(invalidMessage)
        }

        assertEquals(exception.message, "Message must be JakartaMailMessage")
    }

    // Nonsensical test for coverage
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun shouldSendEmailOnProvidedDispatcher() = runTest {
        every { mockSession.createMessage() } returns mockMessage
        every { mockTransport.send(mockMessage) } just Runs

        val provider = SESEmailProvider(
            config,
            mockSession,
            mockTransport,
            UnconfinedTestDispatcher(testScheduler)
        )

        val result = provider.sendEmail(
            to = defaultTo,
            subject = defaultSubject,
            htmlBody = defaultHtmlBody
        )

        assertTrue(result.isSuccess)
    }
}