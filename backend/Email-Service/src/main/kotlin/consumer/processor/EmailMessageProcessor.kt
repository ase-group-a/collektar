package com.collektar.consumer.processor

import com.collektar.builder.IEmailBuilder
import com.collektar.models.EmailMessage
import com.collektar.sender.IEmailSender
import kotlinx.serialization.json.Json
import java.nio.charset.StandardCharsets

class EmailMessageProcessor(
    private val emailBuilder: IEmailBuilder,
    private val emailSender: IEmailSender,
    private val json: Json = Json { ignoreUnknownKeys = false; classDiscriminator = "message_type" }
) : IMessageProcessor {
    override suspend fun process(messageBody: ByteArray): ProcessingResult {
        try {
            val messageJson = String(messageBody, StandardCharsets.UTF_8)
            val emailMessage = json.decodeFromString<EmailMessage>(messageJson)

            val email = emailBuilder.buildEmail(emailMessage)

            return emailSender.send(email).fold(
                onSuccess = { ProcessingResult.Success },
                onFailure = { ProcessingResult.RetryableFailure(it) }
            )
        } catch (e: Exception) {
            return ProcessingResult.PermanentFailure(e)
        }
    }
}