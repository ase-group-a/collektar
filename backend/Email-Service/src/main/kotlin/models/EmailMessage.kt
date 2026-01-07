package com.collektar.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class EmailType {
    WELCOME,
    PASSWORD_RESET,
    EMAIL_VERIFICATION,
    PASSWORD_CHANGED,
    ACCOUNT_DELETED
}


@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("message_type")
sealed class EmailMessage {
    abstract val to: String
    abstract val emailType: EmailType
    abstract fun buildContent(appUrl: String): EmailContent
}

@Serializable
@SerialName("welcome_email")
data class WelcomeEmail (
    override val to: String,
    val displayName: String,
) : EmailMessage() {
    override val emailType = EmailType.WELCOME
    override fun buildContent(appUrl: String) = EmailContent (
        subject = "Welcome to Collektar!",
        title = "Welcome to Collektar!",
        body = """
            <p>Hi $displayName,</p>
            <p>We're thrilled to have you on board.</p>
            <p>You can now start exploring our platform and manage all your media in one place!</p>
            <a href="$appUrl" class="button">Get Started</a>
        """.trimIndent(),
    )
}

@Serializable
@SerialName("password_reset_email")
data class PasswordResetEmail(
    override val to: String,
    val displayName: String,
    val resetToken: String,
    val expiryMinutes: Int = 60
) : EmailMessage() {
    override val emailType = EmailType.PASSWORD_RESET
    override fun buildContent(appUrl: String) = EmailContent (
        subject = "Reset Your Password",
        title = "Reset Your Password",
        body = """
            <p>Hi $displayName,</p>
            <p>We received a request to reset your password. Click the button below to create a new password:</p>
            <a href="$appUrl/reset-password?token=$resetToken" class="button">Reset Password</a>
            <p>This link will expire in $expiryMinutes minutes.</p>
            <p>If you didn't request this, please ignore this email. Your password will remain unchanged.</p>
        """.trimIndent()
    )
}

@Serializable
@SerialName("email_verification_email")
data class EmailVerificationEmail(
    override val to: String,
    val displayName: String,
    val verificationToken: String
) : EmailMessage() {
    override val emailType = EmailType.EMAIL_VERIFICATION
    override fun buildContent(appUrl: String) = EmailContent (
        subject = "Verify Your Email",
        title = "Verify Your Email",
        body = """
            <p>Hi $displayName,</p>
            <p>Thanks for signing up! Please verify your email address by clicking the button below:</p>
            <a href="$appUrl/verify-email?token=$verificationToken" class="button">Verify Email</a>
            <p>If you didn't create an account, you can safely ignore this email.</p>
        """.trimIndent()
    )
}

@Serializable
@SerialName("password_changed_email")
data class PasswordChangedEmail(
    override val to: String,
    val displayName: String
) : EmailMessage() {
    override val emailType = EmailType.PASSWORD_CHANGED
    override fun buildContent(appUrl: String) = EmailContent (
        subject = "Password Changed",
        title = "Password Changed",
        body = """
            <p>Hi $displayName,</p>
            <p>Your password has been changed successfully.</p>
            <p>If you didn't make this change, please contact our support team immediately.</p>
            <a href="$appUrl/support" class="button">Contact Support</a>
        """.trimIndent()
    )
}

@Serializable
@SerialName("account_deleted_email")
data class AccountDeletedEmail(
    override val to: String,
    val displayName: String
) : EmailMessage() {
    override val emailType = EmailType.ACCOUNT_DELETED
    override fun buildContent(appUrl: String) = EmailContent (
        subject = "Account Deleted",
        title = "Account Deleted",
        body = """
            <p>Hi $displayName,</p>
            <p>Your account has been successfully deleted as requested.</p>
            <p>We're sorry to see you go. If you change your mind, you're always welcome to create a new account.</p>
            <p>Thank you for being part of our community.</p>
        """.trimIndent()
    )
}