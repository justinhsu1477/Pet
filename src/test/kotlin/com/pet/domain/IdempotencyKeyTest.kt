package com.pet.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class IdempotencyKeyTest {

    @Test
    fun `should create IdempotencyKey with all fields`() {
        val key = IdempotencyKey(
            key = "test-key-123",
            responseBody = """{"result":"ok"}""",
            httpStatus = 201
        )

        assertEquals("test-key-123", key.key)
        assertEquals("""{"result":"ok"}""", key.responseBody)
        assertEquals(201, key.httpStatus)
        assertNotNull(key.createdAt)
        assertNotNull(key.expiresAt)
    }

    @Test
    fun `should have expiresAt 24 hours after createdAt`() {
        val before = LocalDateTime.now()
        val key = IdempotencyKey(key = "test-key", httpStatus = 200)
        val after = LocalDateTime.now()

        // expiresAt should be roughly 24 hours after creation
        assertTrue(key.expiresAt.isAfter(before.plusHours(23).plusMinutes(59)))
        assertTrue(key.expiresAt.isBefore(after.plusHours(24).plusMinutes(1)))
    }

    @Test
    fun `should default httpStatus to 200`() {
        val key = IdempotencyKey(key = "default-status")

        assertEquals(200, key.httpStatus)
    }

    @Test
    fun `should allow null responseBody`() {
        val key = IdempotencyKey(key = "null-body")

        assertNull(key.responseBody)
    }

    @Test
    fun `should allow mutable responseBody and httpStatus`() {
        val key = IdempotencyKey(key = "mutable-test", httpStatus = 200)

        key.responseBody = """{"updated":true}"""
        key.httpStatus = 500

        assertEquals("""{"updated":true}""", key.responseBody)
        assertEquals(500, key.httpStatus)
    }
}
