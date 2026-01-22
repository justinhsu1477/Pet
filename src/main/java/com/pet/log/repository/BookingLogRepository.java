package com.pet.log.repository;

import com.pet.log.domain.BookingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Booking Log Repository
 * 提供基本 CRUD 操作和報表/分析用查詢方法
 */
@Repository
public interface BookingLogRepository extends JpaRepository<BookingLog, UUID> {

    /**
     * 根據原始 Booking ID 查詢最新的 Log 記錄
     */
    Optional<BookingLog> findTopByBookingIdOrderBySyncTimeDesc(UUID bookingId);

    /**
     * 根據原始 Booking ID 查詢所有 Log 記錄（含歷史）
     */
    List<BookingLog> findByBookingIdOrderBySyncTimeDesc(UUID bookingId);

    /**
     * 根據 Sitter ID 查詢所有 Log 記錄
     */
    List<BookingLog> findBySitterIdOrderBySyncTimeDesc(UUID sitterId);

    /**
     * 根據 User ID 查詢所有 Log 記錄
     */
    List<BookingLog> findByUserIdOrderBySyncTimeDesc(UUID userId);

    /**
     * 根據狀態查詢 Log 記錄
     */
    List<BookingLog> findByStatusOrderBySyncTimeDesc(String status);

    /**
     * 查詢指定時間範圍內的 Log 記錄
     */
    List<BookingLog> findBySyncTimeBetweenOrderBySyncTimeDesc(LocalDateTime start, LocalDateTime end);

    /**
     * 統計指定時間範圍內各狀態的預約數量
     */
    @Query("SELECT bl.status, COUNT(bl) FROM BookingLog bl " +
           "WHERE bl.syncTime BETWEEN :start AND :end " +
           "GROUP BY bl.status")
    List<Object[]> countByStatusInTimeRange(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    /**
     * 統計指定 Sitter 的預約數量（按狀態分組）
     */
    @Query("SELECT bl.status, COUNT(bl) FROM BookingLog bl " +
           "WHERE bl.sitterId = :sitterId " +
           "GROUP BY bl.status")
    List<Object[]> countBySitterIdGroupByStatus(@Param("sitterId") UUID sitterId);

    /**
     * 查詢指定 Sitter 在時間範圍內的營收
     */
    @Query("SELECT COALESCE(SUM(bl.totalPrice), 0) FROM BookingLog bl " +
           "WHERE bl.sitterId = :sitterId " +
           "AND bl.status = 'COMPLETED' " +
           "AND bl.bookingCreatedAt BETWEEN :start AND :end")
    Double calculateSitterRevenueInTimeRange(@Param("sitterId") UUID sitterId,
                                             @Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
