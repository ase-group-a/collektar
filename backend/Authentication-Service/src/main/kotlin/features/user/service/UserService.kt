package com.collektar.features.user.service

import com.collektar.dto.*
import com.collektar.features.user.repository.IUserRepository
import com.collektar.shared.errors.AppError
import java.util.*

class UserService(
    private val repository: IUserRepository,
) : IUserService {
    override suspend fun updateUsername(userId: UUID, request: UpdateUsernameRequest): UpdateUsernameResponse {
        if (repository.usernameExists(request.newUsername)) {
            throw AppError.Conflict.UsernameTaken(request.newUsername)
        }

        repository.updateUsername(userId, request.newUsername)
        return UpdateUsernameResponse(request.newUsername)
    }

    override suspend fun updateDisplayName(userId: UUID, request: UpdateDisplayNameRequest): UpdateDisplayNameResponse {
        repository.updateDisplayName(userId, request.newDisplayName)
        return UpdateDisplayNameResponse(request.newDisplayName)
    }

    override suspend fun updateEmail(userId: UUID, request: UpdateEmailRequest): UpdateEmailResponse {
        /*
            TODO: add email sending/verification logic
         */
        if (repository.emailExists(request.newEmail)) {
            throw AppError.Conflict.EmailAlreadyInUse()
        }

        repository.updateEmail(userId, request.newEmail)
        return UpdateEmailResponse(request.newEmail)
    }
}