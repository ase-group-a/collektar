package com.collektar.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUsernameRequest(
    val newUsername: String
)

@Serializable
data class UpdateUsernameResponse(
    val username: String
)

@Serializable
data class UpdateDisplayNameRequest(
    val newDisplayName: String
)

@Serializable
data class UpdateDisplayNameResponse(
    val displayName: String
)

@Serializable
data class UpdateEmailRequest(
    val newEmail: String
)

@Serializable
data class UpdateEmailResponse(
    val email: String
)