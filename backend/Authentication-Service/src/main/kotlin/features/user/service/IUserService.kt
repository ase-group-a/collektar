package com.collektar.features.user.service

import com.collektar.dto.*
import java.util.*

interface IUserService {
    suspend fun updateUsername(userId: UUID, request: UpdateUsernameRequest): UpdateUsernameResponse
    suspend fun updateDisplayName(userId: UUID, request: UpdateDisplayNameRequest): UpdateDisplayNameResponse
    suspend fun updateEmail(userId: UUID, request: UpdateEmailRequest): UpdateEmailResponse
}