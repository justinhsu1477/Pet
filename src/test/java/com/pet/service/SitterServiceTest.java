package com.pet.service;

import com.pet.domain.Sitter;
import com.pet.dto.AvailableSitterDto;
import com.pet.dto.SitterDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
import com.pet.repository.SitterAvailabilityRepository;
import com.pet.repository.SitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
@DisplayName("SitterService 測試")
class SitterServiceTest {

    @Mock
    private SitterRepository sitterRepository;

    @Mock
    private SitterAvailabilityRepository availabilityRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private SitterService sitterService;

    private UUID testSitterId;
    private Sitter testSitter;
    private SitterDto testSitterDto;

    @BeforeEach
    void setUp() {
        testSitterId = UUID.randomUUID();

        testSitter = new Sitter();
        testSitter.setId(testSitterId);
        testSitter.setName("王保母");
        testSitter.setExperience("5年寵物照護經驗");
        testSitter.setHourlyRate(200.0);
        testSitter.setAverageRating(4.5);
        testSitter.setRatingCount(10);
        testSitter.setCompletedBookings(15);

        testSitterDto = new SitterDto(
                testSitterId,
                "王保母",
                "5年寵物照護經驗"
        );
    }

    @Nested
    @DisplayName("CRUD 操作測試")
    class CrudTests {

        @Test
        @DisplayName("應該取得所有保母")
        void shouldGetAllSitters() {
            // given
            given(sitterRepository.findAllWithUser()).willReturn(List.of(testSitter));

            // when
            List<SitterDto> result = sitterService.getAllSitters();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("王保母");
        }

        @Test
        @DisplayName("應該根據 ID 取得保母")
        void shouldGetSitterById() {
            // given
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));

            // when
            SitterDto result = sitterService.getSitterById(testSitterId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(testSitterId);
            assertThat(result.name()).isEqualTo("王保母");
        }

        @Test
        @DisplayName("當保母不存在時應該拋出例外")
        void shouldThrowExceptionWhenSitterNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(sitterRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sitterService.getSitterById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("保母");
        }

        @Test
        @DisplayName("應該成功建立保母")
        void shouldCreateSitter() {
            // given
            given(sitterRepository.save(any(Sitter.class))).willReturn(testSitter);

            // when
            SitterDto result = sitterService.createSitter(testSitterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("王保母");
            verify(sitterRepository).save(any(Sitter.class));
        }

        @Test
        @DisplayName("應該成功更新保母")
        void shouldUpdateSitter() {
            // given
            given(sitterRepository.existsById(testSitterId)).willReturn(true);
            given(sitterRepository.save(any(Sitter.class))).willReturn(testSitter);

            // when
            SitterDto result = sitterService.updateSitter(testSitterId, testSitterDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("王保母");
            verify(sitterRepository).save(any(Sitter.class));
        }

        @Test
        @DisplayName("當更新不存在的保母時應該拋出例外")
        void shouldThrowExceptionWhenUpdatingNonExistentSitter() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(sitterRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sitterService.updateSitter(unknownId, testSitterDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("應該成功刪除保母")
        void shouldDeleteSitter() {
            // given
            given(sitterRepository.existsById(testSitterId)).willReturn(true);

            // when
            sitterService.deleteSitter(testSitterId);

            // then
            verify(sitterRepository).deleteById(testSitterId);
        }

        @Test
        @DisplayName("當刪除不存在的保母時應該拋出例外")
        void shouldThrowExceptionWhenDeletingNonExistentSitter() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(sitterRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> sitterService.deleteSitter(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("可用保母查詢測試")
    class AvailableSittersTests {

        @Test
        @DisplayName("應該取得指定日期的可用保母（不檢查時段）")
        void shouldGetAvailableSittersWithoutTimeRange() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            given(availabilityRepository.findAvailableSittersByDayOfWeek(dayOfWeek, true))
                    .willReturn(List.of(testSitter));

            // when
            List<AvailableSitterDto> result = sitterService.getAvailableSitters(date, null, null);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(testSitterId);
            assertThat(result.get(0).name()).isEqualTo("王保母");
            assertThat(result.get(0).hourlyRate()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("應該取得指定日期和時段的可用保母（排除有衝突的）")
        void shouldGetAvailableSittersWithTimeRange() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            LocalDateTime startTime = date.atTime(10, 0);
            LocalDateTime endTime = date.atTime(12, 0);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            Sitter availableSitter = new Sitter();
            availableSitter.setId(testSitterId);
            availableSitter.setName("王保母");
            availableSitter.setHourlyRate(200.0);
            availableSitter.setAverageRating(4.5);
            availableSitter.setRatingCount(10);
            availableSitter.setCompletedBookings(15);

            given(availabilityRepository.findAvailableSittersByDayOfWeek(dayOfWeek, true))
                    .willReturn(List.of(availableSitter));
            given(bookingRepository.countConflictingBookings(testSitterId, startTime, endTime))
                    .willReturn(0L); // 沒有衝突

            // when
            List<AvailableSitterDto> result = sitterService.getAvailableSitters(date, startTime, endTime);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo(testSitterId);
        }

        @Test
        @DisplayName("應該排除有時段衝突的保母")
        void shouldFilterOutSittersWithConflictingBookings() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            LocalDateTime startTime = date.atTime(10, 0);
            LocalDateTime endTime = date.atTime(12, 0);
            DayOfWeek dayOfWeek = date.getDayOfWeek();

            given(availabilityRepository.findAvailableSittersByDayOfWeek(dayOfWeek, true))
                    .willReturn(List.of(testSitter));
            given(bookingRepository.countConflictingBookings(testSitterId, startTime, endTime))
                    .willReturn(1L); // 有衝突

            // when
            List<AvailableSitterDto> result = sitterService.getAvailableSitters(date, startTime, endTime);

            // then
            assertThat(result).isEmpty(); // 應該被過濾掉
        }

        @Test
        @DisplayName("應該取得所有保母含評分資訊")
        void shouldGetAllSittersWithRating() {
            // given
            given(sitterRepository.findAllWithUser()).willReturn(List.of(testSitter));

            // when
            List<AvailableSitterDto> result = sitterService.getAllSittersWithRating();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).averageRating()).isEqualTo(4.5);
            assertThat(result.get(0).ratingCount()).isEqualTo(10);
            assertThat(result.get(0).completedBookings()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("內部方法測試")
    class InternalMethodTests {

        @Test
        @DisplayName("應該取得保母實體")
        void shouldGetSitterEntity() {
            // given
            given(sitterRepository.findById(testSitterId)).willReturn(Optional.of(testSitter));

            // when
            Sitter result = sitterService.getSitterEntityById(testSitterId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testSitterId);
            assertThat(result.getName()).isEqualTo("王保母");
        }

        @Test
        @DisplayName("當取得不存在的保母實體時應該拋出例外")
        void shouldThrowExceptionWhenGettingNonExistentEntity() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(sitterRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sitterService.getSitterEntityById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
