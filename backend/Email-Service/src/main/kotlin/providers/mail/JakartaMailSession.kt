package com.collektar.providers.mail

import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage

class JakartaMailSession(private val session: Session) : IMailSession {
    override fun createMessage(): IMailMessage {
        return JakartaMailMessage(MimeMessage(session))
    }
}