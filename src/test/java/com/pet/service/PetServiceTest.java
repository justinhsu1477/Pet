package com.pet.service;

import com.pet.domain.Cat;
import com.pet.domain.Dog;
import com.pet.domain.Pet;
import com.pet.domain.Users;
import com.pet.dto.PetDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.PetRepository;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("PetService 測試")
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetService petService;

    private UUID testPetId;
    private UUID testOwnerId;
    private Cat testCat;
    private Dog testDog;
    private Users testOwner;

    @BeforeEach
    void setUp() {
        testPetId = UUID.randomUUID();
        testOwnerId = UUID.randomUUID();

        testOwner = new Users();
        testOwner.setId(testOwnerId);
        testOwner.setUsername("petowner");

        testCat = new Cat();
        testCat.setId(testPetId);
        testCat.setName("小花");
        testCat.setAge(3);
        testCat.setBreed("波斯貓");
        testCat.setOwner(testOwner);

        testDog = new Dog();
        testDog.setId(UUID.randomUUID());
        testDog.setName("小黑");
        testDog.setAge(5);
        testDog.setBreed("柴犬");
        testDog.setOwner(testOwner);
    }

    @Nested
    @DisplayName("取得所有寵物測試")
    class GetAllPetsTests {

        @Test
        @DisplayName("應該回傳所有寵物")
        void shouldReturnAllPets() {
            // given
            given(petRepository.findAllWithOwner()).willReturn(List.of(testCat, testDog));

            // when
            List<PetDto> result = petService.getAllPets();

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("當沒有寵物時應該回傳空列表")
        void shouldReturnEmptyListWhenNoPets() {
            // given
            given(petRepository.findAllWithOwner()).willReturn(List.of());

            // when
            List<PetDto> result = petService.getAllPets();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("取得單一寵物測試")
    class GetPetByIdTests {

        @Test
        @DisplayName("應該根據 ID 取得寵物")
        void shouldGetPetById() {
            // given
            given(petRepository.findById(testPetId)).willReturn(Optional.of(testCat));

            // when
            PetDto result = petService.getPetById(testPetId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.name()).isEqualTo("小花");
            assertThat(result.petType()).isEqualTo("CAT");
        }

        @Test
        @DisplayName("當寵物不存在時應該拋出例外")
        void shouldThrowExceptionWhenPetNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(petRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.getPetById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("寵物");
        }
    }

    @Nested
    @DisplayName("取得寵物 Entity 測試")
    class GetPetEntityByIdTests {

        @Test
        @DisplayName("應該根據 ID 取得寵物 Entity")
        void shouldGetPetEntityById() {
            // given
            given(petRepository.findById(testPetId)).willReturn(Optional.of(testCat));

            // when
            Pet result = petService.getPetEntityById(testPetId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("小花");
        }

        @Test
        @DisplayName("當寵物不存在時應該拋出例外")
        void shouldThrowExceptionWhenPetEntityNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(petRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.getPetEntityById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("刪除寵物測試")
    class DeletePetTests {

        @Test
        @DisplayName("應該成功刪除寵物")
        void shouldDeletePet() {
            // given
            given(petRepository.existsById(testPetId)).willReturn(true);

            // when
            petService.deletePet(testPetId);

            // then
            verify(petRepository).deleteById(testPetId);
        }

        @Test
        @DisplayName("當寵物不存在時應該拋出例外")
        void shouldThrowExceptionWhenPetNotFoundOnDelete() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(petRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> petService.deletePet(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("根據類型取得寵物測試")
    class GetPetsByTypeTests {

        @Test
        @DisplayName("應該只回傳貓")
        void shouldReturnOnlyCats() {
            // given
            given(petRepository.findAll()).willReturn(List.of(testCat, testDog));

            // when
            List<PetDto> result = petService.getPetsByType("CAT");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).petType()).isEqualTo("CAT");
        }

        @Test
        @DisplayName("應該只回傳狗")
        void shouldReturnOnlyDogs() {
            // given
            given(petRepository.findAll()).willReturn(List.of(testCat, testDog));

            // when
            List<PetDto> result = petService.getPetsByType("DOG");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).petType()).isEqualTo("DOG");
        }

        @Test
        @DisplayName("當類型無效時應該回傳空列表")
        void shouldReturnEmptyListForInvalidType() {
            // given
            given(petRepository.findAll()).willReturn(List.of(testCat, testDog));

            // when
            List<PetDto> result = petService.getPetsByType("BIRD");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("根據用戶取得寵物測試")
    class GetPetsByUserIdTests {

        @Test
        @DisplayName("應該回傳用戶的所有寵物")
        void shouldReturnUserPets() {
            // given
            given(petRepository.findByOwnerIdOrderByNameAsc(testOwnerId))
                    .willReturn(List.of(testCat, testDog));

            // when
            List<PetDto> result = petService.getPetsByUserId(testOwnerId);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("當用戶沒有寵物時應該回傳空列表")
        void shouldReturnEmptyListWhenUserHasNoPets() {
            // given
            UUID userId = UUID.randomUUID();
            given(petRepository.findByOwnerIdOrderByNameAsc(userId)).willReturn(List.of());

            // when
            List<PetDto> result = petService.getPetsByUserId(userId);

            // then
            assertThat(result).isEmpty();
        }
    }
}
