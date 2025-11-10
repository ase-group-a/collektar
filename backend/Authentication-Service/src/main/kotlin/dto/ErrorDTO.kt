package com.collektar.DTOs

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String,
)