package com.collektar.features.collection

import com.collektar.dto.AddCollectionItemRequest
import com.collektar.dto.CollectionInfo
import com.collektar.dto.CollectionItemInfo
import com.collektar.dto.ErrorResponse
import com.collektar.dto.SetCollectionVisibilityRequest
import com.collektar.features.collection.model.CollectionType
import com.collektar.features.collection.service.CollectionService
import com.collektar.shared.security.UserContext
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

object CollectionRoutes {

    private val service = CollectionService()
    private const val USER_HEADER = "X-User-Id"

    fun register(route: Route) {
        route.route("/collections") {

            get {
                val userId = requireUserId(call) ?: return@get
                service.ensureDefaults(userId)
                val cols: List<CollectionInfo> = service.listCollections(userId)
                call.respond(HttpStatusCode.OK, cols)
            }

            patch("{type}/visibility") {
                val userId = requireUserId(call) ?: return@patch
                val type = requireCollectionType(call) ?: return@patch
                val payload = call.receive<SetCollectionVisibilityRequest>()
                try {
                    service.setVisibility(userId, type, payload.hidden)
                    call.respond(HttpStatusCode.OK, mapOf("hidden" to payload.hidden))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Not found"))
                }
            }

            post("{type}/items") {
                val userId = requireUserId(call) ?: return@post
                val type = requireCollectionType(call) ?: return@post
                val payload = call.receive<AddCollectionItemRequest>()
                try {
                    val item: CollectionItemInfo = service.addItem(
                        userId,
                        type,
                        payload.itemId,
                        payload.title,
                        payload.imageUrl,
                        payload.description,
                        payload.source
                    )
                    call.respond(HttpStatusCode.Created, item)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Not found"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(e.message ?: "Bad request"))
                }
            }

            get("{type}/items") {
                val userId = requireUserId(call) ?: return@get
                val type = requireCollectionType(call) ?: return@get
                try {
                    val items: List<CollectionItemInfo> = service.listItems(userId, type)
                    call.respond(HttpStatusCode.OK, items)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Not found"))
                }
            }

            delete("{type}/items/{itemId}") {
                val userId = requireUserId(call) ?: return@delete
                val type = requireCollectionType(call) ?: return@delete
                val itemId = call.parameters["itemId"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing itemId"))
                    return@delete
                }
                try {
                    service.removeItem(userId, type, itemId)
                    call.respond(HttpStatusCode.OK, mapOf("removed" to true))
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(e.message ?: "Not found"))
                }
            }
        }
    }

    private suspend fun requireUserId(call: ApplicationCall): UUID? {
        val userId = UserContext.userIdFromHeader(call, USER_HEADER)
        if (userId == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Missing or invalid $USER_HEADER"))
            return null
        }
        return userId
    }

    private suspend fun requireCollectionType(call: ApplicationCall): CollectionType? {
        val typeStr = call.parameters["type"] ?: run {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing collection type"))
            return null
        }

        val type = CollectionType.fromString(typeStr)
        if (type == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Unknown collection type"))
            return null
        }
        return type
    }
}
