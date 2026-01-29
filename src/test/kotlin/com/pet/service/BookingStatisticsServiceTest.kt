package com.pet.service

import com.pet.domain.Booking
import com.pet.dto.response.BookingStatisticsResponse
import com.pet.repository.BookingRepository
import com.pet.repository.SitterRatingRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.LocalDateTime
import java.util.*

class BookingStatisticsServiceTest {

    private lateinit var bookingStatisticsService: BookingStatisticsService

    @Mock
    private lateinit var bookingRepository: BookingRepository

    @Mock
    private lateinit var sitterRatingRepository: SitterRatingRepository

    private val sitterId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        bookingStatisticsService = BookingStatisticsService(bookingRepository, sitterRatingRepository)
    }

    @Test
    fun `getBookingStats should calculate monthly stats correctly`() {
        // Arrange
        val now = LocalDateTime.now()
        val booking1 = Booking().apply { status = Booking.BookingStatus.COMPLETED }
        val booking2 = Booking().apply { status = Booking.BookingStatus.PENDING }
        val booking3 = Booking().apply { status = Booking.BookingStatus.CANCELLED }
        
        `when`(bookingRepository.findBySitterIdAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(listOf(booking1, booking2, booking3))

        // Act
        val stats = bookingStatisticsService.getBookingStats(sitterId)

        // Assert
        assertEquals(3, stats.currentMonth.total)
        assertEquals(1, stats.currentMonth.completed)
        assertEquals(1, stats.currentMonth.pending)
        assertEquals(1, stats.currentMonth.rejectedOrCancelled)
    }

    @Test
    fun `getRevenueStats should calculate revenue and trend correctly`() {
        // Arrange
        `when`(bookingRepository.sumRevenueByCompletedBookings(any(), any(), any()))
            .thenReturn(1000.0)

        val now = LocalDateTime.now()
        val booking1 = Booking().apply { 
            status = Booking.BookingStatus.COMPLETED
            totalPrice = 500.0
            createdAt = now
        }
        
        `when`(bookingRepository.findBySitterIdAndCreatedAtBetween(any(), any(), any()))
            .thenReturn(listOf(booking1))

        // Act
        val stats = bookingStatisticsService.getRevenueStats(sitterId)

        // Assert
        assertEquals(1000.0, stats.monthlyRevenue)
        assertTrue(stats.dailyTrend.isNotEmpty())
        val todayTrend = stats.dailyTrend.find { it.date == now.toLocalDate() }
        assertNotNull(todayTrend)
        assertEquals(500.0, todayTrend?.revenue)
        assertEquals(1L, todayTrend?.bookingCount)
    }

    @Test
    fun `getRatingStats should handle empty ratings correctly`() {
        // Arrange
        `when`(sitterRatingRepository.calculateAverageRating(sitterId)).thenReturn(null)
        `when`(sitterRatingRepository.`countBySitter_Id`(sitterId)).thenReturn(0L)
        `when`(sitterRatingRepository.countRatingsByStars(sitterId)).thenReturn(emptyList())
        `when`(sitterRatingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId)).thenReturn(emptyList())

        // Act
        val stats = bookingStatisticsService.getRatingStats(sitterId)

        // Assert
        assertEquals(0.0, stats.averageRating)
        assertEquals(0L, stats.totalRatings)
        assertEquals(5, stats.starDistribution.size)
        assertEquals(0L, stats.starDistribution[5])
        assertTrue(stats.latestRatings.isEmpty())
    }
}
