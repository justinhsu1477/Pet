package com.pet.service;

import com.pet.domain.Cat;
import com.pet.domain.Pet;
import com.pet.dto.CatDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.CatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatService 測試")
class CatServiceTest {

    @Mock
    private CatRepository catRepository;

    @InjectMocks
    private CatService catService;

    private Cat testCat;
    private CatDto testCatDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testCat = new Cat();
        testCat.setId(testId);
        testCat.setName("小花");
        testCat.setAge(3);
        testCat.setBreed("米克斯");
        testCat.setGender(Pet.Gender.FEMALE);
        testCat.setIsIndoor(true);
        testCat.setLitterBoxType(Cat.LitterBoxType.COVERED);
        testCat.setScratchingHabit(Cat.ScratchingHabit.MODERATE);

        testCatDto = new CatDto(
                testId, "小花", 3, "米克斯", Pet.Gender.FEMALE,
                null, null, null,
                true, Cat.LitterBoxType.COVERED, Cat.ScratchingHabit.MODERATE
        );
    }

    @Nested
    @DisplayName("Create 操作")
    class CreateTests {

        @Test
        @DisplayName("應該成功建立貓咪")
        void shouldCreateCat() {
            // given
            given(catRepository.save(any(Cat.class))).willReturn(testCat);

            // when
            CatDto result = catService.create(testCatDto);

            // then
            assertThat(result.name()).isEqualTo("小花");
            assertThat(result.breed()).isEqualTo("米克斯");
            verify(catRepository).save(any(Cat.class));
        }
    }

    @Nested
    @DisplayName("Read 操作")
    class ReadTests {

        @Test
        @DisplayName("應該取得所有貓咪")
        void shouldGetAllCats() {
            // given
            given(catRepository.findAll()).willReturn(List.of(testCat));

            // when
            List<CatDto> result = catService.getAll();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("小花");
        }

        @Test
        @DisplayName("應該根據 ID 取得貓咪")
        void shouldGetCatById() {
            // given
            given(catRepository.findById(testId)).willReturn(Optional.of(testCat));

            // when
            CatDto result = catService.getById(testId);

            // then
            assertThat(result.id()).isEqualTo(testId);
            assertThat(result.name()).isEqualTo("小花");
        }

        @Test
        @DisplayName("當貓咪不存在時應該拋出例外")
        void shouldThrowExceptionWhenCatNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(catRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> catService.getById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update 操作")
    class UpdateTests {

        @Test
        @DisplayName("應該成功更新貓咪")
        void shouldUpdateCat() {
            // given
            given(catRepository.findById(testId)).willReturn(Optional.of(testCat));
            given(catRepository.save(any(Cat.class))).willReturn(testCat);

            // when
            CatDto result = catService.update(testId, testCatDto);

            // then
            assertThat(result.name()).isEqualTo("小花");
            verify(catRepository).save(any(Cat.class));
        }

        @Test
        @DisplayName("當更新不存在的貓咪時應該拋出例外")
        void shouldThrowExceptionWhenUpdatingNonExistentCat() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(catRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> catService.update(unknownId, testCatDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete 操作")
    class DeleteTests {

        @Test
        @DisplayName("應該成功刪除貓咪")
        void shouldDeleteCat() {
            // given
            given(catRepository.existsById(testId)).willReturn(true);

            // when
            catService.delete(testId);

            // then
            verify(catRepository).deleteById(testId);
        }

        @Test
        @DisplayName("當刪除不存在的貓咪時應該拋出例外")
        void shouldThrowExceptionWhenDeletingNonExistentCat() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(catRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> catService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}