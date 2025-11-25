package com.collektar.integration.shared

import com.collektar.di.modules.OauthParameterType
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class OauthTokenProvider(
    private val httpClient: HttpClient,
    private val tokenCache: OauthTokenCache = OauthTokenCache()
) {

    private val mutex = Mutex()

    private suspend fun fetchAccessToken(config: OauthConfig): OauthTokenResponse {
        val response: HttpResponse = when (config.oauthParameterType) {
            OauthParameterType.BODY_URLENCODED -> httpClient.post(config.tokenUrl) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(
                    listOf(
                        "grant_type" to "client_credentials",
                        "client_id" to config.clientId,
                        "client_secret" to config.clientSecret
                    ).formUrlEncode()
                )
            }
            
            OauthParameterType.URL_PARAMETER_URLENCODED -> httpClient.post(config.tokenUrl) {
                url {
                    parameters.append("client_id", config.clientId)
                    parameters.append("client_secret", config.clientSecret)
                    parameters.append("grant_type", "client_credentials")
                }
            }
        }

        val bodyText = response.bodyAsText()
        if (!response.status.isSuccess()) throw RuntimeException("Failed to fetch Oauth token: ${response.status} - $bodyText")

        return Json.decodeFromString(OauthTokenResponse.serializer(), bodyText)
    }

    suspend fun getToken(config: OauthConfig): String {
        tokenCache.getIfValid(config.tokenUrl)?.let { return it }

        return mutex.withLock {
            tokenCache.getIfValid(config.tokenUrl)?.let { return it }
            val tokenResp = fetchAccessToken(config)
            tokenCache.put(config.tokenUrl, tokenResp.accessToken, tokenResp.expiresIn)
            tokenResp.accessToken
        }
    }
}
