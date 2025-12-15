package com.collektar.sender

import com.collektar.models.Email
import com.collektar.providers.IEmailProvider

class EmailSender(
    private val emailProvider: IEmailProvider
) : IEmailSender {
    override suspend fun send(email: Email): Result<Unit> {
        return emailProvider.sendEmail(
            to = email.to,
            subject = email.subject,
            htmlBody = email.htmlBody
        )
    }
}