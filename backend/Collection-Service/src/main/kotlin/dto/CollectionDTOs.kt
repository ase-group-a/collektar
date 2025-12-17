package com.collektar.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionInfo(
    val id: String,
    val type: String,
    val hidden: Boolean,
    val createdAt: Long
)

@Serializable
data class CollectionItemInfo(
    val id: String,
    val itemId: String,
    val title: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val source: String? = null,
    val addedAt: Long
)

@Serializable
data class AddCollectionItemRequest(
    val itemId: String,
    val title: String? = null,
    val imageUrl: String? = null,
    val description: String? = null,
    val source: String? = null
)

@Serializable
data class SetCollectionVisibilityRequest(
    val hidden: Boolean
)

@Serializable
data class CreatedCollectionItemResponse(
    val id: String
)
