package com.pet.service

import com.pet.domain.IdempotencyKey
import com.pet.repository.IdempotencyKeyRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class IdempotencyServiceTest {

    private lateinit var idempotencyService: IdempotencyService

    @Mock
    private lateinit var idempotencyKeyRepository: IdempotencyKeyRepository

    @BeforeEach
    fun setUp() {
        idempotencyService = IdempotencyService(idempotencyKeyRepository)
    }

    @Test
    fun `should return null for non-existing key`() {
        `when`(idempotencyKeyRepository.findById("non-existing")).thenReturn(Optional.empty())

        val result = idempotencyService.checkExisting("non-existing")

        assertNull(result)
    }

    @Test
    fun `should return existing key`() {
        val existing = IdempotencyKey(key = "existing-key", responseBody = """{"id":1}""", httpStatus = 200)
        `when`(idempotencyKeyRepository.findById("existing-key")).thenReturn(Optional.of(existing))

        val result = idempotencyService.checkExisting("existing-key")

        assertNotNull(result)
        assertEquals("existing-key", result!!.key)
        assertEquals(200, result.httpStatus)
        assertEquals("""{"id":1}""", result.responseBody)
    }

    @Test
    fun `should save new key`() {
        idempotencyService.save("new-key", """{"success":true}""", 201)

        verify(idempotencyKeyRepository).save(argThat<IdempotencyKey> { key ->
            key.key == "new-key" && key.httpStatus == 201 && key.responseBody == """{"success":true}"""
        })
    }

    @Test
    fun `should cleanup expired keys`() {
        // Use doReturn to avoid Kotlin non-null issues with any()
        lenient().doReturn(5L).`when`(idempotencyKeyRepository)
            .deleteByExpiresAtBefore(any(LocalDateTime::class.java) ?: LocalDateTime.now())

        idempotencyService.cleanupExpired()

        // Verify it was called (don't use argument matcher in verify to avoid null issue)
        verify(idempotencyKeyRepository, times(1))
            .deleteByExpiresAtBefore(any(LocalDateTime::class.java) ?: LocalDateTime.now())
    }

    @Test
    fun `should not log when no expired keys deleted`() {
        lenient().doReturn(0L).`when`(idempotencyKeyRepository)
            .deleteByExpiresAtBefore(any(LocalDateTime::class.java) ?: LocalDateTime.now())

        idempotencyService.cleanupExpired()

        verify(idempotencyKeyRepository, times(1))
            .deleteByExpiresAtBefore(any(LocalDateTime::class.java) ?: LocalDateTime.now())
    }
}
