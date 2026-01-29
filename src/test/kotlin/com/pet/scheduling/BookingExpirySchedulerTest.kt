package com.pet.scheduling

import com.pet.domain.Booking
import com.pet.domain.Booking.BookingStatus
import com.pet.domain.Dog
import com.pet.domain.Sitter
import com.pet.domain.Users
import com.pet.repository.BookingRepository
import com.pet.service.LineMessagingService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(MockitoExtension::class)
class BookingExpirySchedulerTest {

    private lateinit var scheduler: BookingExpiryScheduler

    @Mock
    private lateinit var bookingRepository: BookingRepository

    @Mock
    private lateinit var lineMessagingService: LineMessagingService

    @BeforeEach
    fun setUp() {
        scheduler = BookingExpiryScheduler(bookingRepository, lineMessagingService)
    }

    private fun createTestBooking(): Booking {
        val booking = Booking()
        booking.id = UUID.randomUUID()
        booking.status = BookingStatus.PENDING
        booking.createdAt = LocalDateTime.now().minusHours(25)
        booking.startTime = LocalDateTime.of(2026, 2, 1, 10, 0)
        booking.endTime = LocalDateTime.of(2026, 2, 1, 14, 0)
        booking.totalPrice = 800.0

        val pet = Dog()
        pet.name = "小黑"
        booking.pet = pet

        val sitter = Sitter()
        sitter.name = "王保母"
        booking.sitter = sitter

        val user = Users()
        user.id = UUID.randomUUID()
        booking.user = user

        return booking
    }

    @Test
    fun `should expire pending bookings older than 24 hours`() {
        val booking = createTestBooking()
        doReturn(listOf(booking))
            .`when`(bookingRepository)
            .findByStatusAndCreatedAtBefore(any(BookingStatus::class.java), any(LocalDateTime::class.java))

        scheduler.expireOverdueBookings()

        assertEquals(BookingStatus.EXPIRED, booking.status)
        verify(bookingRepository).save(booking)
    }

    @Test
    fun `should not expire recent pending bookings`() {
        doReturn(emptyList<Booking>())
            .`when`(bookingRepository)
            .findByStatusAndCreatedAtBefore(any(BookingStatus::class.java), any(LocalDateTime::class.java))

        scheduler.expireOverdueBookings()

        verify(bookingRepository, never()).save(any())
    }

    @Test
    fun `should send LINE notification for expired bookings`() {
        val booking = createTestBooking()
        doReturn(listOf(booking))
            .`when`(bookingRepository)
            .findByStatusAndCreatedAtBefore(any(BookingStatus::class.java), any(LocalDateTime::class.java))

        scheduler.expireOverdueBookings()

        verify(lineMessagingService).sendBookingExpiredNotification(booking)
    }

    @Test
    fun `should continue processing other bookings when one fails`() {
        val booking1 = createTestBooking()
        val booking2 = createTestBooking()

        doReturn(listOf(booking1, booking2))
            .`when`(bookingRepository)
            .findByStatusAndCreatedAtBefore(any(BookingStatus::class.java), any(LocalDateTime::class.java))
        `when`(bookingRepository.save(booking1)).thenThrow(RuntimeException("DB error"))

        scheduler.expireOverdueBookings()

        verify(bookingRepository).save(booking2)
    }
}
