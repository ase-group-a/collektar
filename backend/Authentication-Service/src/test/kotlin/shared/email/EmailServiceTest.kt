package com.collektar.shared.email

import com.collektar.dto.AccountDeletedEmail
import com.collektar.dto.PasswordChangedEmail
import com.collektar.dto.PasswordResetEmail
import com.collektar.dto.WelcomeEmail
import com.collektar.shared.producer.IEmailPublisher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EmailServiceTest {
    private lateinit var mockPublisher: IEmailPublisher
    private lateinit var emailService: EmailService

    @BeforeEach
    fun setup() {
        mockPublisher = mockk()
        emailService = EmailService(mockPublisher)
    }

    @Test
    fun shouldPublishWelcomeEmail() {
        val to = "user@example.com"
        val displayName = "Test User"

        every { mockPublisher.publish(any<WelcomeEmail>()) } returns Result.success(Unit)

        emailService.sendWelcomeEmail(to, displayName)

        verify(exactly = 1) {
            mockPublisher.publish(
                WelcomeEmail(
                    to = to,
                    displayName = displayName
                )
            )
        }
    }

    @Test
    fun shouldPublishPasswordResetEmail() {
        val to = "user@example.com"
        val displayName = "Test User"
        val resetToken = "reset-token-123"
        val expiryMinutes = 30

        every { mockPublisher.publish(any<PasswordResetEmail>()) } returns Result.success(Unit)

        val result = emailService.sendPasswordResetEmail(to, displayName, resetToken, expiryMinutes)

        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            mockPublisher.publish(
                PasswordResetEmail(
                    to = to,
                    displayName = displayName,
                    resetToken = resetToken,
                    expiryMinutes = expiryMinutes
                )
            )
        }
    }

    @Test
    fun shouldPublishPasswordChangedEmail() {
        val to = "user@example.com"
        val displayName = "Test User"

        every { mockPublisher.publish(any<PasswordChangedEmail>()) } returns Result.success(Unit)

        val result = emailService.sendPasswordChangedEmail(to, displayName)

        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            mockPublisher.publish(
                PasswordChangedEmail(
                    to = to,
                    displayName = displayName
                )
            )
        }
    }

    @Test
    fun shouldPublishAccountDeletedEmail() {
        val to = "user@example.com"
        val displayName = "Test User"

        every { mockPublisher.publish(any<AccountDeletedEmail>()) } returns Result.success(Unit)

        val result = emailService.sendAccountDeletedEmail(to, displayName)

        assertTrue(result.isSuccess)
        verify(exactly = 1) {
            mockPublisher.publish(
                AccountDeletedEmail(
                    to = to,
                    displayName = displayName
                )
            )
        }
    }

    @Test
    fun shouldReturnFailureWhenPasswordResetEmailPublishFails() {
        val to = "user@example.com"
        val displayName = "Test User"
        val resetToken = "reset-token-123"
        val expiryMinutes = 30
        val exception = RuntimeException("Publish failed")

        every { mockPublisher.publish(any<PasswordResetEmail>()) } returns Result.failure(exception)

        val result = emailService.sendPasswordResetEmail(to, displayName, resetToken, expiryMinutes)

        assertTrue(result.isFailure)
        verify(exactly = 1) {
            mockPublisher.publish(any<PasswordResetEmail>())
        }
    }

    @Test
    fun shouldReturnFailureWhenPasswordChangedEmailPublishFails() {
        val to = "user@example.com"
        val displayName = "Test User"
        val exception = RuntimeException("Publish failed")

        every { mockPublisher.publish(any<PasswordChangedEmail>()) } returns Result.failure(exception)

        val result = emailService.sendPasswordChangedEmail(to, displayName)

        assertTrue(result.isFailure)
        verify(exactly = 1) {
            mockPublisher.publish(any<PasswordChangedEmail>())
        }
    }

    @Test
    fun shouldReturnFailureWhenAccountDeletedEmailPublishFails() {
        val to = "user@example.com"
        val displayName = "Test User"
        val exception = RuntimeException("Publish failed")

        every { mockPublisher.publish(any<AccountDeletedEmail>()) } returns Result.failure(exception)

        val result = emailService.sendAccountDeletedEmail(to, displayName)

        assertTrue(result.isFailure)
        verify(exactly = 1) {
            mockPublisher.publish(any<AccountDeletedEmail>())
        }
    }

    @Test
    fun shouldPublishWelcomeEmailWithCorrectData() {
        val to = "newuser@example.com"
        val displayName = "New User"

        every { mockPublisher.publish(any<WelcomeEmail>()) } returns Result.success(Unit)

        emailService.sendWelcomeEmail(to, displayName)

        verify(exactly = 1) {
            mockPublisher.publish(
                match<WelcomeEmail> {
                    it.to == to && it.displayName == displayName
                }
            )
        }
    }

    @Test
    fun shouldPublishPasswordResetEmailWithCorrectData() {
        val to = "user@example.com"
        val displayName = "User Name"
        val resetToken = "unique-token-456"
        val expiryMinutes = 15

        every { mockPublisher.publish(any<PasswordResetEmail>()) } returns Result.success(Unit)

        emailService.sendPasswordResetEmail(to, displayName, resetToken, expiryMinutes)

        verify(exactly = 1) {
            mockPublisher.publish(
                match<PasswordResetEmail> {
                    it.to == to &&
                            it.displayName == displayName &&
                            it.resetToken == resetToken &&
                            it.expiryMinutes == expiryMinutes
                }
            )
        }
    }
}