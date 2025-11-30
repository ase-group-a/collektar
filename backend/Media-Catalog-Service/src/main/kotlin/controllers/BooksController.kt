package controllers

import io.ktor.server.routing.*
import io.ktor.server.response.*
import service.BooksService

class BooksController (
    private val bookService: BooksService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("books") {
            get {
                //val q = call.queryParam("q")
                val q = call.queryParam("q") ?: "bestseller"
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    bookService.search(q, limit, offset)
                }
            }
        }
    }
}