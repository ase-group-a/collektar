package com.collektar.integration.igdb

interface IGDBClient {
    suspend fun searchGames(query: String?, limit: Int = 20, offset: Int = 0) : IGDBGamesResponse
}