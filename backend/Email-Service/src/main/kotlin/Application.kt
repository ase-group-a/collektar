package com.collektar

import com.collektar.plugins.configureHTTP
import com.collektar.plugins.configureMonitoring
import com.collektar.plugins.configureRouting
import com.collektar.plugins.configureSerialization
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
