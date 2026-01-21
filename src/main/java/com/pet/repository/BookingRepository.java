package com.pet.repository;

import com.pet.domain.Booking;
import com.pet.domain.Booking.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * 使用悲觀鎖查詢（用於狀態更新時避免併發問題）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.id = :id")
    Optional<Booking> findByIdWithLock(@Param("id") UUID id);

    /**
     * 查詢使用者的所有預約
     */
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * 查詢保母的所有預約
     */
    List<Booking> findBySitterIdOrderByStartTimeDesc(UUID sitterId);

    /**
     * 查詢保母待處理的預約
     */
    List<Booking> findBySitterIdAndStatus(UUID sitterId, BookingStatus status);

    /**
     * 查詢寵物的預約歷史
     */
    List<Booking> findByPetIdOrderByCreatedAtDesc(UUID petId);

    /**
     * 檢查時段是否有衝突（防止雙重預約）
     * 面試重點：時間區間重疊檢查邏輯
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.sitter.id = :sitterId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    boolean hasConflictingBooking(
            @Param("sitterId") UUID sitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 排除特定預約的衝突檢查（用於更新時）
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.sitter.id = :sitterId " +
           "AND b.id != :excludeId " +
           "AND b.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((b.startTime < :endTime AND b.endTime > :startTime))")
    boolean hasConflictingBookingExcluding(
            @Param("sitterId") UUID sitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") UUID excludeId);

    /**
     * 統計保母完成的訂單數
     */
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.sitter.id = :sitterId " +
           "AND b.status = 'COMPLETED'")
    long countCompletedBookings(@Param("sitterId") UUID sitterId);

    /**
     * 查詢指定狀態的預約
     */
    List<Booking> findByStatusOrderByCreatedAtDesc(BookingStatus status);

    /**
     * 查詢即將開始的預約（用於發送提醒）
     */
    @Query("SELECT b FROM Booking b " +
           "WHERE b.status = 'CONFIRMED' " +
           "AND b.startTime BETWEEN :now AND :endTime")
    List<Booking> findUpcomingBookings(
            @Param("now") LocalDateTime now,
            @Param("endTime") LocalDateTime endTime);
}
