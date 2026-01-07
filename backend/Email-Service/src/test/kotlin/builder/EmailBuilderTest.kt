package com.collektar.builder

import com.collektar.config.EnvironmentConfig
import com.collektar.models.*
import com.collektar.shared.utility.IEmailTemplateLoader
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmailBuilderTest {
    private lateinit var mockEnvironmentConfig: EnvironmentConfig
    private lateinit var mockTemplateLoader: IEmailTemplateLoader
    private lateinit var emailBuilder: EmailBuilder

    private val defaultAppBaseUrl = "https://app.collektar.com"
    private val defaultRecipient = "user@example.com"
    private val defaultDisplayName = "John Doe"

    private val testTemplate = """
        <!DOCTYPE html>
        <html>
        <head><title>{{title}}</title></head>
        <body>
            <h1>{{title}}</h1>
            <div class="content">{{body}}</div>
        </body>
        </html>
    """.trimIndent()

    @BeforeEach
    fun setup() {
        mockEnvironmentConfig = mockk()
        mockTemplateLoader = mockk()

        every { mockEnvironmentConfig.appBaseUrl } returns defaultAppBaseUrl
        every { mockTemplateLoader.loadTemplate("email-template.html") } returns testTemplate

        emailBuilder = EmailBuilder(mockEnvironmentConfig, mockTemplateLoader)
    }

    @Test
    fun shouldBuildWelcomeEmail() {
        val welcomeEmail = WelcomeEmail(
            to = defaultRecipient,
            displayName = defaultDisplayName
        )

        val result = emailBuilder.buildEmail(welcomeEmail)

        assertEquals(defaultRecipient, result.to)
        assertEquals("Welcome to Collektar!", result.subject)
        assertTrue(result.htmlBody.contains(defaultDisplayName))
        assertTrue(result.htmlBody.contains(defaultAppBaseUrl))
    }

    @Test
    fun shouldBuildPasswordResetEmail() {
        val resetToken = "reset-token-123"
        val resetEmail = PasswordResetEmail(
            to = defaultRecipient,
            displayName = defaultDisplayName,
            resetToken = resetToken
        )

        val result = emailBuilder.buildEmail(resetEmail)

        assertEquals(defaultRecipient, result.to)
        assertEquals("Reset Your Password", result.subject)
        assertTrue(result.htmlBody.contains(defaultDisplayName))
        assertTrue(result.htmlBody.contains("$defaultAppBaseUrl/reset-password?token=$resetToken"))
        assertTrue(result.htmlBody.contains("60 minutes"))
    }

    @Test
    fun shouldBuildEmailVerificationEmail() {
        val verificationToken = "verify-token-456"
        val verificationEmail = EmailVerificationEmail(
            to = defaultRecipient,
            displayName = defaultDisplayName,
            verificationToken = verificationToken
        )

        val result = emailBuilder.buildEmail(verificationEmail)

        assertEquals(defaultRecipient, result.to)
        assertEquals("Verify Your Email", result.subject)
        assertTrue(result.htmlBody.contains(defaultDisplayName))
        assertTrue(result.htmlBody.contains("$defaultAppBaseUrl/verify-email?token=$verificationToken"))
    }

    @Test
    fun shouldBuildPasswordChangedEmail() {
        val changedEmail = PasswordChangedEmail(
            to = defaultRecipient,
            displayName = defaultDisplayName
        )

        val result = emailBuilder.buildEmail(changedEmail)

        assertEquals(defaultRecipient, result.to)
        assertEquals("Password Changed", result.subject)
        assertTrue(result.htmlBody.contains(defaultDisplayName))
        assertTrue(result.htmlBody.contains("$defaultAppBaseUrl/support"))
    }

    @Test
    fun shouldBuildAccountDeletedEmail() {
        val deletedEmail = AccountDeletedEmail(
            to = defaultRecipient,
            displayName = defaultDisplayName
        )

        val result = emailBuilder.buildEmail(deletedEmail)

        assertEquals(defaultRecipient, result.to)
        assertEquals("Account Deleted", result.subject)
        assertTrue(result.htmlBody.contains(defaultDisplayName))
    }
}