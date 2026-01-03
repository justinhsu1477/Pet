package com.pet.service;

import com.pet.domain.Sitter;
import com.pet.dto.SitterDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.SitterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SitterService {

    private final SitterRepository sitterRepository;

    public SitterService(SitterRepository sitterRepository) {
        this.sitterRepository = sitterRepository;
    }

    public List<SitterDto> getAllSitters() {
        return sitterRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SitterDto getSitterById(UUID id) {
        Sitter sitter = sitterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", id));
        return convertToDto(sitter);
    }

    public SitterDto createSitter(SitterDto sitterDto) {
        Sitter sitter = convertToEntity(sitterDto);
        Sitter savedSitter = sitterRepository.save(sitter);
        return convertToDto(savedSitter);
    }

    public SitterDto updateSitter(UUID id, SitterDto sitterDto) {
        if (!sitterRepository.existsById(id)) {
            throw new ResourceNotFoundException("保母", "id", id);
        }
        Sitter sitter = convertToEntity(sitterDto);
        sitter.setId(id);
        Sitter updatedSitter = sitterRepository.save(sitter);
        return convertToDto(updatedSitter);
    }

    public void deleteSitter(UUID id) {
        if (!sitterRepository.existsById(id)) {
            throw new ResourceNotFoundException("保母", "id", id);
        }
        sitterRepository.deleteById(id);
    }

    // 內部方法:用於其他 Service 取得 Sitter Entity
    public Sitter getSitterEntityById(UUID id) {
        return sitterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", id));
    }

    private SitterDto convertToDto(Sitter sitter) {
        return new SitterDto(
                sitter.getId(),
                sitter.getName(),
                sitter.getPhone(),
                sitter.getEmail(),
                sitter.getExperience());
    }

    private Sitter convertToEntity(SitterDto dto) {
        Sitter sitter = new Sitter();
        sitter.setName(dto.name());
        sitter.setPhone(dto.phone());
        sitter.setEmail(dto.email());
        sitter.setExperience(dto.experience());
        return sitter;
    }
}
