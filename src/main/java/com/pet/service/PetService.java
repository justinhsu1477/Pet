package com.pet.service;

import com.pet.domain.Pet;
import com.pet.dto.PetDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public List<PetDto> getAllPets() {
        return petRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public PetDto getPetById(UUID id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", id));
        return convertToDto(pet);
    }

    public PetDto createPet(PetDto petDto) {
        Pet pet = convertToEntity(petDto);
        Pet savedPet = petRepository.save(pet);
        return convertToDto(savedPet);
    }

    public PetDto updatePet(UUID id, PetDto petDto) {
        if (!petRepository.existsById(id)) {
            throw new ResourceNotFoundException("寵物", "id", id);
        }
        Pet pet = convertToEntity(petDto);
        pet.setId(id);
        Pet updatedPet = petRepository.save(pet);
        return convertToDto(updatedPet);
    }

    public void deletePet(UUID id) {
        if (!petRepository.existsById(id)) {
            throw new ResourceNotFoundException("寵物", "id", id);
        }
        petRepository.deleteById(id);
    }

    // 內部方法:用於其他 Service 取得 Pet Entity
    public Pet getPetEntityById(UUID id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("寵物", "id", id));
    }

    private PetDto convertToDto(Pet pet) {
        return new PetDto(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getAge(),
                pet.getBreed(),
                pet.getOwnerName(),
                pet.getOwnerPhone(),
                pet.getSpecialNeeds());
    }

    private Pet convertToEntity(PetDto dto) {
        Pet pet = new Pet();
        pet.setName(dto.name());
        pet.setType(dto.type());
        pet.setAge(dto.age());
        pet.setBreed(dto.breed());
        pet.setOwnerName(dto.ownerName());
        pet.setOwnerPhone(dto.ownerPhone());
        pet.setSpecialNeeds(dto.specialNeeds());
        return pet;
    }
}
