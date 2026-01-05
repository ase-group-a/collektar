package controllers

import io.ktor.server.routing.*
import service.BggMediaService

class BoardGameController(
    private val bggMediaService: BggMediaService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("boardgames") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    // if q is required, you can keep validation elsewhere;
                    // for now mimic movies/books
                    bggMediaService.search(q ?: "", limit, offset)
                }
            }
        }
    }
}
