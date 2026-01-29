package com.pet.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RateLimiterTest {

    private lateinit var rateLimiter: RateLimiter

    @BeforeEach
    fun setUp() {
        rateLimiter = RateLimiter(maxTokens = 5, refillRate = 10.0)
    }

    @Test
    fun `should allow requests within limit`() {
        repeat(5) { i ->
            val (allowed, _) = rateLimiter.tryConsume("test-key")
            assertTrue(allowed, "Request $i should be allowed")
        }
    }

    @Test
    fun `should reject requests exceeding limit`() {
        repeat(5) {
            rateLimiter.tryConsume("test-key")
        }
        val (allowed, _) = rateLimiter.tryConsume("test-key")
        assertFalse(allowed, "6th request should be rejected")
    }

    @Test
    fun `should refill tokens over time`() {
        repeat(5) {
            rateLimiter.tryConsume("test-key")
        }
        // With refillRate=10.0 tokens/sec, sleeping 200ms should refill ~2 tokens
        Thread.sleep(200)
        val (allowed, _) = rateLimiter.tryConsume("test-key")
        assertTrue(allowed, "Should allow after refill")
    }

    @Test
    fun `should track different keys independently`() {
        repeat(5) {
            rateLimiter.tryConsume("key1")
        }
        val (key1Allowed, _) = rateLimiter.tryConsume("key1")
        assertFalse(key1Allowed, "key1 should be exhausted")

        val (key2Allowed, _) = rateLimiter.tryConsume("key2")
        assertTrue(key2Allowed, "key2 should still work")
    }

    @Test
    fun `should return remaining tokens count`() {
        val (_, remaining) = rateLimiter.tryConsume("test-key")
        // After consuming 1 from 5, remaining should be 4
        assertEquals(4.0, remaining, 1.0)
    }

    @Test
    fun `should cleanup old entries after cleanup interval`() {
        // Use a very short cleanup interval
        val shortCleanupLimiter = RateLimiter(maxTokens = 5, refillRate = 10.0, cleanupIntervalMs = 1)
        shortCleanupLimiter.tryConsume("old-key")

        // Sleep to exceed cleanup interval
        Thread.sleep(50)

        // This call should trigger cleanup, and old-key should be removed
        // Verify by consuming again - should get full tokens if bucket was cleaned
        shortCleanupLimiter.tryConsume("trigger-cleanup")

        // old-key should have been cleaned up, so a new consume gets fresh bucket
        val (allowed, remaining) = shortCleanupLimiter.tryConsume("old-key")
        assertTrue(allowed)
        assertEquals(4.0, remaining, 1.0)
    }
}
