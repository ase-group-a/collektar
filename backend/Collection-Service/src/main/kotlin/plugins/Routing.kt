package com.collektar.plugins

import com.collektar.features.collection.collectionRoutes
import com.collektar.features.collection.service.CollectionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val collectionService = CollectionService()

    routing {
        get("/health") {
            call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
        }

        collectionRoutes(collectionService)
    }
}
