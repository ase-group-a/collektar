package integration.books

import kotlinx.serialization.Serializable

@Serializable
data class GoogleBooksSearchResponse(
    val totalItems: Int? = 0,
    val items: List<BookItemDto>? = emptyList()
)

@Serializable
data class BookItemDto(
    val id: String,
    val volumeInfo: VolumeInfoDto? = null
)

@Serializable
data class VolumeInfoDto(
    val title: String? = null,
    val authors: List<String>? = emptyList(),
    val description: String? = null,
    val imageLinks: ImageLinksDto? = null
)

@Serializable
data class ImageLinksDto(
    val smallThumbnail: String? = null,
    val thumbnail: String? = null
)
