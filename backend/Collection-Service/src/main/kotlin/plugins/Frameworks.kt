package com.collektar.plugins

import com.collektar.shared.database.DatabaseFactory
import io.ktor.server.application.*

fun Application.configureFrameworks() {
    DatabaseFactory.create()
}
