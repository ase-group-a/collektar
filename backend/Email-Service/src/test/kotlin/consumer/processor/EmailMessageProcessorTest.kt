package com.collektar.consumer.processor

import com.collektar.builder.IEmailBuilder
import com.collektar.models.*
import com.collektar.sender.IEmailSender
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EmailMessageProcessorTest {
    private lateinit var emailBuilder: IEmailBuilder
    private lateinit var emailSender: IEmailSender
    private lateinit var processor: EmailMessageProcessor

    private val json = Json {
        ignoreUnknownKeys = true
        classDiscriminator = "message_type"
    }

    @BeforeEach
    fun setup() {
        emailBuilder = mockk()
        emailSender = mockk()
        processor = EmailMessageProcessor(emailBuilder, emailSender, json)
    }

    @Test
    fun shouldProcessWelcomeEmailSuccessfully() = runTest {
        val emailMessage: EmailMessage = WelcomeEmail(
            to = "test@example.com",
            displayName = "Test User"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("test@example.com", "Welcome to Collektar!", "<html>Welcome</html>")
        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.success(Unit)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.Success)
        verify { emailBuilder.buildEmail(emailMessage) }
        coVerify { emailSender.send(email) }
    }

    @Test
    fun shouldProcessPasswordResetEmailSuccessfully() = runTest {
        val emailMessage: EmailMessage = PasswordResetEmail(
            to = "user@example.com",
            displayName = "John Doe",
            resetToken = "reset-token-123",
            expiryMinutes = 30
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("user@example.com", "Reset Your Password", "<html>Reset</html>")

        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.success(Unit)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.Success)
    }

    @Test
    fun shouldProcessEmailVerificationSuccessfully() = runTest {
        val emailMessage: EmailMessage = EmailVerificationEmail(
            to = "newuser@example.com",
            displayName = "New User",
            verificationToken = "verify-token-456"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("newuser@example.com", "Verify Your Email", "<html>Verify</html>")

        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.success(Unit)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.Success)
    }

    @Test
    fun shouldProcessPasswordChangedEmailSuccessfully() = runTest {
        val emailMessage: EmailMessage = PasswordChangedEmail(
            to = "user@example.com",
            displayName = "User Name"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("user@example.com", "Password Changed", "<html>Changed</html>")

        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.success(Unit)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.Success)
    }

    @Test
    fun shouldProcessAccountDeletedEmailSuccessfully() = runTest {
        val emailMessage: EmailMessage = AccountDeletedEmail(
            to = "deleted@example.com",
            displayName = "Deleted User"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("deleted@example.com", "Account Deleted", "<html>Goodbye</html>")

        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.success(Unit)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.Success)
    }

    @Test
    fun shouldReturnRetryableFailureWhenSenderFails() = runTest {
        val emailMessage: EmailMessage = WelcomeEmail(
            to = "test@example.com",
            displayName = "Test User"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()
        val email = Email("test@example.com", "Welcome", "<html>Welcome</html>")
        val error = RuntimeException("SMTP connection failed")

        every { emailBuilder.buildEmail(emailMessage) } returns email
        coEvery { emailSender.send(email) } returns Result.failure(error)

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.RetryableFailure)
        assertEquals(error, (result as ProcessingResult.RetryableFailure).error)
    }

    @Test
    fun shouldReturnPermanentFailureWhenJsonInvalid() = runTest {
        val invalidJson = """{"invalid": "structure"}""".toByteArray()

        val result = processor.process(invalidJson)

        assertTrue(result is ProcessingResult.PermanentFailure)
    }

    @Test
    fun shouldReturnPermanentFailureWhenBuilderThrows() = runTest {
        val emailMessage: EmailMessage = WelcomeEmail(
            to = "test@example.com",
            displayName = "Test User"
        )
        val messageBody = json.encodeToString(emailMessage).toByteArray()

        every { emailBuilder.buildEmail(any()) } throws IllegalArgumentException("Template not found")

        val result = processor.process(messageBody)

        assertTrue(result is ProcessingResult.PermanentFailure)
        assertTrue((result as ProcessingResult.PermanentFailure).error is IllegalArgumentException)
    }
}