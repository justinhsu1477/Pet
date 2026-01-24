package com.pet.service;

import com.pet.domain.Cat;
import com.pet.dto.CatDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.CatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CatService implements PetServiceInterface<CatDto> {

    private final CatRepository catRepository;

    public CatService(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    @Override
    public List<CatDto> getAll() {
        return catRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CatDto getById(UUID id) {
        Cat cat = catRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("貓咪", "id", id));
        return convertToDto(cat);
    }

    @Override
    public CatDto create(CatDto dto) {
        Cat cat = convertToEntity(dto);
        Cat savedCat = catRepository.save(cat);
        return convertToDto(savedCat);
    }

    @Override
    public CatDto update(UUID id, CatDto dto) {
        if (!catRepository.existsById(id)) {
            throw new ResourceNotFoundException("貓咪", "id", id);
        }
        Cat cat = convertToEntity(dto);
        cat.setId(id);
        Cat updatedCat = catRepository.save(cat);
        return convertToDto(updatedCat);
    }

    @Override
    public void delete(UUID id) {
        if (!catRepository.existsById(id)) {
            throw new ResourceNotFoundException("貓咪", "id", id);
        }
        catRepository.deleteById(id);
    }

    // ========== 貓特有的方法 ==========

    /**
     * 取得所有室內貓
     */
    public List<CatDto> getIndoorCats() {
        return catRepository.findByIsIndoorTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得所有室外貓
     */
    public List<CatDto> getOutdoorCats() {
        return catRepository.findByIsIndoorFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據貓砂盆類型查詢
     */
    public List<CatDto> getByLitterBoxType(Cat.LitterBoxType litterBoxType) {
        return catRepository.findByLitterBoxType(litterBoxType).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根據抓挠習慣查詢
     */
    public List<CatDto> getByScratchingHabit(Cat.ScratchingHabit scratchingHabit) {
        return catRepository.findByScratchingHabit(scratchingHabit).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ========== 轉換方法 ==========

    private CatDto convertToDto(Cat cat) {
        return new CatDto(
                cat.getId(),
                cat.getName(),
                cat.getAge(),
                cat.getBreed(),
                cat.getGender(),
                cat.getSpecialNeeds(),
                cat.getIsNeutered(),
                cat.getVaccineStatus(),
                cat.getIsIndoor(),
                cat.getLitterBoxType(),
                cat.getScratchingHabit()
        );
    }

    private Cat convertToEntity(CatDto dto) {
        Cat cat = new Cat();
        cat.setName(dto.name());
        cat.setAge(dto.age());
        cat.setBreed(dto.breed());
        cat.setGender(dto.gender());
        cat.setSpecialNeeds(dto.specialNeeds());
        cat.setIsNeutered(dto.isNeutered());
        cat.setVaccineStatus(dto.vaccineStatus());
        cat.setIsIndoor(dto.isIndoor());
        cat.setLitterBoxType(dto.litterBoxType());
        cat.setScratchingHabit(dto.scratchingHabit());
        return cat;
    }
}
