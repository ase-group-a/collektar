package com.collektar.shared.errors

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String
)
