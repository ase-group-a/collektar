package com.collektar.providers

import com.collektar.config.EmailProviderConfig
import com.collektar.providers.mail.IMailSession
import com.collektar.providers.mail.IMailTransport
import com.collektar.providers.mail.JakartaMailSession
import com.collektar.providers.mail.JakartaMailTransport
import jakarta.mail.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class SESEmailProvider(
    private val config: EmailProviderConfig,
    private val mailSession: IMailSession = createDefaultSession(config),
    private val mailTransport: IMailTransport = JakartaMailTransport(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : IEmailProvider {
    override suspend fun sendEmail(
        to: String,
        subject: String,
        htmlBody: String
    ) : Result<Unit> = withContext(dispatcher) {
        try {
            val message = mailSession.createMessage()
            message.setSubject(subject, "UTF-8")
            message.setFrom(config.fromEmail, config.fromName)
            message.setRecipients(to)
            message.setHtmlContent(htmlBody)
            message.setSentDate()

            mailTransport.send(message)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private fun createDefaultSession(config: EmailProviderConfig): IMailSession {
            val properties = Properties().apply {
                put("mail.smtp.auth", true)
                put("mail.smtp.host", config.host)
                put("mail.smtp.port", config.port)
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.starttls.required", "true")
            }

            val authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        config.username,
                        config.password
                    )
                }
            }

            val session = Session.getInstance(properties, authenticator)
            return JakartaMailSession(session)
        }
    }
}