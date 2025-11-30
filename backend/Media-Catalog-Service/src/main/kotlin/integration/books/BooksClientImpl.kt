package integration.books

import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import integration.books.GoogleBooksSearchResponse
import integration.books.BooksConfig



class BooksClientImpl(
    private val httpClient: HttpClient,
    private val config: BooksConfig
) : BooksClient {

    override suspend fun searchBooks(query: String, limit: Int, offset: Int): GoogleBooksSearchResponse {
        val response = httpClient.get("${config.baseUrl}/volumes") {
            url {
                parameters.append("q", query)
                parameters.append("key", config.bookApiKey)
                parameters.append("maxResults", limit.toString())
                parameters.append("startIndex", offset.toString())
            }
        }

        if (!response.status.isSuccess()) {
            val body = response.body<String>()
            throw RuntimeException("Google Books API error: ${response.status} - $body")
        }

        return response.body()
    }
}
