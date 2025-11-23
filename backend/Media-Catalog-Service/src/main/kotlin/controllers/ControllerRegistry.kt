package controllers

import io.ktor.server.routing.Routing

class ControllerRegistry(private val controllers: List<Controller>) {
    fun registerAll(routing: Routing) {
        controllers.forEach { it.register(routing) }
    }
}
