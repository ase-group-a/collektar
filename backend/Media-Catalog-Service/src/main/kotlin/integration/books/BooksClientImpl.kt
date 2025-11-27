class BooksClientImpl(
    private val httpClient: HttpClient,
    private val config: BookConfig
) : BooksClient {

    override suspend fun searchBooks(query: String, limit: Int, offset: Int): GoogleBooksSearchResponse {
        val response = httpClient.get("${config.baseUrl}/volumes") {
            url {
                parameters.append("q", query)
                parameters.append("key", config.apiKey)
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
