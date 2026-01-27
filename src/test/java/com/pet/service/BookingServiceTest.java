package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.BookingDto;
import com.pet.dto.BookingStatusUpdateDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ResourceNotFoundException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService 測試")
class BookingServiceTest {

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

    @Mock
    private LineMessagingService lineMessagingService;

    @InjectMocks
    private BookingService bookingService;

    private UUID testUserId;
    private UUID testPetId;
    private UUID testSitterId;
    private UUID testBookingId;

    private Users testUser;
    private Pet testPet;
    private Sitter testSitter;
    private Booking testBooking;
    private BookingDto testBookingDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testPetId = UUID.randomUUID();
        testSitterId = UUID.randomUUID();
        testBookingId = UUID.randomUUID();

        // 建立測試用戶
        testUser = new Users();
        testUser.setId(testUserId);
        testUser.setUsername("test_user");
        testUser.setRole(UserRole.CUSTOMER);

        // 建立測試寵物
        testPet = new Dog();
        testPet.setId(testPetId);
        testPet.setName("小黑");

        // 建立測試保母
        testSitter = new Sitter();
        testSitter.setId(testSitterId);
        testSitter.setName("王保母");
        testSitter.setHourlyRate(200.0);
        testSitter.setCompletedBookings(0);

        // 建立測試預約
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        testBooking = new Booking();
        testBooking.setId(testBookingId);
        testBooking.setPet(testPet);
        testBooking.setSitter(testSitter);
        testBooking.setUser(testUser);
        testBooking.setStartTime(startTime);
        testBooking.setEndTime(endTime);
        testBooking.setStatus(BookingStatus.PENDING);
        testBooking.setTotalPrice(400.0);
        testBooking.setNotes("測試預約");

