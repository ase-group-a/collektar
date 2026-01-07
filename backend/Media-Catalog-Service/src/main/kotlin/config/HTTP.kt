package com.collektar

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy

fun Application.configureHTTP() {
    val isProd = System.getenv("KTOR_ENVIRONMENT") == "production"
    val domain = System.getenv("DOMAIN") ?: "localhost"

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)

        allowCredentials = true

        if (isProd) {
            allowHost(domain, schemes = listOf("https"))
        } else {
            anyHost()
        }
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
    routing {
        swaggerUI(path = "openapi")
    }
}

@OptIn(ExperimentalSerializationApi::class)
object HttpProvider {

    private val defaultConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    namingStrategy = JsonNamingStrategy.SnakeCase
                }
            )
        }
        engine {
            requestTimeout = 60_000
        }
    }

    val client: HttpClient by lazy { HttpClient(CIO, defaultConfig) }
}
