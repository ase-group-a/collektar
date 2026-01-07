package com.collektar.shared.utility

interface IEmailTemplateLoader {
    fun loadTemplate(templateName: String): String
}