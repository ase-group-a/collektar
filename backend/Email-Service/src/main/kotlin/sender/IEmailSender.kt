package com.collektar.sender

import com.collektar.models.Email

interface IEmailSender {
    suspend fun send(email: Email): Result<Unit>
}