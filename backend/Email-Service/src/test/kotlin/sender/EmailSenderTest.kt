package com.collektar.sender

import com.collektar.models.Email
import com.collektar.providers.IEmailProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmailSenderTest {
    @Test
    fun shouldSendEmailSuccessfully() = runTest {
        val mockProvider = mockk<IEmailProvider>()
        val expectedResult = Result.success(Unit)
        val receiver = "test@example.com"
        val subject = "Test Subject"
        val htmlBody = "<html>Test</html>"
        val email = Email(
            receiver,
            subject,
            htmlBody
        )
        coEvery {
            mockProvider.sendEmail(
                receiver,
                subject,
                htmlBody
            )
        } returns expectedResult

        val sender = EmailSender(mockProvider)

        val result = sender.send(email)
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) {
            mockProvider.sendEmail(
                receiver,
                subject,
                htmlBody
            )
        }
    }

    @Test
    fun shouldReturnFailureWhenProviderFails() = runTest {
        val mockProvider = mockk<IEmailProvider>()
        val expectedExceptions = Exception("Some provider exception")
        val expectedResult = Result.failure<Unit>(expectedExceptions)

        coEvery {
            mockProvider.sendEmail(any(), any(), any())
        } returns expectedResult

        val sender = EmailSender(mockProvider)
        val email = Email(
            to = "test@example.com",
            subject = "Test Subject",
            htmlBody = "<html>Test</html>"
        )

        val result = sender.send(email)

        assertTrue(result.isFailure)
        assertEquals(expectedExceptions, result.exceptionOrNull())
    }
}