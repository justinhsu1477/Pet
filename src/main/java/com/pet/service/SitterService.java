package com.pet.service;

import com.pet.domain.Sitter;
import com.pet.dto.AvailableSitterDto;
import com.pet.dto.SitterDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
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
    private final BookingRepository bookingRepository;

    public SitterService(SitterRepository sitterRepository,
                         SitterAvailabilityRepository availabilityRepository,
                         BookingRepository bookingRepository) {
        this.sitterRepository = sitterRepository;
        this.availabilityRepository = availabilityRepository;
        this.bookingRepository = bookingRepository;
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
     * @param date 預約日期
     * @param startTime 可選：開始時間，如果提供則會排除在該時段已有預約的保母
     * @param endTime 可選：結束時間，如果提供則會排除在該時段已有預約的保母
     */
    @Transactional(readOnly = true)
    public List<AvailableSitterDto> getAvailableSitters(LocalDate date,
                                                         java.time.LocalDateTime startTime,
                                                         java.time.LocalDateTime endTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Sitter> sitters = availabilityRepository.findAvailableSittersByDayOfWeek(dayOfWeek, true);

        // 如果提供了時間範圍，過濾掉在該時段已有預約的保母
        if (startTime != null && endTime != null) {
            sitters = sitters.stream()
                    .filter(sitter -> !hasConflictingBooking(sitter.getId(), startTime, endTime))
                    .toList();
        }

        return sitters.stream()
                .map(this::convertToAvailableDto)
                .collect(Collectors.toList());
    }

    /**
     * 檢查保母在指定時段是否有衝突的預約
     * 只檢查 PENDING 和 CONFIRMED 狀態的預約
     */
    private boolean hasConflictingBooking(UUID sitterId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
        return bookingRepository.countConflictingBookings(sitterId, startTime, endTime) > 0;
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
                sitter.getCompletedBookings(),
                sitter.getHourlyRate(),
                sitter.getExperienceLevel()
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
