package com.collektar.features.collection

import com.collektar.dto.*
import com.collektar.features.collection.service.CollectionService
import com.collektar.shared.security.UserContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

private const val USER_HEADER = "X-User-Id"

fun Route.collectionRoutes(collectionService: CollectionService) {

    get("/collections") {
        val userId = requireUserId(call) ?: return@get
        collectionService.ensureDefaults(userId)
        call.respond(HttpStatusCode.OK, collectionService.listCollections(userId))
    }

    post("/collections") {
        val userId = requireUserId(call) ?: return@post
        val payload = call.receive<CreateCollectionRequest>()
        val newId = collectionService.createCollection(userId, payload.type)
        call.respond(HttpStatusCode.Created, mapOf("id" to newId.toString()))
    }

    delete("/collections/{id}") {
        val userId = requireUserId(call) ?: return@delete
        val id = call.parameters["id"]
            ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing collection id")
            )

        val deleted = collectionService.deleteCollection(userId, id)
        if (deleted) {
            call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
        } else {
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse("Collection not found")
            )
        }
    }

    patch("/collections/{type}/visibility") {
        val userId = requireUserId(call) ?: return@patch
        val type = call.parameters["type"]
            ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing collection type")
            )

        val payload = call.receive<SetCollectionVisibilityRequest>()
        collectionService.setVisibility(userId, type, payload.hidden)
        call.respond(HttpStatusCode.OK, mapOf("hidden" to payload.hidden))
    }

    post("/collections/{type}/items") {
        val userId = requireUserId(call) ?: return@post
        val type = call.parameters["type"]
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing collection type")
            )

        val payload = call.receive<AddCollectionItemRequest>()
        val item = collectionService.addItem(
            userId,
            type,
            payload.itemId,
            payload.title,
            payload.imageUrl,
            payload.description,
            payload.source
        )
        call.respond(HttpStatusCode.Created, item)
    }

    get("/collections/{type}/items") {
        val userId = requireUserId(call) ?: return@get
        val type = call.parameters["type"]
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing collection type")
            )

        call.respond(HttpStatusCode.OK, collectionService.listItems(userId, type))
    }

    delete("/collections/{type}/items/{itemId}") {
        val userId = requireUserId(call) ?: return@delete
        val type = call.parameters["type"]
            ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing collection type")
            )
        val itemId = call.parameters["itemId"]
            ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("Missing itemId")
            )

        collectionService.removeItem(userId, type, itemId)
        call.respond(HttpStatusCode.OK, mapOf("removed" to true))
    }
}

private suspend fun requireUserId(call: ApplicationCall): UUID? {
    val userId = UserContext.userIdFromHeader(call, USER_HEADER)
    if (userId == null) {
        call.respond(
            HttpStatusCode.Unauthorized,
            ErrorResponse("Missing or invalid $USER_HEADER")
        )
        return null
    }
    return userId
}
