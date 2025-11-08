package com.collektar

import controllers.ControllerRegistry
import io.ktor.server.application.*
import io.ktor.server.routing.routing
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureSecurity()
    configureFrameworks()
    configureRouting()

    val registry: ControllerRegistry = get()
    routing {
        registry.registerAll(this)
    }

}
