package integration.books

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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

@Serializable
data class VolumesResponse(
    val items: List<Volume>? = null,
    @SerialName("totalItems") val totalItems: Int
)
