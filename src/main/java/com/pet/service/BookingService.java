package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.BookingDto;
import com.pet.dto.BookingStatusUpdateDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.exception.ResourceNotFoundException;
import com.pet.log.service.BookingLogService;
import com.pet.repository.BookingRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.SitterRepository;
import com.pet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 預約服務
 * 1. 樂觀鎖處理併發更新 (@Version + ObjectOptimisticLockingFailureException)
 * 2. 狀態機設計：限制合法的狀態轉換
 * 3. 時間衝突檢查：防止雙重預約
 * 4. 事務管理：確保資料一致性
 */
@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final PetRepository petRepository;
    private final SitterRepository sitterRepository;
    private final UserRepository userRepository;
    private final BookingLogService bookingLogService;

    public BookingService(BookingRepository bookingRepository,
                          PetRepository petRepository,
                          SitterRepository sitterRepository,
                          UserRepository userRepository,
                          BookingLogService bookingLogService) {
        this.bookingRepository = bookingRepository;
        this.petRepository = petRepository;
        this.sitterRepository = sitterRepository;
        this.userRepository = userRepository;
        this.bookingLogService = bookingLogService;
    }

    /**
     * 建立預約
     * 檢查時間衝突，避免雙重預約
     */
    public BookingDto createBooking(BookingDto dto, UUID userId) {
        // 1. 驗證時間
        validateBookingTime(dto.startTime(), dto.endTime());

        // 2. 檢查時段是否已被預約（防止雙重預約）
        if (bookingRepository.hasConflictingBooking(dto.sitterId(), dto.startTime(), dto.endTime())) {
            throw new BusinessException(ErrorCode.BOOKING_CONFLICT);
        }

        // 3. 取得關聯實體
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", dto.petId()));
        Sitter sitter = sitterRepository.findById(dto.sitterId())
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", dto.sitterId()));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", "id", userId));

        // 4. 建立預約
        Booking booking = new Booking();
        booking.setPet(pet);
        booking.setSitter(sitter);
        booking.setUser(user);
        booking.setStartTime(dto.startTime());
        booking.setEndTime(dto.endTime());
        booking.setNotes(dto.notes());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(dto.totalPrice());

        Booking saved = bookingRepository.save(booking);

        // 同步到 Log DB
        syncToLogDb(saved);

        // TODO: 發送通知給保母（Domain Event）
        // eventPublisher.publishEvent(new BookingCreatedEvent(saved));

        return convertToDto(saved);
    }

    /**
     * 更新預約狀態
     */
    public BookingDto updateBookingStatus(UUID bookingId, BookingStatusUpdateDto updateDto) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("預約", "id", bookingId));

            // 驗證狀態轉換是否合法
            if (!booking.canTransitionTo(updateDto.targetStatus())) {
                throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS_TRANSITION,
                        String.format("無法從 %s 轉換到 %s", booking.getStatus(), updateDto.targetStatus()));
            }

            // 如果是確認預約，再次檢查是否有衝突（防止併發確認）
            if (updateDto.targetStatus() == BookingStatus.CONFIRMED) {
                if (bookingRepository.hasConflictingBookingExcluding(
                        booking.getSitter().getId(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        bookingId)) {
                    throw new BusinessException(ErrorCode.BOOKING_CONFLICT);
                }
            }

            // 更新狀態
            booking.setStatus(updateDto.targetStatus());
            if (updateDto.reason() != null) {
                booking.setSitterResponse(updateDto.reason());
            }

            // 如果完成訂單，更新保母統計
            if (updateDto.targetStatus() == BookingStatus.COMPLETED) {
                Sitter sitter = booking.getSitter();
                sitter.setCompletedBookings(sitter.getCompletedBookings() + 1);
                sitterRepository.save(sitter);
            }

            Booking updated = bookingRepository.save(booking);

            // 同步到 Log DB
            syncToLogDb(updated);

            // TODO: 發送狀態變更通知
            // eventPublisher.publishEvent(new BookingStatusChangedEvent(updated));

            return convertToDto(updated);

        } catch (ObjectOptimisticLockingFailureException e) {
            // 樂觀鎖衝突：其他人已經更新過這筆預約
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_PROCESSED,
                    "預約狀態已被其他操作更新，請重新整理後再試");
        }
    }

    /**
     * 取得預約詳情
     */
    @Transactional(readOnly = true)
    public BookingDto getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("預約", "id", id));
        return convertToDto(booking);
    }

    /**
     * 取得所有預約（管理員用，使用 FETCH JOIN 避免 N+1 問題）
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllWithRelations()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得使用者的所有預約
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByUser(UUID userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得保母的所有預約
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsBySitter(UUID sitterId) {
        return bookingRepository.findBySitterIdOrderByStartTimeDesc(sitterId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得保母待處理的預約
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getPendingBookingsForSitter(UUID sitterId) {
        return bookingRepository.findBySitterIdAndStatus(sitterId, BookingStatus.PENDING)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得寵物的預約歷史
     */
    @Transactional(readOnly = true)
    public List<BookingDto> getBookingsByPet(UUID petId) {
        return bookingRepository.findByPetIdOrderByCreatedAtDesc(petId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取消預約（飼主或保母都可以）
     */
    public BookingDto cancelBooking(UUID bookingId, String reason) {
        BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(BookingStatus.CANCELLED, reason);
        return updateBookingStatus(bookingId, updateDto);
    }

    /**
     * 保母接受預約
     */
    public BookingDto confirmBooking(UUID bookingId, String response) {
        BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(BookingStatus.CONFIRMED, response);
        return updateBookingStatus(bookingId, updateDto);
    }

    /**
     * 保母拒絕預約
     */
    public BookingDto rejectBooking(UUID bookingId, String reason) {
        BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(BookingStatus.REJECTED, reason);
        return updateBookingStatus(bookingId, updateDto);
    }

    /**
     * 完成預約
     */
    public BookingDto completeBooking(UUID bookingId) {
        BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(BookingStatus.COMPLETED, null);
        return updateBookingStatus(bookingId, updateDto);
    }

    // ============ Private Methods ============

    /**
     * 同步 Booking 到 Log DB
     * 失敗時只記錄錯誤，不影響主流程
     */
    private void syncToLogDb(Booking booking) {
        try {
            bookingLogService.syncBookingToLog(booking);
        } catch (Exception e) {
            logger.error("Failed to sync booking {} to log DB, will continue with main operation: {}",
                    booking.getId(), e.getMessage());
        }
    }

    private void validateBookingTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_TIME, "開始和結束時間都必須填寫");
        }
        if (startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_TIME, "開始時間不能晚於結束時間");
        }
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_TIME, "開始時間不能是過去時間");
        }
    }

    private BookingDto convertToDto(Booking booking) {
        // 取得 user 資訊（使用 null 安全處理）
        UUID userId = booking.getUser() != null ? booking.getUser().getId() : null;
        String username = booking.getUser() != null ? booking.getUser().getUsername() : "未知用戶";

        return new BookingDto(
                booking.getId(),
                booking.getPet().getId(),
                booking.getPet().getName(),
                booking.getSitter().getId(),
                booking.getSitter().getName(),
                userId,
                username,
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getStatus(),
                booking.getNotes(),
                booking.getSitterResponse(),
                booking.getTotalPrice(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
