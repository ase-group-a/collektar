package com.collektar.providers.mail

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeBodyPart
import jakarta.mail.internet.MimeMessage
import jakarta.mail.internet.MimeMultipart
import java.util.*

class JakartaMailMessage(private val message: MimeMessage) : IMailMessage {
    override fun setSubject(subject: String, charset: String) {
        message.setSubject(subject, charset)
    }

    override fun setFrom(address: String, name: String) {
        message.setFrom(InternetAddress(address, name))
    }

    override fun setRecipients(to: String) {
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
    }

    override fun setHtmlContent(htmlBody: String) {
        val multipart = MimeMultipart("alternative")
        val htmlPart = MimeBodyPart().apply {
            setContent(htmlBody, "text/html; charset=UTF-8")
        }
        multipart.addBodyPart(htmlPart)
        message.setContent(multipart)
    }

    override fun setSentDate() {
        message.sentDate = Date()
    }

    fun getMimeMessage(): MimeMessage = message
}