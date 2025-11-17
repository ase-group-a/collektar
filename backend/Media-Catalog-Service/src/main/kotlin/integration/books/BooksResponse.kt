package integration.books

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
// https://developers.google.com/books/docs/v1/using?hl=de
@Serializable
data class VolumeInfo(
    val title: String? = null,
    val authors: List<String>? = null,
    val description: String? = null
)

@Serializable
data class Volume(
    val id: String,
    val volumeInfo: VolumeInfo? = null
)

// GET https://www.googleapis.com/books/v1/volumes?q=flowers+inauthor:keyes&key=yourAPIKey
@Serializable
data class VolumesResponse(
    val items: List<Volume>? = null,
    @SerialName("totalItems") val totalItems: Int
)
