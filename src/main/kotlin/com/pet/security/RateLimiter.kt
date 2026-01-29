package com.pet.security

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

/**
 * Token Bucket 速率限制器
 * 使用 in-memory ConcurrentHashMap + CAS 實作，lock-free 且 thread-safe
 */
class RateLimiter(
    val maxTokens: Int = 60,
    private val refillRate: Double = 1.0,
    private val cleanupIntervalMs: Long = 600_000
) {

    private data class TokenBucket(
        val tokens: Double,
        val lastRefillTimestamp: Long
    )

    private val buckets = ConcurrentHashMap<String, AtomicReference<TokenBucket>>()

    @Volatile
    private var lastCleanupTimestamp: Long = System.nanoTime()

    /**
     * 嘗試消耗一個 token
     * @return Pair(是否允許, 剩餘 token 數)
     */
    fun tryConsume(key: String): Pair<Boolean, Double> {
        cleanupIfNeeded()

        val bucketRef = buckets.computeIfAbsent(key) {
            AtomicReference(TokenBucket(maxTokens.toDouble(), System.nanoTime()))
        }

        while (true) {
            val current = bucketRef.get()
            val now = System.nanoTime()
            val elapsedSeconds = (now - current.lastRefillTimestamp) / 1_000_000_000.0
            val refilled = (current.tokens + elapsedSeconds * refillRate).coerceAtMost(maxTokens.toDouble())

            if (refilled < 1.0) {
                return Pair(false, 0.0)
            }

            val updated = TokenBucket(refilled - 1.0, now)
            if (bucketRef.compareAndSet(current, updated)) {
                return Pair(true, updated.tokens)
            }
        }
    }

    private fun cleanupIfNeeded() {
        val now = System.nanoTime()
        if ((now - lastCleanupTimestamp) / 1_000_000 < cleanupIntervalMs) return
        lastCleanupTimestamp = now

        val staleThresholdNs = cleanupIntervalMs * 1_000_000
        buckets.entries.removeIf { (_, ref) ->
            (now - ref.get().lastRefillTimestamp) > staleThresholdNs
        }
    }
}
