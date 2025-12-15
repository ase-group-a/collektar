package com.collektar.consumer.processor

sealed class ProcessingResult {
    data object Success : ProcessingResult()
    data class RetryableFailure(val error: Throwable) : ProcessingResult()
    data class PermanentFailure(val error: Throwable) : ProcessingResult()
}