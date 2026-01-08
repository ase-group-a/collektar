package com.collektar.providers.mail

import jakarta.mail.Transport

class JakartaMailTransport: IMailTransport{
    override fun send(message: IMailMessage) {
        when (message) {
            is JakartaMailMessage -> Transport.send(message.getMimeMessage())
            else -> throw IllegalArgumentException("Message must be JakartaMailMessage")
        }
    }
}