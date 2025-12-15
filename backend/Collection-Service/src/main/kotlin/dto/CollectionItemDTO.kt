package com.collektar.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionItemDTO(
    val id: String,
    val itemId: String,
    val createdAt: Long
)
