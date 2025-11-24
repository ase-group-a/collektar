package com.collektar.features.user.repository

import com.collektar.features.auth.repository.AuthModel
import com.collektar.shared.database.Tables.Users
import com.collektar.shared.repository.BaseRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

class UserRepository(database: Database) : BaseRepository(database), IUserRepository {
    override suspend fun findByUsername(username: String): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.username eq username }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun findByUserId(userId: UUID): AuthModel? = dbQuery {
        Users
            .selectAll()
            .where { Users.id eq userId }
            .map { it.toAuthModel() }
            .singleOrNull()
    }

    override suspend fun usernameExists(username: String): Boolean = dbQuery {
        Users
            .selectAll()
            .where { Users.username eq username }
            .count() > 0
    }

    override suspend fun emailExists(email: String): Boolean = dbQuery {
        Users
            .selectAll()
            .where { Users.email eq email }
            .count() > 0
    }

    override suspend fun updateUsername(userId: UUID, newUsername: String): Unit = dbQuery {
        Users.update({
            Users.id eq userId
        }) {
            it[username] = newUsername
        }
    }

    override suspend fun updateDisplayName(userId: UUID, newDisplayName: String): Unit = dbQuery {
        Users.update({
            Users.id eq userId
        }) {
            it[displayName] = newDisplayName
        }
    }

    override suspend fun updateEmail(userId: UUID, email: String): Unit = dbQuery {
        Users.update({
            Users.id eq userId
        }) {
            it[Users.email] = email
        }
    }

    private fun ResultRow.toAuthModel() = AuthModel(
        id = this[Users.id],
        username = this[Users.username],
        email = this[Users.email],
        displayName = this[Users.displayName],
        passwordHash = this[Users.passwordHash],
    )
}