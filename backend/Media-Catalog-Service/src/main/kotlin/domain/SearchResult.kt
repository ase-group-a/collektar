package domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val items: List<MediaItem>
)
