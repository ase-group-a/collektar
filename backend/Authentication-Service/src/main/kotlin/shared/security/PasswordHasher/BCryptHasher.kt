package com.collektar.shared.security.Hasher


import com.collektar.shared.security.PasswordHasher.IPasswordHasher
import org.mindrot.jbcrypt.BCrypt

class BCryptHasher : IPasswordHasher {
    override fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    override fun verify(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
}