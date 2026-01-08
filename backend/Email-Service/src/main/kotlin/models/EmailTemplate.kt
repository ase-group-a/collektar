package com.collektar.models

data class EmailContent(
    val subject: String,
    val title: String,
    val body: String,
    val footer: String = "Further questions? <a href=\"mailto:support@collektar.com\">support@collektar.com</a>"
) {
    fun renderTemplate(template: String): String {
        return template
            .replace("{{title}}", title)
            .replace("{{body}}", body)
            .replace("{{footer}}", footer)
    }
}

data class Email(
    val to: String,
    val subject: String,
    val htmlBody: String
)