package controllers

import exceptions.RateLimitException
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*

suspend fun <T : Any> ApplicationCall.safeCall(block: suspend () -> T) {
    try {
        val result = block()
        respond(result as Any)
    } catch (e: IllegalArgumentException) {
        respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "bad request")))
    } catch (e: RateLimitException) {
        respond(
            HttpStatusCode.TooManyRequests,
            mapOf("error" to (e.message ?: "rate limit"), "retryAfter" to e.retryAfterSeconds)
        )
    } catch (e: Exception) {
        application.environment.log.error("Controller error", e)
        respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal error"))
    }
}

fun ApplicationCall.queryParam(name: String, default: String? = null): String? =
    request.queryParameters[name] ?: default

fun ApplicationCall.queryParamInt(name: String, default: Int): Int =
    request.queryParameters[name]?.toIntOrNull() ?: default

fun Routing.mediaRoute(path: String, block: Route.() -> Unit) {
    route("/$path", block)
}
