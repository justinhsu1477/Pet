package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.BookingDto;
import com.pet.dto.BookingStatusUpdateDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.log.service.BookingLogService;
import com.pet.repository.BookingRepository;
import com.pet.repository.PetRepository;
import com.pet.repository.SitterRepository;
import com.pet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 樂觀鎖測試
 *
 * 測試目的：
 * 1. 驗證併發更新時樂觀鎖機制正確運作
 * 2. 驗證 ObjectOptimisticLockingFailureException 被正確轉換為 BusinessException
 * 3. 驗證錯誤訊息對用戶友好
 *
 * 樂觀鎖原理：
 * - Booking 實體使用 @Version 欄位
 * - 更新時 JPA 會檢查版本號是否一致
 * - 若版本不一致（表示其他人已更新），拋出 ObjectOptimisticLockingFailureException
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Booking 樂觀鎖測試")
class BookingOptimisticLockTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private SitterRepository sitterRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingLogService bookingLogService;

    @InjectMocks
    private BookingService bookingService;

    private UUID bookingId;
    private Booking booking;
    private Users user;
    private Pet pet;
    private Sitter sitter;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();

        // 建立測試用戶
        user = new Users();
        user.setId(UUID.randomUUID());
        user.setUsername("test_user");
        user.setRole(UserRole.CUSTOMER);

        // 建立測試寵物
        pet = new Dog();
        pet.setId(UUID.randomUUID());
        pet.setName("小黑");

        // 建立測試保母
        sitter = new Sitter();
        sitter.setId(UUID.randomUUID());
        sitter.setName("王保母");
        sitter.setHourlyRate(200.0);
        sitter.setCompletedBookings(0);

        // 建立測試預約（PENDING 狀態）
        booking = new Booking();
        booking.setId(bookingId);
        booking.setPet(pet);
        booking.setSitter(sitter);
        booking.setUser(user);
        booking.setStartTime(LocalDateTime.now().plusDays(1));
        booking.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(400.0);
    }

    @Nested
    @DisplayName("樂觀鎖衝突場景")
    class OptimisticLockConflictTests {

        @Test
        @DisplayName("當發生樂觀鎖衝突時，應該拋出 BusinessException 並顯示友好訊息")
        void shouldThrowBusinessExceptionWhenOptimisticLockConflict() {
            // given - 模擬第一次讀取成功
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

            // 模擬 save 時拋出樂觀鎖異常（表示另一個用戶已經更新過這筆資料）
            given(bookingRepository.save(any(Booking.class)))
                    .willThrow(new ObjectOptimisticLockingFailureException(Booking.class, bookingId));

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED,
                    "我可以接受"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(bookingId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("預約狀態已被其他操作更新")
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.BOOKING_ALREADY_PROCESSED);
                    });

            // verify - 確認有嘗試儲存
            verify(bookingRepository).save(any(Booking.class));
        }

        @Test
        @DisplayName("模擬情境：保母 A 和保母 B 同時確認同一筆預約")
        void shouldHandleConcurrentConfirmation() {
            /*
             * 情境說明：
             * 1. 保母 A 讀取預約（version = 0）
             * 2. 保母 B 讀取預約（version = 0）
             * 3. 保母 A 確認預約並儲存（version 變成 1）
             * 4. 保母 B 嘗試確認預約，但版本號不符（預期是 0，實際是 1）
             * 5. 系統拋出樂觀鎖異常，保母 B 收到友好錯誤訊息
             */

            // given
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

            // 模擬樂觀鎖衝突
            given(bookingRepository.save(any(Booking.class)))
                    .willThrow(new ObjectOptimisticLockingFailureException(
                            "Row was updated or deleted by another transaction",
                            new Exception("version mismatch")));

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED,
                    "保母 B 的確認"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(bookingId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("請重新整理後再試");
        }

        @Test
        @DisplayName("模擬情境：用戶取消預約時，保母同時確認")
        void shouldHandleConcurrentCancelAndConfirm() {
            /*
             * 情境說明：
             * 1. 用戶想取消預約
             * 2. 保母同時想確認預約
             * 3. 其中一方會因為樂觀鎖而失敗
             */

            // given
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));

            // 模擬樂觀鎖衝突（保母已經先確認了）
            given(bookingRepository.save(any(Booking.class)))
                    .willThrow(new ObjectOptimisticLockingFailureException(Booking.class, bookingId));

            BookingStatusUpdateDto cancelDto = new BookingStatusUpdateDto(
                    BookingStatus.CANCELLED,
                    "用戶想取消"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(bookingId, cancelDto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.BOOKING_ALREADY_PROCESSED);
                    });
        }
    }

    @Nested
    @DisplayName("樂觀鎖正常流程")
    class OptimisticLockNormalFlowTests {

        @Test
        @DisplayName("無衝突時應該成功更新狀態")
        void shouldSuccessfullyUpdateWhenNoConflict() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
                given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(bookingId);
                confirmedBooking.setPet(pet);
                confirmedBooking.setSitter(sitter);
                confirmedBooking.setUser(user);
                confirmedBooking.setStartTime(booking.getStartTime());
                confirmedBooking.setEndTime(booking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED,
                        "確認預約"
                );

                // when
                BookingDto result = bookingService.updateBookingStatus(bookingId, updateDto);

                // then
                assertThat(result).isNotNull();
                assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
                verify(bookingRepository, times(1)).save(any(Booking.class));
            }
        }

        @Test
        @DisplayName("第一次樂觀鎖衝突後，用戶重新整理應該能看到最新狀態")
        void shouldSeeLatestStateAfterRefresh() {
            /*
             * 情境說明：
             * 1. 用戶 A 嘗試更新但發生樂觀鎖衝突
             * 2. 用戶 A 重新整理頁面，讀取最新資料
             * 3. 此時應該能看到其他用戶更新後的狀態
             */

            // given - 第二次讀取應該能看到最新狀態（已被確認）
            Booking updatedBooking = new Booking();
            updatedBooking.setId(bookingId);
            updatedBooking.setPet(pet);
            updatedBooking.setSitter(sitter);
            updatedBooking.setUser(user);
            updatedBooking.setStartTime(booking.getStartTime());
            updatedBooking.setEndTime(booking.getEndTime());
            updatedBooking.setStatus(BookingStatus.CONFIRMED); // 已被其他人確認
            updatedBooking.setTotalPrice(400.0);
            updatedBooking.setSitterResponse("其他保母已確認");

            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(updatedBooking));

            // when - 重新讀取
            BookingDto result = bookingService.getBookingById(bookingId);

            // then - 應該看到最新狀態
            assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
            assertThat(result.sitterResponse()).isEqualTo("其他保母已確認");
        }
    }

    @Nested
    @DisplayName("樂觀鎖 vs 悲觀鎖比較測試")
    class OptimisticVsPessimisticLockTests {

        @Test
        @DisplayName("樂觀鎖：衝突時拋出異常，不阻塞其他操作")
        void optimisticLockShouldThrowExceptionOnConflict() {
            // given
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);
            given(bookingRepository.save(any(Booking.class)))
                    .willThrow(new ObjectOptimisticLockingFailureException(Booking.class, bookingId));

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED,
                    "確認"
            );

            // when & then - 樂觀鎖衝突時拋出 BusinessException
            assertThatThrownBy(() -> bookingService.updateBookingStatus(bookingId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("預約狀態已被其他操作更新");
        }

        @Test
        @DisplayName("悲觀鎖：使用 findByIdWithLock 時不會發生版本衝突")
        void pessimisticLockShouldNotHaveVersionConflict() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

                // 悲觀鎖查詢
                given(bookingRepository.findByIdWithLock(bookingId)).willReturn(Optional.of(booking));
                given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(bookingId);
                confirmedBooking.setPet(pet);
                confirmedBooking.setSitter(sitter);
                confirmedBooking.setUser(user);
                confirmedBooking.setStartTime(booking.getStartTime());
                confirmedBooking.setEndTime(booking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                // 悲觀鎖情況下，save 不會因為版本衝突而失敗
                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED,
                        "確認"
                );

                // when - 使用悲觀鎖更新
                BookingDto result = bookingService.updateBookingStatusWithPessimisticLock(bookingId, updateDto);

                // then - 悲觀鎖應該成功更新
                assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);

                // 驗證使用了悲觀鎖查詢
                verify(bookingRepository).findByIdWithLock(bookingId);
                verify(bookingRepository, never()).findById(bookingId);
            }
        }
    }

    @Nested
    @DisplayName("錯誤碼驗證")
    class ErrorCodeValidationTests {

        @Test
        @DisplayName("樂觀鎖衝突應該使用 BOOKING_ALREADY_PROCESSED 錯誤碼")
        void shouldUseCorrectErrorCode() {
            // given
            given(bookingRepository.findById(bookingId)).willReturn(Optional.of(booking));
            given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);
            given(bookingRepository.save(any(Booking.class)))
                    .willThrow(new ObjectOptimisticLockingFailureException(Booking.class, bookingId));

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED,
                    "確認"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(bookingId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> {
                        BusinessException be = (BusinessException) ex;
                        // 驗證錯誤碼
                        assertThat(be.getErrorCode()).isEqualTo(ErrorCode.BOOKING_ALREADY_PROCESSED);
                        // 驗證錯誤訊息對用戶友好
                        assertThat(be.getMessage()).contains("重新整理");
                    });
        }
    }
}