package com.collektar.consumer.processor

interface IEmailMessageProcessor {
    suspend fun process(messageBody: ByteArray): ProcessingResult
}