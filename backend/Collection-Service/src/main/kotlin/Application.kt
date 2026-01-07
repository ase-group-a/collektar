package com.collektar

import com.collektar.plugins.configureFrameworks
import com.collektar.plugins.configureHTTP
import com.collektar.plugins.configureMonitoring
import com.collektar.plugins.configureRouting
import com.collektar.plugins.configureSecurity
import com.collektar.plugins.configureSerialization
import com.collektar.shared.errors.configureStatusPages
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFrameworks()
    configureHTTP()
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureMonitoring()
    configureRouting()
}
