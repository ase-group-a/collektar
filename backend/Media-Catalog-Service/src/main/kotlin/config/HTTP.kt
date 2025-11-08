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
import kotlinx.serialization.json.Json

fun Application.configureHTTP() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)

        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader("MyCustomHeader")

        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }
    routing {
        swaggerUI(path = "openapi")
    }
}

object HttpProvider {

    private val defaultConfig: HttpClientConfig<CIOEngineConfig>.() -> Unit = {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        engine { requestTimeout = 60_000 }
    }

    // Default client
    val client: HttpClient by lazy { HttpClient(CIO, defaultConfig) }

    // Use this if your API needs a specific configuration
    fun createClient(configure: HttpClientConfig<CIOEngineConfig>.() -> Unit): HttpClient {
        return HttpClient(CIO) {
            defaultConfig()
            configure()
        }
    }
}
