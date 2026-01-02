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
data class TmdbShowDto(
    val id: Int,
    val name: String,
    val overview: String? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val firstAirDate: String? = null,
    val voteAverage: Double? = null
)


@Serializable
data class TmdbMovieSearchResponse(
    val page: Int,
    val results: List<TmdbMovieDto> = emptyList(),
    val totalResults: Int,
    val totalPages: Int
)

@Serializable
data class TmdbShowSearchResponse(
    val page: Int,
    val results: List<TmdbShowDto> = emptyList(),
    val totalResults: Int,
    val totalPages: Int
)
