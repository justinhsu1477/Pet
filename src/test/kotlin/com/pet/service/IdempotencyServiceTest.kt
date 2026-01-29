package com.pet.service

import com.pet.domain.IdempotencyKey
import com.pet.repository.IdempotencyKeyRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*

class IdempotencyServiceTest {

    private lateinit var idempotencyService: IdempotencyService

    @Mock
    private lateinit var idempotencyKeyRepository: IdempotencyKeyRepository

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
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
    }

    @Test
    fun `should save new key`() {
        idempotencyService.save("new-key", """{"success":true}""", 201)

        verify(idempotencyKeyRepository).save(argThat<IdempotencyKey> { key ->
            key.key == "new-key" && key.httpStatus == 201
        })
    }

    @Test
    fun `should cleanup expired keys`() {
        `when`(idempotencyKeyRepository.deleteByExpiresAtBefore(any())).thenReturn(5)

        idempotencyService.cleanupExpired()

        verify(idempotencyKeyRepository).deleteByExpiresAtBefore(any())
    }
}
