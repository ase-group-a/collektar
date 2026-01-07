package com.collektar.shared.email

interface IEmailService {
    fun sendWelcomeEmail(to: String, displayName: String)
    fun sendAccountDeletedEmail(to: String, displayName: String): Result<Unit>
    fun sendPasswordChangedEmail(to: String, displayName: String): Result<Unit>
    fun sendPasswordResetEmail(to: String, displayName: String, resetToken: String, expiryMinutes: Int): Result<Unit>
}