package com.collektar.builder

import com.collektar.models.Email
import com.collektar.models.EmailMessage

interface IEmailBuilder {
    fun buildEmail(message: EmailMessage): Email
}