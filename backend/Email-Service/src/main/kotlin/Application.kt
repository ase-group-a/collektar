package com.collektar

import com.collektar.consumer.IConsumer
import com.collektar.plugins.configureHTTP
import com.collektar.plugins.configureMonitoring
import com.collektar.plugins.configureRouting
import com.collektar.plugins.configureSerialization
import com.collektar.plugins.configureFrameworks
import io.ktor.server.application.*
import org.koin.ktor.ext.inject

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
    configureFrameworks()

    val emailConsumer by inject<IConsumer>()
    emailConsumer.start()
    monitor.subscribe(ApplicationStopped) {
        emailConsumer.close()
    }
}
