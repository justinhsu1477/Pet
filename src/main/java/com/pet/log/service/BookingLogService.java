package com.pet.log.service;

import com.pet.domain.Booking;
import com.pet.log.domain.BookingLog;
import com.pet.log.repository.BookingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Booking Log 同步服務
 * 負責將 Primary DB 的 Booking 同步到 Log DB
 */
@Service
public class BookingLogService {

    private static final Logger logger = LoggerFactory.getLogger(BookingLogService.class);

    private final BookingLogRepository bookingLogRepository;

    public BookingLogService(BookingLogRepository bookingLogRepository) {
        this.bookingLogRepository = bookingLogRepository;
    }

    /**
     * 同步 Booking 到 Log DB
     * 每次同步都會建立新的 Log 記錄（保留歷史）
     */
    @Transactional("logTransactionManager")
    public BookingLog syncBookingToLog(Booking booking) {
        try {
            BookingLog log = convertToBookingLog(booking);
            BookingLog saved = bookingLogRepository.save(log);
            logger.info("Successfully synced booking {} to log DB, log id: {}",
                    booking.getId(), saved.getId());
            return saved;
        } catch (Exception e) {
            logger.error("Failed to sync booking {} to log DB: {}",
                    booking.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 將 Booking Entity 轉換為 BookingLog Entity（扁平化）
     */
    private BookingLog convertToBookingLog(Booking booking) {
        BookingLog log = new BookingLog();

        log.setBookingId(booking.getId());

        // Pet 資訊
        if (booking.getPet() != null) {
            log.setPetId(booking.getPet().getId());
            log.setPetName(booking.getPet().getName());
        }

        // Sitter 資訊
        if (booking.getSitter() != null) {
            log.setSitterId(booking.getSitter().getId());
            log.setSitterName(booking.getSitter().getName());
        }

        // User 資訊
        if (booking.getUser() != null) {
            log.setUserId(booking.getUser().getId());
            log.setUsername(booking.getUser().getUsername());
        }

        // 預約時間
        log.setStartTime(booking.getStartTime());
        log.setEndTime(booking.getEndTime());

        // 狀態
        log.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);

        // 備註
        log.setNotes(booking.getNotes());
        log.setSitterResponse(booking.getSitterResponse());

        // 價格（轉換為 BigDecimal）
        if (booking.getTotalPrice() != null) {
            log.setTotalPrice(BigDecimal.valueOf(booking.getTotalPrice()));
        }

        // 時間戳
        log.setBookingCreatedAt(booking.getCreatedAt());
        log.setBookingUpdatedAt(booking.getUpdatedAt());
        log.setSyncTime(LocalDateTime.now());

        return log;
    }
}
