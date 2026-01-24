package com.pet.service;

import com.pet.domain.Cat;
import com.pet.domain.Dog;
import com.pet.domain.Pet;
import com.pet.dto.PetDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 寵物服務 - 提供多態查詢所有寵物（貓和狗）
 * 新增/更新/刪除請使用 CatService 或 DogService
 */
@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    /**
     * 取得所有寵物（包含貓和狗）
     */
    public List<PetDto> getAllPets() {
        return petRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 取得寵物
     */
    public PetDto getPetById(UUID id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", id));
        return convertToDto(pet);
    }

    /**
     * 刪除寵物（通用刪除）
     */
    public void deletePet(UUID id) {
        if (!petRepository.existsById(id)) {
            throw new ResourceNotFoundException("寵物", "id", id);
        }
        petRepository.deleteById(id);
    }

    /**
     * 內部方法: 用於其他 Service 取得 Pet Entity（如 SitterRecordService）
     */
    public Pet getPetEntityById(UUID id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", id));
    }

    /**
     * 根據寵物類型取得所有寵物
     */
    public List<PetDto> getPetsByType(String petType) {
        return petRepository.findAll().stream()
                .filter(pet -> {
                    if ("CAT".equalsIgnoreCase(petType)) {
                        return pet instanceof Cat;
                    } else if ("DOG".equalsIgnoreCase(petType)) {
                        return pet instanceof Dog;
                    }
                    return false;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得使用者的所有寵物
     */
    @Transactional(readOnly = true)
    public List<PetDto> getPetsByUserId(UUID userId) {
        return petRepository.findByOwnerIdOrderByNameAsc(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private PetDto convertToDto(Pet pet) {
        String petType = pet instanceof Cat ? "CAT" : "DOG";
        return new PetDto(
                pet.getId(),
                pet.getName(),
                pet.getAge(),
                pet.getBreed(),
                pet.getGender(),
                pet.getSpecialNeeds(),
                pet.getIsNeutered(),
                pet.getVaccineStatus(),
                petType,
                pet.getPetTypeName()
        );
    }
}
