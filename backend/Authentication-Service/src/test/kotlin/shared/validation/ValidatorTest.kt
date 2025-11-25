package com.collektar.shared.validation

import com.collektar.shared.errors.AppError
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValidatorTest {
    @Test
    fun shouldAcceptValidUsername() {
        val validUsernames = listOf(
            "user123",
            "test_user",
            "my-username",
            "abc",
            "a".repeat(50),
            "User_Name-123",
            "USERNAME",
            "lowercase"
        )

        validUsernames.forEach { username ->
            assertDoesNotThrow {
                Validator.validateUsername(username)
            }
        }
    }

    @Test
    fun shouldRejectUsernameToShort() {
        val tooShort = "ab"

        assertThrows<AppError.BadRequest.InvalidUsername> {
            Validator.validateUsername(tooShort)
        }
    }

    @Test
    fun shouldRejectUsernameToLong() {
        val tooLong = "a".repeat(51)

        assertThrows<AppError.BadRequest.InvalidUsername> {
            Validator.validateUsername(tooLong)
        }
    }

    @Test
    fun shouldRejectUsernameWithSpaces() {
        val usernameWithSpaces = "user name"

        assertThrows<AppError.BadRequest.InvalidUsername> {
            Validator.validateUsername(usernameWithSpaces)
        }
    }

    @Test
    fun shouldRejectUsernameSpecialCharacters() {
        val specialCharUsernames = listOf(
            "user@name",
            "user#name",
            "user.name",
            "user!name",
            "user\$name",
            "user%name"
        )

        specialCharUsernames.forEach { username ->
            assertThrows<AppError.BadRequest.InvalidUsername> {
                Validator.validateUsername(username)
            }
        }
    }

    @Test
    fun shouldRejectEmptyUsername() {
        val emptyUsername = ""

        assertThrows<AppError.BadRequest.InvalidUsername> {
            Validator.validateUsername(emptyUsername)
        }
    }

    @Test
    fun shouldAcceptValidEmailAddresses() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "first_last@subdomain.example.com",
            "user123@test-domain.com",
            "a@b.co",
            "Test@Example.COM"
        )

        validEmails.forEach { email ->
            assertDoesNotThrow {
                Validator.validateEmail(email)
            }
        }
    }

    @Test
    fun shouldRejectEmailMissingAtSymbol() {
        val invalidEmail = "testexample.com"

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(invalidEmail)
        }
    }

    @Test
    fun shouldRejectEmailMissingDomain() {
        val invalidEmail = "test@"

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(invalidEmail)
        }
    }

    @Test
    fun shouldRejectEmailMissingLocal() {
        val invalidEmail = "@example.com"

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(invalidEmail)
        }
    }

    @Test
    fun shouldRejectEmailMissingTLD() {
        val invalidEmail = "test@example"

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(invalidEmail)
        }
    }

    @Test
    fun shouldRejectEmailWithSpaces() {
        val invalidEmails = listOf(
            "test @example.com",
            "test@ example.com",
            "test@example .com"
        )

        invalidEmails.forEach { email ->
            assertThrows<AppError.BadRequest.InvalidEmail> {
                Validator.validateEmail(email)
            }
        }
    }

    @Test
    fun shouldRejectEmailWithInvalidCharacters() {
        val invalidEmails = listOf(
            "test#user@example.com",
            "test(user)@example.com",
            "test@exam ple.com"
        )

        invalidEmails.forEach { email ->
            assertThrows<AppError.BadRequest.InvalidEmail> {
                Validator.validateEmail(email)
            }
        }
    }

    @Test
    fun shouldRejectEmptyEMail() {
        val emptyEmail = ""

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(emptyEmail)
        }
    }

    @Test
    fun shouldRejectEmailWithMultipleAtSymbols() {
        val invalidEmail = "test@@example.com"

        assertThrows<AppError.BadRequest.InvalidEmail> {
            Validator.validateEmail(invalidEmail)
        }
    }

    @Test
    fun shouldAcceptValidDisplayName() {
        val validDisplayNames = listOf(
            "John Doe",
            "Alice",
            "Bob Smith Jr.",
            "María García",
            "User 123",
            "a",
            "a".repeat(100)
        )

        validDisplayNames.forEach { displayName ->
            assertDoesNotThrow {
                Validator.validateDisplayName(displayName)
            }
        }
    }

    @Test
    fun shouldRejectBlankDisplayName() {
        val blankDisplayNames = listOf(
            "",
            "   ",
            "\t",
            "\n"
        )

        blankDisplayNames.forEach { displayName ->
            assertThrows<AppError.BadRequest.InvalidDisplayName> {
                Validator.validateDisplayName(displayName)
            }
        }
    }

    @Test
    fun shouldRejectDisplayNameToLong() {
        val tooLong = "a".repeat(101)

        assertThrows<AppError.BadRequest.InvalidDisplayName> {
            Validator.validateDisplayName(tooLong)
        }
    }

    @Test
    fun shouldAcceptValidPasswords() {
        val validPasswords = listOf(
            "Password123!",
            "MyP@ssw0rd",
            "Secure#Pass1",
            "Test1234!@#$",
            "aB3$" + "x".repeat(4),
            "A1b!" + "x".repeat(60)
        )

        validPasswords.forEach { password ->
            assertDoesNotThrow {
                Validator.validatePassword(password)
            }
        }
    }

    @Test
    fun shouldRejectPasswordToShort() {
        val tooShort = "Pass1!"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(tooShort)
        }
        assertTrue(exception.message!!.contains("8–64 characters"))
    }

    @Test
    fun shouldRejectPasswordToLong() {
        val tooLong = "Password1!" + "a".repeat(60)

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(tooLong)
        }
        assertTrue(exception.message!!.contains("8–64 characters"))
    }

    @Test
    fun shouldRejectPasswordWithSpaces() {
        val passwordWithSpaces = "Pass word123!"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(passwordWithSpaces)
        }
        assertTrue(exception.message!!.contains("cannot contain spaces"))
    }

    @Test
    fun shouldRejectPasswordNoUpper() {
        val noUppercase = "password123!"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(noUppercase)
        }
        assertTrue(exception.message!!.contains("uppercase letter"))
    }

    @Test
    fun shouldRejectPasswordNoLower() {
        val noLowercase = "PASSWORD123!"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(noLowercase)
        }
        assertTrue(exception.message!!.contains("lowercase letter"))
    }

    @Test
    fun shouldRejectPasswordNoDigit() {
        val noDigit = "Password!@#"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(noDigit)
        }
        assertTrue(exception.message!!.contains("digit"))
    }

    @Test
    fun shouldRejectPasswordNoSpecialChar() {
        val noSpecialChar = "Password123"

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(noSpecialChar)
        }
        assertTrue(exception.message!!.contains("special character"))
    }

    @Test
    fun shouldAcceptPasswordWithAllowedSpecialChar() {
        val specialChars = "!@#$%^&*()_+-=[]{}|;:'\",.<>?/"
        val passwords = specialChars.map { char ->
            "Password1$char"
        }

        passwords.forEach { password ->
            assertDoesNotThrow {
                Validator.validatePassword(password)
            }
        }
    }

    @Test
    fun shouldRejectEmptyPassword() {
        val emptyPassword = ""

        val exception = assertThrows<AppError.BadRequest.InvalidPassword> {
            Validator.validatePassword(emptyPassword)
        }
        assertTrue(exception.message!!.contains("8–64 characters"))
    }
}