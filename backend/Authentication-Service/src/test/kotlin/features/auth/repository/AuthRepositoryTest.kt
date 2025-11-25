package com.collektar.features.auth.repository

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
                SchemaUtils.create(Users, RefreshTokens)
            }

            repository = AuthRepository(database)
        }

        @JvmStatic
        @AfterAll
        fun teardownDatabase() {
            transaction(database) {
                SchemaUtils.drop(RefreshTokens, Users)
            }
        }
    }

    @BeforeEach
    fun cleanDatabase() {
        transaction(database) {
            SchemaUtils.drop(RefreshTokens, Users)
            SchemaUtils.create(Users, RefreshTokens)
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
}