package com.pet.service;

import com.pet.domain.Pet;
import com.pet.domain.Sitter;
import com.pet.domain.SitterRecord;
import com.pet.dto.CreateSitterRecordDto;
import com.pet.dto.SitterRecordDto;
import com.pet.dto.UpdateSitterRecordDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.SitterRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SitterRecordService {

    private final SitterRecordRepository sitterRecordRepository;
    private final PetService petService;
    private final SitterService sitterService;

    public SitterRecordService(SitterRecordRepository sitterRecordRepository,
            PetService petService,
            SitterService sitterService) {
        this.sitterRecordRepository = sitterRecordRepository;
        this.petService = petService;
        this.sitterService = sitterService;
    }

    public List<SitterRecordDto> getAllRecords() {
        return sitterRecordRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SitterRecordDto getRecordById(UUID id) {
        SitterRecord record = sitterRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("保母記錄", "id", id));
        return convertToDto(record);
    }

    public List<SitterRecordDto> getRecordsByPetId(UUID petId) {
        // 驗證寵物是否存在
        petService.getPetEntityById(petId);
        return sitterRecordRepository.findByPetId(petId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SitterRecordDto> getRecordsBySitterId(UUID sitterId) {
        // 驗證保母是否存在
        sitterService.getSitterEntityById(sitterId);
        return sitterRecordRepository.findBySitterId(sitterId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SitterRecordDto createRecord(CreateSitterRecordDto createDto) {
        // 取得並驗證 Pet 和 Sitter 是否存在
        Pet pet = petService.getPetEntityById(createDto.petId());
        Sitter sitter = sitterService.getSitterEntityById(createDto.sitterId());

        SitterRecord record = new SitterRecord();
        record.setPet(pet);
        record.setSitter(sitter);
        record.setRecordTime(LocalDateTime.now()); // 自動設定記錄時間
        record.setActivity(createDto.activity());
        record.setFed(createDto.fed());
        record.setWalked(createDto.walked());
        record.setMoodStatus(createDto.moodStatus());
        record.setNotes(createDto.notes());
        record.setPhotos(createDto.photos());

        SitterRecord savedRecord = sitterRecordRepository.save(record);
        return convertToDto(savedRecord);
    }

    public SitterRecordDto updateRecord(UUID id, UpdateSitterRecordDto updateDto) {
        SitterRecord record = sitterRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("保母記錄", "id", id));

        // 只更新非 null 的欄位
        if (updateDto.activity() != null) {
            record.setActivity(updateDto.activity());
        }
        if (updateDto.fed() != null) {
            record.setFed(updateDto.fed());
        }
        if (updateDto.walked() != null) {
            record.setWalked(updateDto.walked());
        }
        if (updateDto.moodStatus() != null) {
            record.setMoodStatus(updateDto.moodStatus());
        }
        if (updateDto.notes() != null) {
            record.setNotes(updateDto.notes());
        }
        if (updateDto.photos() != null) {
            record.setPhotos(updateDto.photos());
        }

        SitterRecord updatedRecord = sitterRecordRepository.save(record);
        return convertToDto(updatedRecord);
    }

    public void deleteRecord(UUID id) {
        if (!sitterRecordRepository.existsById(id)) {
            throw new ResourceNotFoundException("保母記錄", "id", id);
        }
        sitterRecordRepository.deleteById(id);
    }

    private SitterRecordDto convertToDto(SitterRecord record) {
        return new SitterRecordDto(
                record.getId(),
                petService.getPetById(record.getPet().getId()),
                sitterService.getSitterById(record.getSitter().getId()),
                record.getRecordTime(),
                record.getActivity(),
                record.getFed(),
                record.getWalked(),
                record.getMoodStatus(),
                record.getNotes(),
                record.getPhotos());
    }
}
