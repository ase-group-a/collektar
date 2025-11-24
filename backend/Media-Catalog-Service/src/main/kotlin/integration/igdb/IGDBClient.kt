package com.collektar.integration.igdb

interface IGDBClient {
    suspend fun searchGame(query: String) // https://api-docs.igdb.com/?shell#search
}