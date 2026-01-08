package com.collektar.shared.producer

import com.collektar.dto.EmailMessage

interface IEmailPublisher {
    fun publish(emailMessage: EmailMessage): Result<Unit>
}