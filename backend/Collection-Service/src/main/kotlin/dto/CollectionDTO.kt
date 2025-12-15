package com.collektar.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionDTO(
    val id: String,
    val type: String,
    val hidden: Boolean,
    val createdAt: Long
)
