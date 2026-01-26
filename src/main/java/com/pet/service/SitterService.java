package com.pet.service;

import com.pet.domain.Sitter;
import com.pet.dto.AvailableSitterDto;
import com.pet.dto.SitterDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.SitterAvailabilityRepository;
import com.pet.repository.SitterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class SitterService {

    private final SitterRepository sitterRepository;
    private final SitterAvailabilityRepository availabilityRepository;

    public SitterService(SitterRepository sitterRepository,
                         SitterAvailabilityRepository availabilityRepository) {
        this.sitterRepository = sitterRepository;
        this.availabilityRepository = availabilityRepository;
    }

    public List<SitterDto> getAllSitters() {
        return sitterRepository.findAllWithUser().stream()
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

    /**
     * 取得指定日期可用的保母列表
     */
    @Transactional(readOnly = true)
    public List<AvailableSitterDto> getAvailableSitters(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Sitter> sitters = availabilityRepository.findAvailableSittersByDayOfWeek(dayOfWeek);
        return sitters.stream()
                .map(this::convertToAvailableDto)
                .collect(Collectors.toList());
    }

    /**
     * 取得所有保母（含評分資訊，使用 FETCH JOIN 避免 N+1 問題）
     */
    @Transactional(readOnly = true)
    public List<AvailableSitterDto> getAllSittersWithRating() {
        return sitterRepository.findAllWithUser().stream()
                .map(this::convertToAvailableDto)
                .collect(Collectors.toList());
    }

    private AvailableSitterDto convertToAvailableDto(Sitter sitter) {
        return new AvailableSitterDto(
                sitter.getId(),
                sitter.getName(),
                sitter.getExperience(),
                sitter.getAverageRating(),
                sitter.getRatingCount(),
                sitter.getCompletedBookings()
        );
    }

    private SitterDto convertToDto(Sitter sitter) {
        return new SitterDto(
                sitter.getId(),
                sitter.getName(),
                sitter.getExperience());
    }

    private Sitter convertToEntity(SitterDto dto) {
        Sitter sitter = new Sitter();
        sitter.setName(dto.name());
        sitter.setExperience(dto.experience());
        return sitter;
    }
}
