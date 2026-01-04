package controllers

import io.ktor.server.routing.*
import service.ShowService

class ShowController(
    private val showService: ShowService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("shows") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    showService.searchShows(q, limit, offset)
                }
            }
        }
    }
}
