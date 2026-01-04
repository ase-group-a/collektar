package integration.tmdb

import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class TmdbClientImpl(
    private val httpClient: HttpClient,
    private val config: TmdbConfig
) : TmdbClient {

    override suspend fun searchMovies(query: String?, page: Int): TmdbMovieSearchResponse =
        if (query.isNullOrBlank()) {
            performRequest(
                endpoint = "/movie/popular",
                includeQuery = false,
                query = null,
                page = page
            )
        } else {
            performRequest(
                endpoint = "/search/movie",
                includeQuery = true,
                query = query,
                page = page
            )
        }

    override suspend fun searchShows(query: String?, page: Int): TmdbShowSearchResponse =
        if (query.isNullOrBlank()) {
            performRequest(
                endpoint = "/tv/popular",
                includeQuery = false,
                query = null,
                page = page
            )
        } else {
            performRequest(
                endpoint = "/search/tv",
                includeQuery = true,
                query = query,
                page = page
            )
        }

    private suspend inline fun <reified T> performRequest(
        endpoint: String,
        includeQuery: Boolean,
        query: String?,
        page: Int
    ): T {
        val response: HttpResponse = httpClient.get("${config.baseUrl}$endpoint") {
            header(HttpHeaders.Authorization, "Bearer ${config.bearerToken}")
            url {
                if (includeQuery && query != null) {
                    parameters.append("query", query)
                    parameters.append("include_adult", "false")
                }
                parameters.append("page", page.toString())
            }
        }

        val text = response.bodyAsText()

        if (!response.status.isSuccess()) {
            if (response.status == HttpStatusCode.TooManyRequests) {
                val retryAfter = response.headers["Retry-After"]?.toLongOrNull() ?: 1L
                throw RateLimitException("TMDB rate limited", retryAfter)
            }
            throw RuntimeException("TMDB search failed: ${response.status} - $text")
        }

        return response.body()
    }
}
