package com.collektar.providers

interface IEmailProvider {
    suspend fun sendEmail(
        to: String,
        subject: String,
        htmlBody: String,
    ): Result<Unit>
}