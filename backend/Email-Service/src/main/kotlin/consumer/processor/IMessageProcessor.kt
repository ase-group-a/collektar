package com.collektar.consumer.processor

interface IMessageProcessor {
    suspend fun process(messageBody: ByteArray): ProcessingResult
}