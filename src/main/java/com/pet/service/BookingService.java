package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.BookingDto;
import com.pet.dto.BookingStatusUpdateDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.exception.ResourceNotFoundException;
import com.pet.log.service.BookingLogService;
import com.pet.pricing.PricingStrategyFactory;
import com.pet.repository.BookingRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.SitterRepository;
import com.pet.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
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
     * 使用悲觀鎖防止時段衝突的 race condition
     *
     * 問題場景：兩個飼主同時對同一保母的同一時段建立預約
     * 若不加鎖，兩個 transaction 可能同時通過 countConflictingBookings 檢查，導致重複預約
     *
     * 解法：先用悲觀鎖鎖定保母資料列，確保同一時間只有一個 transaction 可以進行檢查和寫入
     */
    public BookingDto createBooking(BookingDto dto, UUID userId) {
        // 1. 驗證時間
        validateBookingTime(dto.startTime(), dto.endTime());

        // 2. 使用悲觀鎖取得保母（防止併發建立預約的 race condition）
        // 其他嘗試為同一保母建立預約的 transaction 會在此等待
        Sitter sitter = sitterRepository.findByIdWithLock(dto.sitterId())
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", dto.sitterId()));

        // 3. 檢查時段是否已被預約（此時已持有鎖，檢查是安全的）
        if (bookingRepository.countConflictingBookings(dto.sitterId(), dto.startTime(), dto.endTime()) > 0) {
            throw new BusinessException(ErrorCode.BOOKING_CONFLICT);
        }

        // 4. 取得其他關聯實體
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", dto.petId()));
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", "id", userId));

        // 5. 計算費用（後端計算，不依賴前端傳入）
        double totalPrice = calculateBookingPrice(sitter, dto.startTime(), dto.endTime());

        // 6. 建立預約
        Booking booking = new Booking();
        booking.setPet(pet);
        booking.setSitter(sitter);
        booking.setUser(user);
        booking.setStartTime(dto.startTime());
        booking.setEndTime(dto.endTime());
        booking.setNotes(dto.notes());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(totalPrice);

        Booking saved = bookingRepository.save(booking);

        // 註冊 afterCommit callback，在主交易 commit 後才同步到 Log DB
        registerAfterCommitSync(saved);

        // TODO: 發送通知給保母（Domain Event）
        // eventPublisher.publishEvent(new BookingCreatedEvent(saved));

        return convertToDto(saved);
    }

    /**
     * 更新預約狀態（使用樂觀鎖 + 確認時悲觀鎖防止時段衝突）
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

            // 如果是確認預約，使用悲觀鎖防止併發確認導致時段衝突
            if (updateDto.targetStatus() == BookingStatus.CONFIRMED) {
                // 鎖定保母資料列，確保同時只有一個確認操作
                sitterRepository.findByIdWithLock(booking.getSitter().getId());

                // 此時已持有鎖，檢查是安全的
                if (bookingRepository.countConflictingBookingsExcluding(
                        booking.getSitter().getId(),
                        booking.getStartTime(),
                        booking.getEndTime(),
                        bookingId) > 0) {
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

            // 註冊 afterCommit callback，在主交易 commit 後才同步到 Log DB
            registerAfterCommitSync(updated);

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
     * 更新預約狀態（使用悲觀鎖）
     * 使用場景：
     * 1. 高併發環境下的訂單確認
     * 2. 需要強一致性保證的業務場景
     * 3. 預期衝突率較高的情況
     * 與樂觀鎖的差異：
     * - 樂觀鎖：假設衝突不常發生，在 commit 時才檢查版本號，發生衝突時拋出異常
     * - 悲觀鎖：假設衝突經常發生，在讀取時就加鎖，阻止其他交易同時讀取
     */
    public BookingDto updateBookingStatusWithPessimisticLock(UUID bookingId, BookingStatusUpdateDto updateDto) {
        // 使用悲觀寫鎖查詢，其他交易必須等待此鎖釋放
        Booking booking = bookingRepository.findByIdWithLock(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("預約", "id", bookingId));

        logger.info("已使用悲觀鎖鎖定預約 {}", bookingId);

        // 驗證狀態轉換是否合法
        if (!booking.canTransitionTo(updateDto.targetStatus())) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS_TRANSITION,
                    String.format("無法從 %s 轉換到 %s", booking.getStatus(), updateDto.targetStatus()));
        }

        // 如果是確認預約，也鎖定保母以防止時段衝突
        if (updateDto.targetStatus() == BookingStatus.CONFIRMED) {
            // 鎖定保母資料列，確保同時只有一個確認操作
            sitterRepository.findByIdWithLock(booking.getSitter().getId());

            if (bookingRepository.countConflictingBookingsExcluding(
                    booking.getSitter().getId(),
                    booking.getStartTime(),
                    booking.getEndTime(),
                    bookingId) > 0) {
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

        // 註冊 afterCommit callback，在主交易 commit 後才同步到 Log DB
        registerAfterCommitSync(updated);

        logger.info("悲觀鎖：預約 {} 狀態已更新為 {}", bookingId, updateDto.targetStatus());

        return convertToDto(updated);
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
     * 註冊 afterCommit callback
     * 確保只在主交易成功 commit 後才同步到 Log DB
     * 這樣可以避免：
     * 1. 主 DB rollback 但 log 已寫入的不一致問題
     * 2. log 寫入拖長主交易時間的效能問題
     */
    private void registerAfterCommitSync(Booking booking) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    logger.info("Main transaction committed, syncing booking {} to log DB", booking.getId());
                    bookingLogService.syncBookingToLog(booking);
                } catch (Exception e) {
                    // Log DB 寫入失敗不影響主流程（主交易已經 commit）
                    logger.error("Failed to sync booking {} to log DB after commit: {}",
                            booking.getId(), e.getMessage());
                }
            }
        });
    }

    /**
     * 計算預約費用
     * 使用保母的經驗等級和時薪來計算
     */
    private double calculateBookingPrice(Sitter sitter, LocalDateTime startTime, LocalDateTime endTime) {
        // 計算時長（小時）
        Duration duration = Duration.between(startTime, endTime);
        long hours = duration.toHours();

        // 使用計費策略計算費用
        return PricingStrategyFactory.calculateBookingPrice(
                sitter.getExperienceLevel(),
                sitter.getHourlyRate(),
                (int) hours
        );
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
