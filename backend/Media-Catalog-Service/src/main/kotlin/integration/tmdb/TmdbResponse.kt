package integration.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String,
    @SerialName("overview") val overview: String? = null,
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double? = null
)

@Serializable
data class TmdbMovieSearchResponse(
    val page: Int,
    val results: List<TmdbMovieDto> = emptyList(),
    @SerialName("total_results") val totalResults: Int,
    @SerialName("total_pages") val totalPages: Int
)

