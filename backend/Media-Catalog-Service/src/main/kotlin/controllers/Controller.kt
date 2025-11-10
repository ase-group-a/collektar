package controllers

import io.ktor.server.routing.Routing

interface Controller {
    fun register(routing: Routing)
}
