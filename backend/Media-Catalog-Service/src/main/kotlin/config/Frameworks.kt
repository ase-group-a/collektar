package com.collektar
import di.clientModule
import di.configModule
import di.controllerModule
import di.serviceModule
import io.ktor.server.application.*
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()
        modules(
            configModule(environment),
            clientModule,
            serviceModule,
            controllerModule
        )
    }
}