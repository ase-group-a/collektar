package com.collektar

import com.collektar.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureSecurity()
    //configureDatabases()
    configureFrameworks()
    configureRouting()
}
