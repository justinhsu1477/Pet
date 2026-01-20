package com.pet.service;

import com.pet.domain.PetActivity;
import com.pet.dto.PetActivityDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.PetActivityRepository;
import com.pet.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PetActivityService {

    private final PetActivityRepository petActivityRepository;
    private final PetRepository petRepository;

    public PetActivityService(PetActivityRepository petActivityRepository, PetRepository petRepository) {
        this.petActivityRepository = petActivityRepository;
        this.petRepository = petRepository;
    }

    /**
     * 取得今天的活動紀錄
     */
    public PetActivityDto getTodayActivity(UUID petId) {
        validatePetExists(petId);
        LocalDate today = LocalDate.now();
        return petActivityRepository.findByPetIdAndActivityDate(petId, today)
                .map(this::convertToDto)
                .orElse(createEmptyActivity(petId, today));
    }

    /**
     * 記錄活動
     */
    public PetActivityDto recordActivity(UUID petId, PetActivityDto dto) {
        validatePetExists(petId);
        LocalDate activityDate = dto.activityDate() != null ? dto.activityDate() : LocalDate.now();

        PetActivity activity = petActivityRepository.findByPetIdAndActivityDate(petId, activityDate)
                .orElseGet(() -> {
                    PetActivity newActivity = new PetActivity();
                    newActivity.setPetId(petId);
                    newActivity.setActivityDate(activityDate);
                    return newActivity;
                });

        // 更新散步狀態
        if (dto.walked() != null) {
            activity.setWalked(dto.walked());
            if (dto.walked() && activity.getWalkTime() == null) {
                activity.setWalkTime(LocalDateTime.now());
            } else if (!dto.walked()) {
                activity.setWalkTime(null);
            }
        }

        // 更新餵食狀態
        if (dto.fed() != null) {
            activity.setFed(dto.fed());
            if (dto.fed() && activity.getFeedTime() == null) {
                activity.setFeedTime(LocalDateTime.now());
            } else if (!dto.fed()) {
                activity.setFeedTime(null);
            }
        }

        // 更新備註
        if (dto.notes() != null) {
            activity.setNotes(dto.notes());
        }

        PetActivity savedActivity = petActivityRepository.save(activity);
        return convertToDto(savedActivity);
    }

    /**
     * 取得活動歷史紀錄
     */
    public List<PetActivityDto> getActivityHistory(UUID petId) {
        validatePetExists(petId);
        return petActivityRepository.findByPetIdOrderByActivityDateDesc(petId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private void validatePetExists(UUID petId) {
        if (!petRepository.existsById(petId)) {
            throw new ResourceNotFoundException("寵物", "id", petId);
        }
    }

    private PetActivityDto convertToDto(PetActivity activity) {
        return new PetActivityDto(
                activity.getId(),
                activity.getPetId(),
                activity.getActivityDate(),
                activity.getWalked(),
                activity.getWalkTime(),
                activity.getFed(),
                activity.getFeedTime(),
                activity.getNotes(),
                activity.getCreatedAt()
        );
    }

    private PetActivityDto createEmptyActivity(UUID petId, LocalDate date) {
        return new PetActivityDto(
                null,
                petId,
                date,
                false,
                null,
                false,
                null,
                null,
                null
        );
    }
}
