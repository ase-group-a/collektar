package com.collektar.integration.igdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameDto(
    val id: Long,
    val cover: CoverDto? = null,
    val name: String,
    val summary: String? = null,
)

@Serializable
data class CoverDto(
    val id: Long,
    @SerialName("image_id")
    val imageId: String
)

@Serializable
data class IGDBGamesResponse(
    val items: List<GameDto>,
    val total: Int = 0
)