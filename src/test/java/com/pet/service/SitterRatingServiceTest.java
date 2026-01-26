package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.SitterRatingDto;
import com.pet.dto.SitterRatingStatsDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
import com.pet.repository.SitterRatingRepository;
import com.pet.repository.SitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SitterRatingService 測試")
class SitterRatingServiceTest {

    @Mock
    private SitterRatingRepository ratingRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private SitterRepository sitterRepository;

    @InjectMocks
    private SitterRatingService ratingService;

    private UUID testUserId;
    private UUID testSitterId;
    private UUID testBookingId;
    private UUID testRatingId;

    private Users testUser;
    private Sitter testSitter;
    private Booking testBooking;
    private SitterRating testRating;
    private SitterRatingDto testRatingDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testSitterId = UUID.randomUUID();
        testBookingId = UUID.randomUUID();
        testRatingId = UUID.randomUUID();

        // 建立測試用戶
        testUser = new Users();
        testUser.setId(testUserId);
        testUser.setUsername("test_user");

        // 建立測試保母
        testSitter = new Sitter();
        testSitter.setId(testSitterId);
        testSitter.setName("王保母");
        testSitter.setAverageRating(4.5);
        testSitter.setRatingCount(10);
        testSitter.setCompletedBookings(15);

        // 建立測試預約（已完成狀態）
        testBooking = new Booking();
        testBooking.setId(testBookingId);
        testBooking.setUser(testUser);
        testBooking.setSitter(testSitter);
        testBooking.setStatus(BookingStatus.COMPLETED);
        testBooking.setStartTime(LocalDateTime.now().minusDays(2));
        testBooking.setEndTime(LocalDateTime.now().minusDays(2).plusHours(2));

        // 建立測試評價
        testRating = new SitterRating();
        testRating.setId(testRatingId);
        testRating.setBooking(testBooking);
        testRating.setSitter(testSitter);
        testRating.setUser(testUser);
        testRating.setOverallRating(5);
        testRating.setProfessionalismRating(5);
        testRating.setCommunicationRating(4);
        testRating.setPunctualityRating(5);
        testRating.setComment("非常好的保母！");
        testRating.setIsAnonymous(false);