        testBookingDto = new BookingDto(
                null,
                testPetId,
                "小黑",
                testSitterId,
                "王保母",
                testUserId,
                "test_user",
                startTime,
                endTime,
                BookingStatus.PENDING,
                "測試預約",
                null,
                400.0,
                null,
                null
        );
    }

    @Nested
    @DisplayName("建立預約測試")
    class CreateBookingTests {

        @Test
        @DisplayName("應該成功建立預約")
        void shouldCreateBooking() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                // 注意：新流程使用樂觀鎖（findById），不再使用悲觀鎖（findByIdWithLock）
                given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
                given(bookingRepository.countConflictingBookings(any(), any(), any())).willReturn(0L);
                given(petRepository.findById(testPetId)).willReturn(Optional.of(testPet));
                given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
                given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);

                // when
                BookingDto result = bookingService.createBooking(testBookingDto, testUserId);

                // then
                assertThat(result).isNotNull();
                assertThat(result.petName()).isEqualTo("小黑");
                assertThat(result.sitterName()).isEqualTo("王保母");
                assertThat(result.status()).isEqualTo(BookingStatus.PENDING);
                verify(bookingRepository).save(any(Booking.class));
                // 確認使用的是 findById 而非 findByIdWithLock
                verify(sitterRepository).findById(testSitterId);
                verify(sitterRepository, never()).findByIdWithLock(any());
            }
        }

        @Test
        @DisplayName("當寵物不存在時應該拋出例外")
        void shouldThrowExceptionWhenPetNotFound() {
            // given
            // 新流程：先取得保母（樂觀鎖），檢查時段衝突，再取得寵物
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
            given(bookingRepository.countConflictingBookings(any(), any(), any())).willReturn(0L);
            given(petRepository.findById(testPetId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingDto, testUserId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("寵物");
        }

        @Test
        @DisplayName("當保母不存在時應該拋出例外")
        void shouldThrowExceptionWhenSitterNotFound() {
            // given
            // 新流程：使用樂觀鎖取得保母，若不存在直接拋例外
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingDto, testUserId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("保母");
        }

        @Test
        @DisplayName("當時間衝突時應該拋出例外")
        void shouldThrowExceptionWhenTimeConflict() {
            // given
            // 新流程：使用樂觀鎖取得保母，再檢查時段衝突
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
            given(bookingRepository.countConflictingBookings(any(), any(), any())).willReturn(1L);

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingDto, testUserId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("當開始時間晚於結束時間時應該拋出例外")
        void shouldThrowExceptionWhenStartTimeAfterEndTime() {
            // given
            LocalDateTime startTime = LocalDateTime.now().plusDays(1);
            LocalDateTime endTime = startTime.minusHours(1); // 結束時間早於開始時間

            BookingDto invalidDto = new BookingDto(
                    null, testPetId, "小黑", testSitterId, "王保母",
                    testUserId, "test_user", startTime, endTime,
                    BookingStatus.PENDING, "測試", null, 400.0, null, null
            );

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(invalidDto, testUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("開始時間不能晚於結束時間");
        }

        @Test
        @DisplayName("當開始時間是過去時間時應該拋出例外")
        void shouldThrowExceptionWhenStartTimeInPast() {
            // given
            LocalDateTime pastTime = LocalDateTime.now().minusHours(1);
            LocalDateTime endTime = LocalDateTime.now().plusHours(1);

            BookingDto invalidDto = new BookingDto(
                    null, testPetId, "小黑", testSitterId, "王保母",
                    testUserId, "test_user", pastTime, endTime,
                    BookingStatus.PENDING, "測試", null, 400.0, null, null
            );

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(invalidDto, testUserId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("開始時間不能是過去時間");
        }
    }

    @Nested
    @DisplayName("更新預約狀態測試")
    class UpdateBookingStatusTests {

        @Test
        @DisplayName("應該成功確認預約")
        void shouldConfirmBooking() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
                // 新流程：確認預約時不再使用悲觀鎖，改用樂觀鎖 + countConflictingBookingsExcluding
                given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(testBookingId);
                confirmedBooking.setPet(testPet);
                confirmedBooking.setSitter(testSitter);
                confirmedBooking.setUser(testUser);
                confirmedBooking.setStartTime(testBooking.getStartTime());
                confirmedBooking.setEndTime(testBooking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED,
                        "我可以接受這個預約"
                );

                // when
                BookingDto result = bookingService.updateBookingStatus(testBookingId, updateDto);

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
                verify(bookingRepository).save(any(Booking.class));
                // 確認不再使用悲觀鎖
                verify(sitterRepository, never()).findByIdWithLock(any());
            }
        }

        @Test
        @DisplayName("應該成功完成預約並更新保母統計")
        void shouldCompleteBookingAndUpdateSitterStats() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                testBooking.setStatus(BookingStatus.CONFIRMED);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking completedBooking = new Booking();
                completedBooking.setId(testBookingId);
                completedBooking.setPet(testPet);
                completedBooking.setSitter(testSitter);
                completedBooking.setUser(testUser);
                completedBooking.setStartTime(testBooking.getStartTime());
                completedBooking.setEndTime(testBooking.getEndTime());
                completedBooking.setStatus(BookingStatus.COMPLETED);
                completedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(completedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.COMPLETED,
                        null
                );

                // when
                BookingDto result = bookingService.updateBookingStatus(testBookingId, updateDto);

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.COMPLETED);
                verify(sitterRepository).save(any(Sitter.class));
                assertThat(testSitter.getCompletedBookings()).isEqualTo(1);
            }
        }

        @Test
        @DisplayName("當預約不存在時應該拋出例外")
        void shouldThrowExceptionWhenBookingNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(bookingRepository.findById(unknownId)).willReturn(Optional.empty());

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED,
                    "確認"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(unknownId, updateDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("當狀態轉換不合法時應該拋出例外")
        void shouldThrowExceptionWhenInvalidStatusTransition() {
            // given
            testBooking.setStatus(BookingStatus.COMPLETED); // 已完成的預約
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.PENDING, // 嘗試轉回 PENDING（不合法）
                    "測試"
            );

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(testBookingId, updateDto))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("無法從");
        }
    }

    @Nested
    @DisplayName("查詢預約測試")
    class GetBookingTests {

        @Test
        @DisplayName("應該根據 ID 取得預約")
        void shouldGetBookingById() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when
            BookingDto result = bookingService.getBookingById(testBookingId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testBookingId);
            assertThat(result.petName()).isEqualTo("小黑");
        }

        @Test
        @DisplayName("應該取得使用者的所有預約")
        void shouldGetBookingsByUser() {
            // given
            given(bookingRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                    .willReturn(List.of(testBooking));

            // when
            List<BookingDto> result = bookingService.getBookingsByUser(testUserId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).userId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("應該取得保母的所有預約")
        void shouldGetBookingsBySitter() {
            // given
            given(bookingRepository.findBySitterIdOrderByStartTimeDesc(testSitterId))
                    .willReturn(List.of(testBooking));

            // when
            List<BookingDto> result = bookingService.getBookingsBySitter(testSitterId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).sitterId()).isEqualTo(testSitterId);
        }

        @Test
        @DisplayName("應該取得保母待處理的預約")
        void shouldGetPendingBookingsForSitter() {
            // given
            given(bookingRepository.findBySitterIdAndStatus(testSitterId, BookingStatus.PENDING))
                    .willReturn(List.of(testBooking));

            // when
            List<BookingDto> result = bookingService.getPendingBookingsForSitter(testSitterId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(BookingStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("便捷方法測試")
    class ConvenienceMethodTests {

        @Test
        @DisplayName("應該成功取消預約")
        void shouldCancelBooking() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking cancelledBooking = new Booking();
                cancelledBooking.setId(testBookingId);
                cancelledBooking.setPet(testPet);
                cancelledBooking.setSitter(testSitter);
                cancelledBooking.setUser(testUser);
                cancelledBooking.setStartTime(testBooking.getStartTime());
                cancelledBooking.setEndTime(testBooking.getEndTime());
                cancelledBooking.setStatus(BookingStatus.CANCELLED);
                cancelledBooking.setTotalPrice(400.0);
                cancelledBooking.setSitterResponse("臨時有事");

                given(bookingRepository.save(any(Booking.class))).willReturn(cancelledBooking);

                // when
                BookingDto result = bookingService.cancelBooking(testBookingId, "臨時有事");

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
                assertThat(result.sitterResponse()).isEqualTo("臨時有事");
            }
        }

        @Test
        @DisplayName("應該成功拒絕預約")
        void shouldRejectBooking() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking rejectedBooking = new Booking();
                rejectedBooking.setId(testBookingId);
                rejectedBooking.setPet(testPet);
                rejectedBooking.setSitter(testSitter);
                rejectedBooking.setUser(testUser);
                rejectedBooking.setStartTime(testBooking.getStartTime());
                rejectedBooking.setEndTime(testBooking.getEndTime());
                rejectedBooking.setStatus(BookingStatus.REJECTED);
                rejectedBooking.setTotalPrice(400.0);
                rejectedBooking.setSitterResponse("時間不合");

                given(bookingRepository.save(any(Booking.class))).willReturn(rejectedBooking);

                // when
                BookingDto result = bookingService.rejectBooking(testBookingId, "時間不合");

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.REJECTED);
                assertThat(result.sitterResponse()).isEqualTo("時間不合");
            }
        }
    }

    @Nested
    @DisplayName("時段衝突檢查測試（樂觀鎖版本）")
    class ConflictCheckingTests {

        @Test
        @DisplayName("createBooking 應該檢查時段衝突並拋出例外")
        void createBookingShouldCheckConflictAndThrow() {
            // given
            // 樂觀鎖版本：使用 findById 而非 findByIdWithLock
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
            given(bookingRepository.countConflictingBookings(
                    eq(testSitterId), any(), any())).willReturn(1L);

            // when & then
            assertThatThrownBy(() -> bookingService.createBooking(testBookingDto, testUserId))
                    .isInstanceOf(BusinessException.class);

            // 確認使用樂觀鎖版本
            verify(sitterRepository).findById(testSitterId);
            verify(sitterRepository, never()).findByIdWithLock(any());
            // 確認未嘗試儲存預約（因為衝突）
            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("createBooking 無衝突時應該成功建立預約")
        void createBookingShouldSucceedWhenNoConflict() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
                given(bookingRepository.countConflictingBookings(any(), any(), any())).willReturn(0L);
                given(petRepository.findById(testPetId)).willReturn(Optional.of(testPet));
                given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
                given(bookingRepository.save(any(Booking.class))).willReturn(testBooking);

                // when
                BookingDto result = bookingService.createBooking(testBookingDto, testUserId);

                // then
                assertThat(result).isNotNull();
                verify(bookingRepository).countConflictingBookings(any(), any(), any());
                verify(bookingRepository).save(any(Booking.class));
            }
        }

        @Test
        @DisplayName("updateBookingStatus CONFIRMED 應該檢查時段衝突")
        void updateStatusToConfirmedShouldCheckConflict() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
                // 無衝突
                given(bookingRepository.countConflictingBookingsExcluding(
                        eq(testSitterId), any(), any(), eq(testBookingId))).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(testBookingId);
                confirmedBooking.setPet(testPet);
                confirmedBooking.setSitter(testSitter);
                confirmedBooking.setUser(testUser);
                confirmedBooking.setStartTime(testBooking.getStartTime());
                confirmedBooking.setEndTime(testBooking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED, "確認");

                // when
                BookingDto result = bookingService.updateBookingStatus(testBookingId, updateDto);

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
                // 確認有呼叫衝突檢查
                verify(bookingRepository).countConflictingBookingsExcluding(
                        eq(testSitterId), any(), any(), eq(testBookingId));
                // 確認不使用悲觀鎖
                verify(sitterRepository, never()).findByIdWithLock(any());
            }
        }

        @Test
        @DisplayName("updateBookingStatus CONFIRMED 有衝突時應該拋出例外")
        void updateStatusToConfirmedShouldThrowWhenConflict() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
            // 有衝突：已有其他確認的預約
            given(bookingRepository.countConflictingBookingsExcluding(
                    eq(testSitterId), any(), any(), eq(testBookingId))).willReturn(1L);

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED, "確認");

            // when & then
            assertThatThrownBy(() -> bookingService.updateBookingStatus(testBookingId, updateDto))
                    .isInstanceOf(BusinessException.class);

            // 確認未嘗試儲存（因為衝突）
            verify(bookingRepository, never()).save(any(Booking.class));
        }

        @Test
        @DisplayName("updateBookingStatus 非 CONFIRMED 狀態不應檢查時段衝突")
        void updateStatusToNonConfirmedShouldNotCheckConflict() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking cancelledBooking = new Booking();
                cancelledBooking.setId(testBookingId);
                cancelledBooking.setPet(testPet);
                cancelledBooking.setSitter(testSitter);
                cancelledBooking.setUser(testUser);
                cancelledBooking.setStartTime(testBooking.getStartTime());
                cancelledBooking.setEndTime(testBooking.getEndTime());
                cancelledBooking.setStatus(BookingStatus.CANCELLED);
                cancelledBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(cancelledBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CANCELLED, "取消原因");

                // when
                BookingDto result = bookingService.updateBookingStatus(testBookingId, updateDto);

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.CANCELLED);
                // 確認沒有呼叫衝突檢查（只有 CONFIRMED 需要）
                verify(bookingRepository, never()).countConflictingBookingsExcluding(any(), any(), any(), any());
            }
        }
    }

    @Nested
    @DisplayName("LINE 通知測試")
    class LineNotificationTests {

        @Test
        @DisplayName("確認預約時應該註冊 LINE 通知 callback")
        void shouldRegisterLineNotificationOnConfirm() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                ArgumentCaptor<TransactionSynchronization> syncCaptor =
                        ArgumentCaptor.forClass(TransactionSynchronization.class);

                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
                given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(testBookingId);
                confirmedBooking.setPet(testPet);
                confirmedBooking.setSitter(testSitter);
                confirmedBooking.setUser(testUser);
                confirmedBooking.setStartTime(testBooking.getStartTime());
                confirmedBooking.setEndTime(testBooking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED, "確認");

                // when
                bookingService.updateBookingStatus(testBookingId, updateDto);

                // then
                // 確認有註冊 TransactionSynchronization（應該有兩個：log sync 和 LINE 通知）
                txManager.verify(() ->
                        TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()),
                        times(2));
            }
        }

        @Test
        @DisplayName("取消預約時應該註冊 LINE 通知 callback")
        void shouldRegisterLineNotificationOnCancel() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                ArgumentCaptor<TransactionSynchronization> syncCaptor =
                        ArgumentCaptor.forClass(TransactionSynchronization.class);

                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking cancelledBooking = new Booking();
                cancelledBooking.setId(testBookingId);
                cancelledBooking.setPet(testPet);
                cancelledBooking.setSitter(testSitter);
                cancelledBooking.setUser(testUser);
                cancelledBooking.setStartTime(testBooking.getStartTime());
                cancelledBooking.setEndTime(testBooking.getEndTime());
                cancelledBooking.setStatus(BookingStatus.CANCELLED);
                cancelledBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(cancelledBooking);

                // when
                bookingService.cancelBooking(testBookingId, "臨時有事");

                // then
                // 確認有註冊 TransactionSynchronization
                txManager.verify(() ->
                        TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()),
                        times(2));
            }
        }

        @Test
        @DisplayName("完成預約時應該註冊 LINE 通知 callback")
        void shouldRegisterLineNotificationOnComplete() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                ArgumentCaptor<TransactionSynchronization> syncCaptor =
                        ArgumentCaptor.forClass(TransactionSynchronization.class);

                testBooking.setStatus(BookingStatus.CONFIRMED); // 需要先是 CONFIRMED 才能 COMPLETE
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking completedBooking = new Booking();
                completedBooking.setId(testBookingId);
                completedBooking.setPet(testPet);
                completedBooking.setSitter(testSitter);
                completedBooking.setUser(testUser);
                completedBooking.setStartTime(testBooking.getStartTime());
                completedBooking.setEndTime(testBooking.getEndTime());
                completedBooking.setStatus(BookingStatus.COMPLETED);
                completedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(completedBooking);

                // when
                bookingService.completeBooking(testBookingId);

                // then
                // 確認有註冊 TransactionSynchronization
                txManager.verify(() ->
                        TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()),
                        times(2));
            }
        }

        @Test
        @DisplayName("拒絕預約時應該註冊 LINE 通知 callback")
        void shouldRegisterLineNotificationOnReject() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                ArgumentCaptor<TransactionSynchronization> syncCaptor =
                        ArgumentCaptor.forClass(TransactionSynchronization.class);

                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

                Booking rejectedBooking = new Booking();
                rejectedBooking.setId(testBookingId);
                rejectedBooking.setPet(testPet);
                rejectedBooking.setSitter(testSitter);
                rejectedBooking.setUser(testUser);
                rejectedBooking.setStartTime(testBooking.getStartTime());
                rejectedBooking.setEndTime(testBooking.getEndTime());
                rejectedBooking.setStatus(BookingStatus.REJECTED);
                rejectedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(rejectedBooking);

                // when
                bookingService.rejectBooking(testBookingId, "時間不合");

                // then
                // 確認有註冊 TransactionSynchronization
                txManager.verify(() ->
                        TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()),
                        times(2));
            }
        }
    }

    @Nested
    @DisplayName("悲觀鎖版本測試（MSSQL 相容）")
    class PessimisticLockVersionTests {

        @Test
        @DisplayName("updateBookingStatusWithPessimisticLock 應該使用 findById 而非 findByIdWithLock")
        void pessimisticLockVersionShouldUseFindById() {
            try (MockedStatic<TransactionSynchronizationManager> txManager = mockStatic(TransactionSynchronizationManager.class)) {
                // given
                txManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);
                // MSSQL 相容：改用 findById
                given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
                given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(0L);

                Booking confirmedBooking = new Booking();
                confirmedBooking.setId(testBookingId);
                confirmedBooking.setPet(testPet);
                confirmedBooking.setSitter(testSitter);
                confirmedBooking.setUser(testUser);
                confirmedBooking.setStartTime(testBooking.getStartTime());
                confirmedBooking.setEndTime(testBooking.getEndTime());
                confirmedBooking.setStatus(BookingStatus.CONFIRMED);
                confirmedBooking.setTotalPrice(400.0);

                given(bookingRepository.save(any(Booking.class))).willReturn(confirmedBooking);

                BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                        BookingStatus.CONFIRMED, "確認");

                // when
                BookingDto result = bookingService.updateBookingStatusWithPessimisticLock(testBookingId, updateDto);

                // then
                assertThat(result.status()).isEqualTo(BookingStatus.CONFIRMED);
                // 確認使用 findById 而非 findByIdWithLock（MSSQL 相容）
                verify(bookingRepository).findById(testBookingId);
                verify(bookingRepository, never()).findByIdWithLock(any());
                verify(sitterRepository, never()).findByIdWithLock(any());
            }
        }

        @Test
        @DisplayName("updateBookingStatusWithPessimisticLock 有衝突時應該拋出例外")
        void pessimisticLockVersionShouldThrowOnConflict() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
            given(bookingRepository.countConflictingBookingsExcluding(any(), any(), any(), any())).willReturn(1L);

            BookingStatusUpdateDto updateDto = new BookingStatusUpdateDto(
                    BookingStatus.CONFIRMED, "確認");

            // when & then
            assertThatThrownBy(() ->
                    bookingService.updateBookingStatusWithPessimisticLock(testBookingId, updateDto))
                    .isInstanceOf(BusinessException.class);

            verify(bookingRepository, never()).save(any(Booking.class));
        }
    }
}
