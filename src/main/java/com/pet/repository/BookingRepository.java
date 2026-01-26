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
     * 查詢使用者的所有預約（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    /**
     * 查詢保母的所有預約（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.sitter.id = :sitterId " +
           "ORDER BY b.startTime DESC")
    List<Booking> findBySitterIdOrderByStartTimeDesc(@Param("sitterId") UUID sitterId);

    /**
     * 查詢保母待處理的預約（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.sitter.id = :sitterId " +
           "AND b.status = :status")
    List<Booking> findBySitterIdAndStatus(@Param("sitterId") UUID sitterId, @Param("status") BookingStatus status);

    /**
     * 查詢寵物的預約歷史（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.pet.id = :petId " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByPetIdOrderByCreatedAtDesc(@Param("petId") UUID petId);

    /**
     * 檢查時段是否有衝突（防止雙重預約）
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
     * 查詢指定狀態的預約（使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.status = :status " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findByStatusOrderByCreatedAtDesc(@Param("status") BookingStatus status);

    /**
     * 查詢即將開始的預約（用於發送提醒，使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "WHERE b.status = 'CONFIRMED' " +
           "AND b.startTime BETWEEN :now AND :endTime")
    List<Booking> findUpcomingBookings(
            @Param("now") LocalDateTime now,
            @Param("endTime") LocalDateTime endTime);

    // ============================================
    // 統計相關查詢方法
    // ============================================

    /**
     * 查詢保母在指定時間範圍內的所有預約（用於統計）
     */
    @Query("SELECT b FROM Booking b WHERE b.sitter.id = :sitterId AND b.createdAt BETWEEN :startTime AND :endTime")
    List<Booking> findBySitterIdAndCreatedAtBetween(
            @Param("sitterId") UUID sitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計保母在指定時間範圍內特定狀態的預約數量
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.sitter.id = :sitterId AND b.status = :status AND b.createdAt BETWEEN :startTime AND :endTime")
    long countBySitterIdAndStatusAndCreatedAtBetween(
            @Param("sitterId") UUID sitterId,
            @Param("status") BookingStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 統計保母在指定時間範圍內多個狀態的預約數量（例如 REJECTED + CANCELLED）
     */
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.sitter.id = :sitterId AND b.status IN :statuses AND b.createdAt BETWEEN :startTime AND :endTime")
    long countBySitterIdAndStatusInAndCreatedAtBetween(
            @Param("sitterId") UUID sitterId,
            @Param("statuses") List<BookingStatus> statuses,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 計算保母在指定時間範圍內已完成訂單的總收入
     * 使用 COALESCE 避免 null 值
     */
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM Booking b WHERE b.sitter.id = :sitterId AND b.status = 'COMPLETED' AND b.createdAt BETWEEN :startTime AND :endTime")
    Double sumRevenueByCompletedBookings(
            @Param("sitterId") UUID sitterId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢保母在某一天的所有已完成預約（用於每日收入趨勢）
     */
    @Query("SELECT b FROM Booking b WHERE b.sitter.id = :sitterId AND b.status = 'COMPLETED' AND DATE(b.createdAt) = :date")
    List<Booking> findCompletedBookingsByDate(
            @Param("sitterId") UUID sitterId,
            @Param("date") java.time.LocalDate date);

    /**
     * 取得所有預約（管理員用，使用 JOIN FETCH 預加載關聯實體）
     */
    @Query("SELECT b FROM Booking b " +
           "JOIN FETCH b.user " +
           "JOIN FETCH b.pet " +
           "JOIN FETCH b.sitter " +
           "ORDER BY b.createdAt DESC")
    List<Booking> findAllWithRelations();
}
