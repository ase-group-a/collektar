package com.collektar.shared.security.PasswordHasher

interface IPasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}