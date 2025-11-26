package com.collektar.integration.igdb

import com.collektar.integration.shared.OauthTokenProvider
import exceptions.RateLimitException
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class IGDBClientImpl(
    private val httpClient: HttpClient, private val config: IGDBConfig, private val tokenProvider: OauthTokenProvider
) : IGDBClient {
    override suspend fun searchGames(query: String?, limit: Int, offset: Int) : IGDBGamesResponse {
        if (limit !in 1..500) {
            // 500 is the maximum allowed value of the IGDB API
            throw IllegalArgumentException("Limit must be between 1 and 500")
        }

        val token = tokenProvider.getToken(config)

        val response: HttpResponse = httpClient.post("${config.baseUrl}/games") {
            header("Authorization", "Bearer $token")
            header("Client-ID", config.clientId)
            contentType(ContentType.Text.Plain)
            setBody(
                "fields id, name, summary, cover.image_id;" +
                        "limit ${limit};" +
                        "offset ${offset};" +
                        if (query != null) // Add optional search query
                            "search \"${query}\";"
                        else ""
            )
        }
        
        if (!response.status.isSuccess()) {
            val text = response.bodyAsText()
            if (response.status == HttpStatusCode.TooManyRequests) {
                throw RateLimitException("IGDB rate limit exceeded")
            }
            throw RuntimeException("IGDB request failed: ${response.status} - $text")
        }
        
        val games = response.body<List<GameDto>>()
        val count = response.headers["X-Count"]?.toInt() ?: 0
        
        return IGDBGamesResponse(games, count)
    }
}