        testRatingDto = new SitterRatingDto(
                null,
                testBookingId,
                testSitterId,
                "王保母",
                testUserId,
                "test_user",
                5,
                5,
                4,
                5,
                "非常好的保母！",
                null,
                false,
                4.85,
                null
        );
    }

    @Nested
    @DisplayName("建立評價測試")
    class CreateRatingTests {

        @Test
        @DisplayName("應該成功建立評價")
        void shouldCreateRating() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
            given(ratingRepository.existsByBookingId(testBookingId)).willReturn(false);
            given(ratingRepository.save(any(SitterRating.class))).willReturn(testRating);
            given(ratingRepository.calculateWeightedAverageRating(testSitterId)).willReturn(4.8);
            given(ratingRepository.countBySitter_Id(testSitterId)).willReturn(11L);
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));

            // when
            SitterRatingDto result = ratingService.createRating(testRatingDto, testUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.overallRating()).isEqualTo(5);
            assertThat(result.comment()).isEqualTo("非常好的保母！");
            verify(ratingRepository).save(any(SitterRating.class));
            verify(sitterRepository).save(any(Sitter.class));
        }

        @Test
        @DisplayName("當預約不存在時應該拋出例外")
        void shouldThrowExceptionWhenBookingNotFound() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratingService.createRating(testRatingDto, testUserId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("預約");
        }

        @Test
        @DisplayName("當預約未完成時應該拋出例外")
        void shouldThrowExceptionWhenBookingNotCompleted() {
            // given
            testBooking.setStatus(BookingStatus.CONFIRMED); // 未完成狀態
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when & then
            assertThatThrownBy(() -> ratingService.createRating(testRatingDto, testUserId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("當評價者不是預約的飼主時應該拋出例外")
        void shouldThrowExceptionWhenUnauthorizedUser() {
            // given
            UUID otherUserId = UUID.randomUUID();
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));

            // when & then
            assertThatThrownBy(() -> ratingService.createRating(testRatingDto, otherUserId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("當預約已有評價時應該拋出例外")
        void shouldThrowExceptionWhenRatingAlreadyExists() {
            // given
            given(bookingRepository.findById(testBookingId)).willReturn(Optional.of(testBooking));
            given(ratingRepository.existsByBookingId(testBookingId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> ratingService.createRating(testRatingDto, testUserId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("保母回覆評價測試")
    class ReplyToRatingTests {

        @Test
        @DisplayName("應該成功回覆評價")
        void shouldReplyToRating() {
            // given
            String reply = "感謝您的評價！";
            given(ratingRepository.findById(testRatingId)).willReturn(Optional.of(testRating));

            SitterRating repliedRating = new SitterRating();
            repliedRating.setId(testRatingId);
            repliedRating.setBooking(testBooking);
            repliedRating.setSitter(testSitter);
            repliedRating.setUser(testUser);
            repliedRating.setOverallRating(5);
            repliedRating.setProfessionalismRating(5);
            repliedRating.setCommunicationRating(4);
            repliedRating.setPunctualityRating(5);
            repliedRating.setComment("非常好的保母！");
            repliedRating.setSitterReply(reply);
            repliedRating.setIsAnonymous(false);

            given(ratingRepository.save(any(SitterRating.class))).willReturn(repliedRating);

            // when
            SitterRatingDto result = ratingService.replyToRating(testRatingId, reply, testSitterId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sitterReply()).isEqualTo(reply);
            verify(ratingRepository).save(any(SitterRating.class));
        }

        @Test
        @DisplayName("當評價不存在時應該拋出例外")
        void shouldThrowExceptionWhenRatingNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(ratingRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> ratingService.replyToRating(unknownId, "回覆", testSitterId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("當不是被評價的保母時應該拋出例外")
        void shouldThrowExceptionWhenUnauthorizedSitter() {
            // given
            UUID otherSitterId = UUID.randomUUID();
            given(ratingRepository.findById(testRatingId)).willReturn(Optional.of(testRating));

            // when & then
            assertThatThrownBy(() -> ratingService.replyToRating(testRatingId, "回覆", otherSitterId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("只有被評價的保母可以回覆");
        }
    }

    @Nested
    @DisplayName("查詢評價測試")
    class GetRatingTests {

        @Test
        @DisplayName("應該取得保母的評價列表")
        void shouldGetSitterRatings() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<SitterRating> ratingsPage = new PageImpl<>(List.of(testRating));
            given(ratingRepository.findBySitter_IdOrderByCreatedAtDesc(testSitterId, pageable))
                    .willReturn(ratingsPage);

            // when
            Page<SitterRatingDto> result = ratingService.getSitterRatings(testSitterId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).sitterId()).isEqualTo(testSitterId);
        }

        @Test
        @DisplayName("應該取得保母的評價統計")
        void shouldGetSitterRatingStats() {
            // given
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));
            given(ratingRepository.calculateDetailedAverages(testSitterId))
                    .willReturn(new Object[]{4.8, 4.9, 4.7, 4.8});
            given(ratingRepository.countRatingsByStars(testSitterId))
                    .willReturn(List.of(
                            new Object[]{5, 8L},
                            new Object[]{4, 2L}
                    ));
            given(ratingRepository.countBySitter_Id(testSitterId)).willReturn(10L);

            // when
            SitterRatingStatsDto result = ratingService.getSitterRatingStats(testSitterId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.sitterId()).isEqualTo(testSitterId);
            assertThat(result.sitterName()).isEqualTo("王保母");
            assertThat(result.totalRatings()).isEqualTo(10);
            assertThat(result.fiveStarCount()).isEqualTo(8);
            assertThat(result.fourStarCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("應該根據 ID 取得評價")
        void shouldGetRatingById() {
            // given
            given(ratingRepository.findById(testRatingId)).willReturn(Optional.of(testRating));

            // when
            SitterRatingDto result = ratingService.getRatingById(testRatingId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testRatingId);
            assertThat(result.overallRating()).isEqualTo(5);
        }

        @Test
        @DisplayName("應該根據預約 ID 取得評價")
        void shouldGetRatingByBooking() {
            // given
            given(ratingRepository.findByBookingId(testBookingId)).willReturn(Optional.of(testRating));

            // when
            SitterRatingDto result = ratingService.getRatingByBooking(testBookingId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.bookingId()).isEqualTo(testBookingId);
        }

        @Test
        @DisplayName("應該取得使用者給出的所有評價")
        void shouldGetUserRatings() {
            // given
            given(ratingRepository.findByUserIdOrderByCreatedAtDesc(testUserId))
                    .willReturn(List.of(testRating));

            // when
            List<SitterRatingDto> result = ratingService.getUserRatings(testUserId);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).userId()).isEqualTo(testUserId);
        }
    }

    @Nested
    @DisplayName("匿名評價測試")
    class AnonymousRatingTests {

        @Test
        @DisplayName("應該正確處理匿名評價")
        void shouldHandleAnonymousRating() {
            // given
            testRating.setIsAnonymous(true);
            given(ratingRepository.findById(testRatingId)).willReturn(Optional.of(testRating));

            // when
            SitterRatingDto result = ratingService.getRatingById(testRatingId);

            // then
            assertThat(result.username()).isEqualTo("匿名用戶");
        }
    }
}
