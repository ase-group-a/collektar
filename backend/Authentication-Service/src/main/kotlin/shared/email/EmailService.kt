package com.collektar.shared.email

import com.collektar.dto.AccountDeletedEmail
import com.collektar.dto.PasswordChangedEmail
import com.collektar.dto.PasswordResetEmail
import com.collektar.dto.WelcomeEmail
import com.collektar.shared.producer.IEmailPublisher

class EmailService(
    private val publisher: IEmailPublisher
) : IEmailService {
    override fun sendWelcomeEmail(to: String, displayName: String) {
        publisher.publish(
            WelcomeEmail(
                to = to,
                displayName = displayName
            )
        )
    }

    override fun sendPasswordResetEmail(
        to: String,
        displayName: String,
        resetToken: String,
        expiryMinutes: Int
    ): Result<Unit> {
        return publisher.publish(
            PasswordResetEmail(
                to = to,
                displayName = displayName,
                resetToken = resetToken,
                expiryMinutes = expiryMinutes
            )
        )
    }

    override fun sendPasswordChangedEmail(to: String, displayName: String): Result<Unit> {
        return publisher.publish(
            PasswordChangedEmail(
                to = to,
                displayName = displayName
            )
        )
    }

    override fun sendAccountDeletedEmail(to: String, displayName: String): Result<Unit> {
        return publisher.publish(
            AccountDeletedEmail(
                to = to,
                displayName = displayName
            )
        )
    }
}