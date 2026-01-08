package com.collektar.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.utilityRoutes() {
    get("/health") {
        call.respondText("OK", ContentType.Text.Plain, HttpStatusCode.OK)
    }
}