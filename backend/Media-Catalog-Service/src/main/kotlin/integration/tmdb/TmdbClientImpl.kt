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

    override suspend fun searchMovies(query: String, page: Int): TmdbMovieSearchResponse {
        return performSearch("/search/movie", query, page)
    }

    private suspend inline fun <reified T> performSearch(
        endpoint: String,
        query: String,
        page: Int
    ): T {
        val response: HttpResponse = httpClient.get("${config.baseUrl}$endpoint") {
            header(HttpHeaders.Authorization, "Bearer ${config.bearerToken}")
            url {
                parameters.append("query", query)
                parameters.append("page", page.toString())
                parameters.append("include_adult", "false")
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