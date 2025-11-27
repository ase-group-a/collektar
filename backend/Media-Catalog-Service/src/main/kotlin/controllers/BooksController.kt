package controllers

import io.ktor.server.routing.*
import io.ktor.server.response.*
import service.BookService

class BooksController (
    private val bookService: BookService
) : Controller {

    override fun register(routing: Routing) {
        routing.mediaRoute("books") {
            get {
                val q = call.queryParam("q")
                val limit = call.queryParamInt("limit", 20)
                val offset = call.queryParamInt("offset", 0)

                call.safeCall {
                    bookService.search(q, limit, offset)
                }
            }
        }
    }
}