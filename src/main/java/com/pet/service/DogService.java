package com.pet.service;

import com.pet.domain.Dog;
import com.pet.domain.Users;
import com.pet.dto.DogDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.DogRepository;
import com.pet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class DogService implements PetServiceInterface<DogDto> {

    private final DogRepository dogRepository;
    private final UserRepository userRepository;

    public DogService(DogRepository dogRepository, UserRepository userRepository) {
        this.dogRepository = dogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<DogDto> getAll() {
        return dogRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DogDto getById(UUID id) {
        Dog dog = dogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("狗狗", "id", id));
        return convertToDto(dog);
    }

    @Override
    public DogDto create(DogDto dto) {
        Dog dog = convertToEntity(dto);
        Dog savedDog = dogRepository.save(dog);
        return convertToDto(savedDog);
    }

    /**
     * 建立狗狗（指定飼主）
     */
    public DogDto create(DogDto dto, UUID userId) {
        Users owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", "id", userId));

        Dog dog = convertToEntity(dto);
        dog.setOwner(owner);
        Dog savedDog = dogRepository.save(dog);
        return convertToDto(savedDog);
    }

    @Override
    public DogDto update(UUID id, DogDto dto) {
        // 先從數據庫加載現有的 Dog（包含 owner 關聯）
        Dog existingDog = dogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("狗狗", "id", id));

        // 更新屬性（保留 owner）
        existingDog.setName(dto.name());
        existingDog.setAge(dto.age());
        existingDog.setBreed(dto.breed());
        existingDog.setGender(dto.gender());
        existingDog.setSpecialNeeds(dto.specialNeeds());
        existingDog.setIsNeutered(dto.isNeutered());
        existingDog.setVaccineStatus(dto.vaccineStatus());
        existingDog.setSize(dto.size());
        existingDog.setIsWalkRequired(dto.isWalkRequired());
        existingDog.setWalkFrequencyPerDay(dto.walkFrequencyPerDay());
        existingDog.setTrainingLevel(dto.trainingLevel());
        existingDog.setIsFriendlyWithDogs(dto.isFriendlyWithDogs());
        existingDog.setIsFriendlyWithPeople(dto.isFriendlyWithPeople());
        existingDog.setIsFriendlyWithChildren(dto.isFriendlyWithChildren());

        Dog updatedDog = dogRepository.save(existingDog);
        return convertToDto(updatedDog);
    }

    @Override
    public void delete(UUID id) {
        if (!dogRepository.existsById(id)) {
            throw new ResourceNotFoundException("狗狗", "id", id);
        }
        dogRepository.deleteById(id);
    }

    // ========== 狗特有的方法 ==========

    /**
     * 根據體型查詢
     */
    public List<DogDto> getBySize(Dog.Size size) {
        return dogRepository.findBySize(size).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得需要遛狗的狗狗
     */
    public List<DogDto> getDogsNeedingWalk() {
        return dogRepository.findByIsWalkRequiredTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據訓練程度查詢
     */
    public List<DogDto> getByTrainingLevel(Dog.TrainingLevel trainingLevel) {
        return dogRepository.findByTrainingLevel(trainingLevel).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得對其他狗友善的狗狗
     */
    public List<DogDto> getDogFriendlyDogs() {
        return dogRepository.findByIsFriendlyWithDogsTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得對小孩友善的狗狗
     */
    public List<DogDto> getChildFriendlyDogs() {
        return dogRepository.findByIsFriendlyWithChildrenTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== 轉換方法 ==========

    private DogDto convertToDto(Dog dog) {
        return new DogDto(
                dog.getId(),
                dog.getName(),
                dog.getAge(),
                dog.getBreed(),
                dog.getGender(),
                dog.getSpecialNeeds(),
                dog.getIsNeutered(),
                dog.getVaccineStatus(),
                dog.getSize(),
                dog.getIsWalkRequired(),
                dog.getWalkFrequencyPerDay(),
                dog.getTrainingLevel(),
                dog.getIsFriendlyWithDogs(),
                dog.getIsFriendlyWithPeople(),
                dog.getIsFriendlyWithChildren()
        );
    }

    private Dog convertToEntity(DogDto dto) {
        Dog dog = new Dog();
        dog.setName(dto.name());
        dog.setAge(dto.age());
        dog.setBreed(dto.breed());
        dog.setGender(dto.gender());
        dog.setSpecialNeeds(dto.specialNeeds());
        dog.setIsNeutered(dto.isNeutered());
        dog.setVaccineStatus(dto.vaccineStatus());
        dog.setSize(dto.size());
        dog.setIsWalkRequired(dto.isWalkRequired());
        dog.setWalkFrequencyPerDay(dto.walkFrequencyPerDay());
        dog.setTrainingLevel(dto.trainingLevel());
        dog.setIsFriendlyWithDogs(dto.isFriendlyWithDogs());
        dog.setIsFriendlyWithPeople(dto.isFriendlyWithPeople());
        dog.setIsFriendlyWithChildren(dto.isFriendlyWithChildren());
        return dog;
    }
}
