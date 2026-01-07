package com.collektar.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
enum class EmailType {
    WELCOME,
    PASSWORD_RESET,
    PASSWORD_CHANGED,
    ACCOUNT_DELETED
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("message_type")
sealed class EmailMessage {
    abstract val to: String
    abstract val emailType: EmailType
}

@Serializable
@SerialName("welcome_email")
data class WelcomeEmail(
    override val to: String,
    val displayName: String,
) : EmailMessage() {
    override val emailType = EmailType.WELCOME
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
}

@Serializable
@SerialName("password_changed_email")
data class PasswordChangedEmail(
    override val to: String,
    val displayName: String
) : EmailMessage() {
    override val emailType = EmailType.PASSWORD_CHANGED
}

@Serializable
@SerialName("account_deleted_email")
data class AccountDeletedEmail(
    override val to: String,
    val displayName: String
) : EmailMessage() {
    override val emailType = EmailType.ACCOUNT_DELETED
}