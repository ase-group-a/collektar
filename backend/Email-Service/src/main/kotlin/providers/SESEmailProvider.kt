package com.collektar.providers

import com.collektar.config.EmailProviderConfig
import jakarta.mail.*
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class SESEmailProvider(
    private val config: EmailProviderConfig
) : IEmailProvider {
    private val session: Session by lazy { createSession(config) }

    override suspend fun sendEmail(
        to: String,
        subject: String,
        htmlBody: String
    ) : Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val message = createMessage(
                session = session,
                to = to,
                subject = subject,
                htmlBody = htmlBody
            )
            Transport.send(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createSession(config: EmailProviderConfig): Session {
        return Session.getInstance(
            createSmtpProperties(config),
            createAuthenticator(config)
        )
    }

    private fun createSmtpProperties(config: EmailProviderConfig) = Properties().apply {
            put("mail.smtp.auth", true)
            put("mail.smtp.host", config.host)
            put("mail.smtp.port", config.port)
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
    }

    private fun createAuthenticator(config: EmailProviderConfig) = object : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(
                config.username,
                config.password
            )
        }
    }

    private fun createMessage(
        session: Session,
        to: String,
        subject: String,
        htmlBody: String
    ) = MimeMessage(session).apply {
        setSubject(subject, "UTF-8")
        setFrom(InternetAddress(config.fromEmail, config.fromName))
        setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(to)
        )

        val multipart = MimeMultipart("alternative")
        val htmlPart = createBodyPart(htmlBody)

        multipart.addBodyPart(htmlPart)
        setContent(multipart)
        sentDate = Date()
    }

    private fun createBodyPart(htmlBody: String) = MimeBodyPart().apply {
        setContent(htmlBody, "text/html; charset=UTF-8")
    }
}