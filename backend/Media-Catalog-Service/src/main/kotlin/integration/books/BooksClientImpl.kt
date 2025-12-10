package integration.books

import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import integration.books.GoogleBooksSearchResponse
import integration.books.BooksConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.serializer


class BooksClientImpl(
    private val httpClient: HttpClient,
    private val config: BooksConfig
) : BooksClient {

    override suspend fun searchBooks(query: String, limit: Int, offset: Int): GoogleBooksSearchResponse {
        val searchQuery = if (query.isBlank()) "bestseller" else query

        val response: HttpResponse = httpClient.get("${config.baseUrl}/volumes") {
            url {
                parameters.append("q", searchQuery)
                parameters.append("key", config.bookApiKey)
                parameters.append("maxResults", limit.toString())
                parameters.append("startIndex", offset.toString())
            }
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            throw RuntimeException("Google Books API error: ${response.status} - $body")
        }

        val rawJson = response.bodyAsText()

        val json = Json { ignoreUnknownKeys = true; isLenient = true }
        val searchResult = json.decodeFromString(GoogleBooksSearchResponse.serializer(), rawJson)

        return searchResult
    }
}