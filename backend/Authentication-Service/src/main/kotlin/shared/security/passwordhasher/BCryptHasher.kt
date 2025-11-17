package com.collektar.shared.security.passwordhasher


import org.mindrot.jbcrypt.BCrypt

class BCryptHasher : IPasswordHasher {
    override fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    override fun verify(password: String, hash: String): Boolean {
        return BCrypt.checkpw(password, hash)
    }
}