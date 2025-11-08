package domain

import kotlinx.serialization.Serializable

@Serializable
data class SearchResult<T>(
    val total: Int,
    val limit: Int,
    val offset: Int,
    val items: List<T>
)
