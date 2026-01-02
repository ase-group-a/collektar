package com.collektar.builder

import com.collektar.config.EnvironmentConfig
import com.collektar.models.*
import com.collektar.shared.utility.EmailTemplateLoader
import com.collektar.shared.utility.IEmailTemplateLoader

class EmailBuilder(
    private val environmentConfig: EnvironmentConfig,
    private val emailTemplateLoader: IEmailTemplateLoader
): IEmailBuilder {
    private val emailTemplate by lazy { emailTemplateLoader.loadTemplate("email-template.html") }

    override fun buildEmail(message: EmailMessage): Email {
        val content = message.buildContent(environmentConfig.appBaseUrl)
        val htmlBody = content.renderTemplate(emailTemplate)

        return Email(
            to = message.to,
            subject = content.subject,
            htmlBody = htmlBody,
        )
    }
}