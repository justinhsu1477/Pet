package com.pet.service;

import com.pet.domain.Dog;
import com.pet.domain.Pet;
import com.pet.dto.DogDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.DogRepository;
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
@DisplayName("DogService 測試")
class DogServiceTest {

    @Mock
    private DogRepository dogRepository;

    @InjectMocks
    private DogService dogService;

    private Dog testDog;
    private DogDto testDogDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testDog = new Dog();
        testDog.setId(testId);
        testDog.setName("旺財");
        testDog.setAge(5);
        testDog.setBreed("柴犬");
        testDog.setGender(Pet.Gender.MALE);
        testDog.setOwnerName("李大華");
        testDog.setOwnerPhone("0987654321");
        testDog.setSize(Dog.Size.MEDIUM);
        testDog.setIsWalkRequired(true);
        testDog.setWalkFrequencyPerDay(2);
        testDog.setTrainingLevel(Dog.TrainingLevel.BASIC);
        testDog.setIsFriendlyWithDogs(true);
        testDog.setIsFriendlyWithChildren(true);

        testDogDto = new DogDto(
                testId, "旺財", 5, "柴犬", Pet.Gender.MALE,
                "李大華", "0987654321", null, null, null,
                Dog.Size.MEDIUM, true, 2, Dog.TrainingLevel.BASIC,
                true, null, true
        );
    }

    @Nested
    @DisplayName("Create 操作")
    class CreateTests {

        @Test
        @DisplayName("應該成功建立狗狗")
        void shouldCreateDog() {
            // given
            given(dogRepository.save(any(Dog.class))).willReturn(testDog);

            // when
            DogDto result = dogService.create(testDogDto);

            // then
            assertThat(result.name()).isEqualTo("旺財");
            assertThat(result.breed()).isEqualTo("柴犬");
            verify(dogRepository).save(any(Dog.class));
        }
    }

    @Nested
    @DisplayName("Read 操作")
    class ReadTests {

        @Test
        @DisplayName("應該取得所有狗狗")
        void shouldGetAllDogs() {
            // given
            given(dogRepository.findAll()).willReturn(List.of(testDog));

            // when
            List<DogDto> result = dogService.getAll();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("旺財");
        }

        @Test
        @DisplayName("應該根據 ID 取得狗狗")
        void shouldGetDogById() {
            // given
            given(dogRepository.findById(testId)).willReturn(Optional.of(testDog));

            // when
            DogDto result = dogService.getById(testId);

            // then
            assertThat(result.id()).isEqualTo(testId);
            assertThat(result.name()).isEqualTo("旺財");
        }

        @Test
        @DisplayName("當狗狗不存在時應該拋出例外")
        void shouldThrowExceptionWhenDogNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(dogRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> dogService.getById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Update 操作")
    class UpdateTests {

        @Test
        @DisplayName("應該成功更新狗狗")
        void shouldUpdateDog() {
            // given
            given(dogRepository.existsById(testId)).willReturn(true);
            given(dogRepository.save(any(Dog.class))).willReturn(testDog);

            // when
            DogDto result = dogService.update(testId, testDogDto);

            // then
            assertThat(result.name()).isEqualTo("旺財");
            verify(dogRepository).save(any(Dog.class));
        }

        @Test
        @DisplayName("當更新不存在的狗狗時應該拋出例外")
        void shouldThrowExceptionWhenUpdatingNonExistentDog() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(dogRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> dogService.update(unknownId, testDogDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Delete 操作")
    class DeleteTests {

        @Test
        @DisplayName("應該成功刪除狗狗")
        void shouldDeleteDog() {
            // given
            given(dogRepository.existsById(testId)).willReturn(true);

            // when
            dogService.delete(testId);

            // then
            verify(dogRepository).deleteById(testId);
        }

        @Test
        @DisplayName("當刪除不存在的狗狗時應該拋出例外")
        void shouldThrowExceptionWhenDeletingNonExistentDog() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(dogRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> dogService.delete(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
