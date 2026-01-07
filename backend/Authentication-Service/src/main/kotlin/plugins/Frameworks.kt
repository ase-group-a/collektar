package com.collektar.plugins


import com.collektar.config.*
import com.collektar.features.auth.repository.AuthRepository
import com.collektar.features.auth.repository.IAuthRepository
import com.collektar.features.auth.service.AuthService
import com.collektar.features.auth.service.IAuthService
import com.collektar.shared.database.DatabaseFactory
import com.collektar.shared.email.EmailService
import com.collektar.shared.email.IEmailService
import com.collektar.shared.producer.EmailPublisher
import com.collektar.shared.producer.IEmailPublisher
import com.collektar.shared.producer.connectionmanager.RabbitMQConnection
import com.collektar.shared.security.cookies.CookieProvider
import com.collektar.shared.security.cookies.ICookieProvider
import com.collektar.shared.security.jwt.IJWTService
import com.collektar.shared.security.jwt.JWTService
import com.collektar.shared.security.opaquetokengeneration.IOpaqueTokenGenerator
import com.collektar.shared.security.opaquetokengeneration.OpaqueTokenGenerator
import com.collektar.shared.security.passwordhasher.BCryptHasher
import com.collektar.shared.security.passwordhasher.IPasswordHasher
import com.collektar.shared.security.tokenhasher.HmacTokenHasher
import com.collektar.shared.security.tokenhasher.IRefreshTokenHasher
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
        val appConfig = AppConfig.fromEnv()
        val rabbitMQConfig = RabbitMQConfig.fromEnv()
        modules(module {
            single { jwtConfig }
            single { tokenHasherConfig }
            single { opaqueTokenConfig }
            single { database }
            single { appConfig }
            single { rabbitMQConfig }
            single<RabbitMQConnection> { RabbitMQConnection(get()) }
            single<IJWTService> { JWTService(get()) }
            single<IPasswordHasher> { BCryptHasher() }
            single<IRefreshTokenHasher> { HmacTokenHasher(get()) }
            single<IOpaqueTokenGenerator> { OpaqueTokenGenerator(get()) }
            single<IAuthRepository> { AuthRepository(get()) }
            single<ITokenService> { TokenService(get(), get(), get(), get()) }
            single<IEmailPublisher> { EmailPublisher(get(), get()) }
            single<IEmailService> { EmailService(get()) }
            single<IAuthService> { AuthService(get(), get(), get(), get()) }
            single<ICookieProvider> { CookieProvider(get()) }
        })
    }
}
