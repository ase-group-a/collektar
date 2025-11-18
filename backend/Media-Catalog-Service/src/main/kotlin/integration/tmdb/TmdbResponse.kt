package integration.tmdb

import kotlinx.serialization.Serializable

@Serializable
data class TmdbMovieDto(
    val id: Int,
    val title: String,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val releaseDate: String? = null,
    val voteAverage: Double? = null
)

@Serializable
data class TmdbMovieSearchResponse(
    val page: Int,
    val results: List<TmdbMovieDto> = emptyList(),
    val totalResults: Int,
    val totalPages: Int
)
