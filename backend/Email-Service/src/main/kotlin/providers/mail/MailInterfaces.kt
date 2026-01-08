package com.collektar.providers.mail

interface IMailSession {
    fun createMessage(): IMailMessage
}

interface IMailMessage {
    fun setSubject(subject: String, charset: String)
    fun setFrom(address: String, name: String)
    fun setRecipients(to: String)
    fun setHtmlContent(htmlBody: String)
    fun setSentDate()
}

interface IMailTransport {
    fun send(message: IMailMessage)
}