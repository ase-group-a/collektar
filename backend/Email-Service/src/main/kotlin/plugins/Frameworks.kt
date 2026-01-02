package com.collektar.plugins


import com.collektar.builder.EmailBuilder
import com.collektar.builder.IEmailBuilder
import com.collektar.config.*
import com.collektar.consumer.EmailConsumer
import com.collektar.consumer.IEmailConsumer
import com.collektar.consumer.connectionmanager.RabbitMQConnectionManager
import com.collektar.consumer.processor.EmailMessageProcessor
import com.collektar.consumer.processor.IEmailMessageProcessor
import com.collektar.providers.IEmailProvider
import com.collektar.providers.SESEmailProvider
import com.collektar.sender.EmailSender
import com.collektar.sender.IEmailSender
import com.collektar.shared.utility.EmailTemplateLoader
import com.collektar.shared.utility.IEmailTemplateLoader
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
    install(Koin) {
        slf4jLogger()

        modules(module {
            single { RabbitMQConfig.fromEnv() }
            single { EmailProviderConfig.fromEnv() }
            single { EnvironmentConfig.fromEnv() }
            single { TemplateLoaderConfig.fromEnv() }
            single<IEmailProvider> { SESEmailProvider(get()) }
            single<IEmailTemplateLoader> { EmailTemplateLoader(get()) }
            single<IEmailBuilder> { EmailBuilder(get(), get()) }
            single<IEmailSender> { EmailSender(get()) }
            single { RabbitMQConnectionManager(get()) }
            single<IEmailMessageProcessor> { EmailMessageProcessor(get(), get()) }
            single<IEmailConsumer> { EmailConsumer(get(), get(), get()) }
        })
    }
}
