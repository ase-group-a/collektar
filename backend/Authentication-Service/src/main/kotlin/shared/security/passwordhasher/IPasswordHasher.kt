package com.collektar.shared.security.passwordhasher

interface IPasswordHasher {
    fun hash(password: String): String
    fun verify(password: String, hash: String): Boolean
}