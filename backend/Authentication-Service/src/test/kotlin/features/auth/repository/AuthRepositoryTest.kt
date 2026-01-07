package com.collektar.features.auth.repository

import com.collektar.shared.database.Tables.PasswordResetTokens
import com.collektar.shared.database.Tables.RefreshTokens
import com.collektar.shared.database.Tables.Users
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class AuthRepositoryTest {
    companion object {
        private lateinit var database: Database
        private lateinit var repository: AuthRepository

        @JvmStatic
        @BeforeAll
        fun setupDatabase() {
            database = Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                driver = "org.h2.Driver"
            )

            transaction {
                SchemaUtils.create(Users, RefreshTokens, PasswordResetTokens)
            }

            repository = AuthRepository(database)
        }

        @JvmStatic
        @AfterAll
        fun teardownDatabase() {
            transaction(database) {
                SchemaUtils.drop(PasswordResetTokens, RefreshTokens, Users)
            }
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        transaction(database) {
            SchemaUtils.drop(PasswordResetTokens, RefreshTokens, Users)
            SchemaUtils.create(Users, RefreshTokens, PasswordResetTokens)
        }
    }


    @Test
    fun shouldCreateUserAndReturnAuthModel() = runTest {
        val userId = UUID.randomUUID()
        val username = "testuser"
        val email = "test@example.com"
        val displayName = "Test User"
        val passwordHash = "hashed_password"

        val result = repository.createUser(
            userId = userId,
            username = username,
            email = email,
            displayName = displayName,
            passwordHash = passwordHash
        )

        assertEquals(userId, result.id)
        assertEquals(username, result.username)
        assertEquals(email, result.email)
        assertEquals(displayName, result.displayName)
        assertEquals(passwordHash, result.passwordHash)
    }

    @Test
    fun shouldFindUserIfUserWithUsernameExists() = runTest {
        val userId = UUID.randomUUID()

        val username = "findme"
        repository.createUser(
            userId = userId,
            username = username,
            email = "find@test.com",
            displayName = "Find Me",
            passwordHash = "hash"
        )

        val result = repository.findByUsername(username)

        assertNotNull(result)
        assertEquals(username, result!!.username)
        assertEquals(userId, result.id)
    }

    @Test
    fun shouldReturnNullWhenUserWithUsernameDoesntExist() = runTest {
        val result = repository.findByUsername("nonexistent")

        assertNull(result)
    }

    @Test
    fun shouldFindUserByEmailIfExists() = runTest {
        val userId = UUID.randomUUID()

        val email = "findme@email.com"
        repository.createUser(
            userId = userId,
            username = "username",
            email = email,
            displayName = "Find Me",
            passwordHash = "hash"
        )

        val result = repository.findByEmail(email)

        assertNotNull(result)
        assertEquals(email, result!!.email)
        assertEquals(userId, result.id)
    }

    @Test
    fun shouldReturnNullWhenUserWithEmailDoesntExist() = runTest {
        val result = repository.findByEmail("nonexistent@email.cm")

        assertNull(result)
    }

    @Test
    fun shouldFindUserByUserIdIfExists() = runTest {
        val userId = UUID.randomUUID()

        repository.createUser(
            userId = userId,
            username = "username",
            email = "email@email.com",
            displayName = "Find Me",
            passwordHash = "hash"
        )

        val result = repository.findByUserId(userId)

        assertNotNull(result)
        assertEquals(userId, result!!.id)
    }

    @Test
    fun shouldReturnNullWhenUserWithIdDoesntExist() = runTest {
        val result = repository.findByUserId(UUID.randomUUID())

        assertNull(result)
    }

    @Test
    fun shouldReturnTrueIfUserNameExists() = runTest {
        val username = "existing"
        repository.createUser(
            userId = UUID.randomUUID(),
            username = username,
            email = "existing@test.com",
            displayName = "Existing",
            passwordHash = "hash"
        )

        val exists = repository.usernameExists(username)

        assertTrue(exists)
    }

    @Test
    fun shouldReturnFalseIfUsernameDoesNotExist() = runTest {
        val exists = repository.usernameExists("nonexistent")

        assertEquals(false, exists)
    }

    @Test
    fun shouldReturnTrueIfEmailExists() = runTest {
        val email = "exists@test.com"
        repository.createUser(
            userId = UUID.randomUUID(),
            username = "user",
            email = email,
            displayName = "User",
            passwordHash = "hash"
        )

        val exists = repository.emailExists(email)

        assertTrue(exists)
    }

    @Test
    fun shouldReturnFalseIfEmailDoesNotExist() = runTest {
        val exists = repository.emailExists("notexistend@test.com")

        assertEquals(false, exists)
    }

    @Test
    fun shouldSaveRefreshTokenAndRetrieveIt() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "tokenuser",
            email = "token@test.com",
            displayName = "Token User",
            passwordHash = "hash"
        )

        val tokenHash = "token_hash_123"
        val issuedAt = Instant.now()
        val expiresAt = Instant.now().plusSeconds(86400)

        repository.saveRefreshToken(userId, tokenHash, expiresAt, issuedAt)
        val retrieved = repository.findRefreshToken(tokenHash)

        assertNotNull(retrieved)
        assertEquals(tokenHash, retrieved!!.tokenHash)
        assertEquals(userId, retrieved.userId)
    }

    @Test
    fun shouldReturnNullIfTokenDoesNotExist() = runTest {
        val result = repository.findRefreshToken("nonexistent_token")

        assertNull(result)
    }

    @Test
    fun shouldDeleteToken() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "revokeuser",
            email = "revoke@test.com",
            displayName = "Revoke User",
            passwordHash = "hash"
        )

        val tokenHash = "revoke_token"
        repository.saveRefreshToken(
            userId,
            tokenHash,
            Instant.now().plusSeconds(86400),
            Instant.now()
        )

        repository.revokeRefreshToken(tokenHash)
        val retrieved = repository.findRefreshToken(tokenHash)

        assertNull(retrieved)
    }

    @Test
    fun shouldRevokeAllUserTokens() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "multitoken",
            email = "multi@test.com",
            displayName = "Multi Token",
            passwordHash = "hash"
        )

        val token1 = "token1"
        val token2 = "token2"
        repository.saveRefreshToken(userId, token1, Instant.now().plusSeconds(86400), Instant.now())
        repository.saveRefreshToken(userId, token2, Instant.now().plusSeconds(86400), Instant.now())

        repository.revokeAllUserTokens(userId)

        assertNull(repository.findRefreshToken(token1))
        assertNull(repository.findRefreshToken(token2))
    }

    @Test
    fun shouldUpdateLastUsedTimestamp() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "updateuser",
            email = "update@test.com",
            displayName = "Update User",
            passwordHash = "hash"
        )

        val tokenHash = "update_token"
        repository.saveRefreshToken(
            userId,
            tokenHash,
            Instant.now().plusSeconds(86400),
            Instant.now()
        )

        repository.updateLastUsed(tokenHash)
        val result = repository.findRefreshToken(tokenHash)

        assertNotNull(result)
        assertEquals(tokenHash, result!!.tokenHash)
    }

    @Test
    fun shouldUpdateUserPassword() = runTest {
        val userId = UUID.randomUUID()
        val oldPasswordHash = "old_password_hash"
        val newPasswordHash = "new_password_hash"

        repository.createUser(
            userId = userId,
            username = "passworduser",
            email = "password@test.com",
            displayName = "Password User",
            passwordHash = oldPasswordHash
        )

        repository.updatePassword(userId, newPasswordHash)

        val user = repository.findByUserId(userId)
        assertNotNull(user)
        assertEquals(newPasswordHash, user!!.passwordHash)
    }

    @Test
    fun shouldDeleteUserAndAllRelatedData() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "deleteuser",
            email = "delete@test.com",
            displayName = "Delete User",
            passwordHash = "hash"
        )

        repository.saveRefreshToken(
            userId,
            "refresh_token_hash",
            Instant.now().plusSeconds(86400),
            Instant.now()
        )

        repository.savePasswordResetToken(
            userId,
            "reset_token_hash",
            Instant.now().plusSeconds(3600)
        )

        repository.deleteUser(userId)

        assertNull(repository.findByUserId(userId))
        assertNull(repository.findRefreshToken("refresh_token_hash"))
        assertNull(repository.findPasswordResetToken("reset_token_hash"))
    }

    @Test
    fun shouldSavePasswordResetTokenAndReturnTokenId() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "resetuser",
            email = "reset@test.com",
            displayName = "Reset User",
            passwordHash = "hash"
        )

        val tokenHash = "reset_token_hash_123"
        val expiresAt = Instant.now().plusSeconds(3600)

        val tokenId = repository.savePasswordResetToken(userId, tokenHash, expiresAt)

        assertNotNull(tokenId)

        val retrieved = repository.findPasswordResetToken(tokenHash)
        assertNotNull(retrieved)
        assertEquals(tokenId, retrieved!!.id)
        assertEquals(userId, retrieved.userId)
        assertEquals(tokenHash, retrieved.tokenHash)
        assertNull(retrieved.usedAt)
    }

    @Test
    fun shouldFindPasswordResetTokenIfValidAndUnused() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "findresetuser",
            email = "findreset@test.com",
            displayName = "Find Reset User",
            passwordHash = "hash"
        )

        val tokenHash = "valid_reset_token"
        val expiresAt = Instant.now().plusSeconds(3600)

        repository.savePasswordResetToken(userId, tokenHash, expiresAt)

        val result = repository.findPasswordResetToken(tokenHash)

        assertNotNull(result)
        assertEquals(tokenHash, result!!.tokenHash)
        assertEquals(userId, result.userId)
        assertNull(result.usedAt)
    }

    @Test
    fun shouldReturnNullIfPasswordResetTokenDoesNotExist() = runTest {
        val result = repository.findPasswordResetToken("nonexistent_reset_token")

        assertNull(result)
    }

    @Test
    fun shouldReturnNullIfPasswordResetTokenIsExpired() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "expireduser",
            email = "expired@test.com",
            displayName = "Expired User",
            passwordHash = "hash"
        )

        val tokenHash = "expired_reset_token"
        val expiresAt = Instant.now().minusSeconds(3600)

        repository.savePasswordResetToken(userId, tokenHash, expiresAt)

        val result = repository.findPasswordResetToken(tokenHash)

        assertNull(result)
    }

    @Test
    fun shouldReturnNullIfPasswordResetTokenIsAlreadyUsed() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "useduser",
            email = "used@test.com",
            displayName = "Used User",
            passwordHash = "hash"
        )

        val tokenHash = "used_reset_token"
        val expiresAt = Instant.now().plusSeconds(3600)

        val tokenId = repository.savePasswordResetToken(userId, tokenHash, expiresAt)
        repository.markPasswordResetTokenAsUsed(tokenId)

        val result = repository.findPasswordResetToken(tokenHash)

        assertNull(result)
    }

    @Test
    fun shouldMarkPasswordResetTokenAsUsed() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "markuser",
            email = "mark@test.com",
            displayName = "Mark User",
            passwordHash = "hash"
        )

        val tokenHash = "mark_used_token"
        val expiresAt = Instant.now().plusSeconds(3600)

        val tokenId = repository.savePasswordResetToken(userId, tokenHash, expiresAt)

        val beforeMark = repository.findPasswordResetToken(tokenHash)
        assertNotNull(beforeMark)
        assertNull(beforeMark!!.usedAt)

        repository.markPasswordResetTokenAsUsed(tokenId)

        val afterMark = repository.findPasswordResetToken(tokenHash)
        assertNull(afterMark)
    }

    @Test
    fun shouldDeleteAllPasswordResetTokensForUser() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "multiresetuser",
            email = "multireset@test.com",
            displayName = "Multi Reset User",
            passwordHash = "hash"
        )

        val token1Hash = "reset_token_1"
        val token2Hash = "reset_token_2"
        val expiresAt = Instant.now().plusSeconds(3600)

        repository.savePasswordResetToken(userId, token1Hash, expiresAt)
        repository.savePasswordResetToken(userId, token2Hash, expiresAt)

        assertNotNull(repository.findPasswordResetToken(token1Hash))
        assertNotNull(repository.findPasswordResetToken(token2Hash))

        repository.deletePasswordResetTokensOfUser(userId)

        assertNull(repository.findPasswordResetToken(token1Hash))
        assertNull(repository.findPasswordResetToken(token2Hash))
    }

    @Test
    fun shouldNotDeletePasswordResetTokensOfOtherUsers() = runTest {
        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()

        repository.createUser(
            userId = userId1,
            username = "user1",
            email = "user1@test.com",
            displayName = "User 1",
            passwordHash = "hash"
        )

        repository.createUser(
            userId = userId2,
            username = "user2",
            email = "user2@test.com",
            displayName = "User 2",
            passwordHash = "hash"
        )

        val token1Hash = "user1_reset_token"
        val token2Hash = "user2_reset_token"
        val expiresAt = Instant.now().plusSeconds(3600)

        repository.savePasswordResetToken(userId1, token1Hash, expiresAt)
        repository.savePasswordResetToken(userId2, token2Hash, expiresAt)

        repository.deletePasswordResetTokensOfUser(userId1)

        assertNull(repository.findPasswordResetToken(token1Hash))

        assertNotNull(repository.findPasswordResetToken(token2Hash))
    }

    @Test
    fun shouldHandleMultipleRefreshTokensWhenDeletingUser() = runTest {
        val userId = UUID.randomUUID()
        repository.createUser(
            userId = userId,
            username = "multitokenuser",
            email = "multitoken@test.com",
            displayName = "Multi Token User",
            passwordHash = "hash"
        )

        repository.saveRefreshToken(userId, "token1", Instant.now().plusSeconds(86400), Instant.now())
        repository.saveRefreshToken(userId, "token2", Instant.now().plusSeconds(86400), Instant.now())
        repository.saveRefreshToken(userId, "token3", Instant.now().plusSeconds(86400), Instant.now())

        repository.deleteUser(userId)

        assertNull(repository.findRefreshToken("token1"))
        assertNull(repository.findRefreshToken("token2"))
        assertNull(repository.findRefreshToken("token3"))
    }
}