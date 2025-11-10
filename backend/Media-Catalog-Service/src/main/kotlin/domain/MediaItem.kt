package domain

import kotlinx.serialization.Serializable

@Serializable
data class MediaItem(
    val id: String,
    val title: String,
    val type: MediaType,
    val imageUrl: String? = null,
    val description: String? = null,
    val source: String? = null
)
