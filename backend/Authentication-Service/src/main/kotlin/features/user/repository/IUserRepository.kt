package com.collektar.features.user.repository

import com.collektar.features.auth.repository.AuthModel
import java.util.*

interface IUserRepository {
    suspend fun findByUserId(userId: UUID): AuthModel?
    suspend fun findByUsername(username: String): AuthModel?
    suspend fun findByEmail(email: String): AuthModel?
    suspend fun usernameExists(username: String): Boolean
    suspend fun emailExists(email: String): Boolean

    suspend fun updateUsername(userId: UUID, newUsername: String)
    suspend fun updateDisplayName(userId: UUID, newDisplayName: String)
    suspend fun updateEmail(userId: UUID, email: String)
}