package controllers

import domain.SearchResult
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.BggMediaService

class BoardGameController(
    private val bggMediaService: BggMediaService
) : Controller {

    override fun register(routing: Routing) {

        routing.route("/boardgames") {

            // GET /boardgames/search
            get("/search") {
                val query = call.request.queryParameters["q"] ?: ""
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                if (query.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Missing query parameter 'q'")
                    )
                    return@get
                }

                try {
                    val result = bggMediaService.search(query, limit, offset)
                    call.respond(result)
                } catch (e: Exception) {
                    call.application.log.error("Error while searching board games from BGG", e)
                    call.respond(
                        HttpStatusCode.OK,
                        SearchResult(
                            total = 0,
                            limit = limit,
                            offset = offset,
                            items = emptyList()
                        )
                    )
                }
            }

            // GET /boardgames/{id}
            get("/{id}") {
                val idParam = call.parameters["id"]
                val id = idParam?.toLongOrNull()

                if (id == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Invalid board game id")
                    )
                    return@get
                }

                try {
                    val item = bggMediaService.getById(id)

                    if (item == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            mapOf("error" to "Board game not found")
                        )
                    } else {
                        call.respond(item)
                    }
                } catch (e: Exception) {
                    call.application.log.error("Error while fetching board game $id from BGG", e)

                    // On error, behave as "not found" instead of 500
                    call.respond(
                        HttpStatusCode.NotFound,
                        mapOf("error" to "Board game not found")
                    )
                }
            }
        }
    }
}
