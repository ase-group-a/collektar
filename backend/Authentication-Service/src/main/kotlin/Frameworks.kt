package com.collektar


import com.collektar.config.JWTConfig
import com.collektar.features.auth.AuthRepository
import com.collektar.features.auth.AuthService
import com.collektar.features.auth.IAuthRepository
import com.collektar.features.auth.IAuthService
import com.collektar.shared.database.DatabaseFactory
import com.collektar.shared.security.JWTService.IJWTService
import com.collektar.shared.security.JWTService.JWTService
import com.collektar.shared.security.PasswordHasher.BCryptHasher
import com.collektar.shared.security.PasswordHasher.IPasswordHasher
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()

        val jwtConfig = JWTConfig.fromEnv()
        val database = DatabaseFactory.create()
        modules(module {
            single { jwtConfig }
            single { database }
            single<IJWTService> { JWTService(get()) }
            single<IPasswordHasher> { BCryptHasher() }
            single<IAuthRepository> { AuthRepository(get()) }
            single<IAuthService> { AuthService(get(), get(), get()) }
        })
    }
}
