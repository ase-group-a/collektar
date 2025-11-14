package com.collektar


import com.collektar.config.JWTConfig
import com.collektar.config.OpaqueTokenConfig
import com.collektar.config.TokenHasherConfig
import com.collektar.features.auth.AuthRepository
import com.collektar.features.auth.AuthService
import com.collektar.features.auth.IAuthRepository
import com.collektar.features.auth.IAuthService
import com.collektar.shared.database.DatabaseFactory
import com.collektar.shared.security.JWTService.IJWTService
import com.collektar.shared.security.JWTService.JWTService
import com.collektar.shared.security.PasswordHasher.BCryptHasher
import com.collektar.shared.security.PasswordHasher.IPasswordHasher
import com.collektar.shared.security.RefreshTokenHasher.HmacTokenHasher
import com.collektar.shared.security.RefreshTokenHasher.IRefreshTokenHasher
import com.collektar.shared.security.opaquetokengeneration.IOpaqueTokenGenerator
import com.collektar.shared.security.opaquetokengeneration.OpaqueTokenGenerator
import com.collektar.shared.security.tokenservice.ITokenService
import com.collektar.shared.security.tokenservice.TokenService
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()

        val jwtConfig = JWTConfig.fromEnv()
        val tokenHasherConfig = TokenHasherConfig.fromEnv()
        val opaqueTokenConfig = OpaqueTokenConfig()
        val database = DatabaseFactory.create()
        modules(module {
            single { jwtConfig }
            single { tokenHasherConfig }
            single { opaqueTokenConfig }
            single { database }
            single<IJWTService> { JWTService(get()) }
            single<IPasswordHasher> { BCryptHasher() }
            single<IRefreshTokenHasher> { HmacTokenHasher(get()) }
            single<IOpaqueTokenGenerator> { OpaqueTokenGenerator(get()) }
            single<IAuthRepository> { AuthRepository(get()) }
            single<ITokenService> { TokenService(get(), get(), get(), get()) }
            single<IAuthService> { AuthService(get(), get(), get()) }
        })
    }
}
