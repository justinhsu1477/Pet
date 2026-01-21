package com.pet.service;

import com.pet.domain.*;
import com.pet.domain.Booking.BookingStatus;
import com.pet.dto.SitterRatingDto;
import com.pet.dto.SitterRatingStatsDto;
import com.pet.exception.BusinessException;
import com.pet.exception.ErrorCode;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.BookingRepository;
import com.pet.repository.SitterRatingRepository;
import com.pet.repository.SitterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 保母評價服務
 *
 * 面試亮點：
 * 1. 防濫用：只有 COMPLETED 訂單才能評價
 * 2. 唯一性檢查：一個訂單只能評價一次
 * 3. 加權平均計算：總體40% + 專業25% + 溝通20% + 準時15%
 * 4. 反正規化更新：更新評價時同步更新 Sitter 的平均分
 */
@Service
@Transactional
public class SitterRatingService {

    private final SitterRatingRepository ratingRepository;
    private final BookingRepository bookingRepository;
    private final SitterRepository sitterRepository;

    public SitterRatingService(SitterRatingRepository ratingRepository,
                               BookingRepository bookingRepository,
                               SitterRepository sitterRepository) {
        this.ratingRepository = ratingRepository;
        this.bookingRepository = bookingRepository;
        this.sitterRepository = sitterRepository;
    }

    /**
     * 建立評價
     * 面試重點：
     * 1. 驗證訂單狀態（只有 COMPLETED 可評價）
     * 2. 驗證評價者身份（只有訂單的飼主可評價）
     * 3. 防止重複評價
     * 4. 更新保母的平均評分（反正規化）
     */
    public SitterRatingDto createRating(SitterRatingDto dto, UUID userId) {
        // 1. 取得並驗證預約
        Booking booking = bookingRepository.findById(dto.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("預約", "id", dto.bookingId()));

        // 2. 驗證訂單狀態
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.RATING_BOOKING_NOT_COMPLETED);
        }

        // 3. 驗證評價者身份
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.RATING_UNAUTHORIZED);
        }

        // 4. 檢查是否已評價
        if (ratingRepository.existsByBookingId(dto.bookingId())) {
            throw new BusinessException(ErrorCode.RATING_ALREADY_EXISTS);
        }

        // 5. 建立評價
        SitterRating rating = new SitterRating();
        rating.setBooking(booking);
        rating.setSitter(booking.getSitter());
        rating.setUser(booking.getUser());
        rating.setOverallRating(dto.overallRating());
        rating.setProfessionalismRating(dto.professionalismRating());
        rating.setCommunicationRating(dto.communicationRating());
        rating.setPunctualityRating(dto.punctualityRating());
        rating.setComment(dto.comment());
        rating.setIsAnonymous(dto.isAnonymous() != null ? dto.isAnonymous() : false);

        SitterRating saved = ratingRepository.save(rating);

        // 6. 更新保母的平均評分（反正規化）
        updateSitterAverageRating(booking.getSitter().getId());

        return convertToDto(saved);
    }

    /**
     * 保母回覆評價
     */
    public SitterRatingDto replyToRating(UUID ratingId, String reply, UUID sitterId) {
        SitterRating rating = ratingRepository.findById(ratingId)
                .orElseThrow(() -> new ResourceNotFoundException("評價", "id", ratingId));

        // 驗證是評價的對象保母
        if (!rating.getSitter().getId().equals(sitterId)) {
            throw new BusinessException(ErrorCode.RATING_UNAUTHORIZED, "只有被評價的保母可以回覆");
        }

        rating.setSitterReply(reply);
        SitterRating updated = ratingRepository.save(rating);
        return convertToDto(updated);
    }

    /**
     * 取得保母的評價列表（分頁）
     */
    @Transactional(readOnly = true)
    public Page<SitterRatingDto> getSitterRatings(UUID sitterId, Pageable pageable) {
        return ratingRepository.findBySitterIdOrderByCreatedAtDesc(sitterId, pageable)
                .map(this::convertToDto);
    }

    /**
     * 取得保母的評價統計
     * 面試重點：加權平均計算 + 評分分佈統計
     */
    @Transactional(readOnly = true)
    public SitterRatingStatsDto getSitterRatingStats(UUID sitterId) {
        Sitter sitter = sitterRepository.findById(sitterId)
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", sitterId));

        // 計算各項平均分
        Object[] averages = ratingRepository.calculateDetailedAverages(sitterId);
        Double avgOverall = averages[0] != null ? (Double) averages[0] : null;
        Double avgProfessionalism = averages[1] != null ? (Double) averages[1] : null;
        Double avgCommunication = averages[2] != null ? (Double) averages[2] : null;
        Double avgPunctuality = averages[3] != null ? (Double) averages[3] : null;

        // 計算評分分佈
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        List<Object[]> ratingCounts = ratingRepository.countRatingsByStars(sitterId);
        for (Object[] row : ratingCounts) {
            Integer stars = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(stars, count);
        }

        long totalRatings = ratingRepository.countBySitterId(sitterId);

        return new SitterRatingStatsDto(
                sitterId,
                sitter.getName(),
                sitter.getAverageRating(),
                avgProfessionalism,
                avgCommunication,
                avgPunctuality,
                (int) totalRatings,
                sitter.getCompletedBookings(),
                distribution.get(5).intValue(),
                distribution.get(4).intValue(),
                distribution.get(3).intValue(),
                distribution.get(2).intValue(),
                distribution.get(1).intValue()
        );
    }

    /**
     * 取得單一評價
     */
    @Transactional(readOnly = true)
    public SitterRatingDto getRatingById(UUID id) {
        SitterRating rating = ratingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("評價", "id", id));
        return convertToDto(rating);
    }

    /**
     * 取得預約的評價
     */
    @Transactional(readOnly = true)
    public SitterRatingDto getRatingByBooking(UUID bookingId) {
        SitterRating rating = ratingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("評價", "bookingId", bookingId));
        return convertToDto(rating);
    }

    /**
     * 取得使用者給出的所有評價
     */
    @Transactional(readOnly = true)
    public List<SitterRatingDto> getUserRatings(UUID userId) {
        return ratingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ============ Private Methods ============

    /**
     * 更新保母的平均評分（反正規化）
     * 面試重點：使用加權平均計算
     */
    private void updateSitterAverageRating(UUID sitterId) {
        Double weightedAvg = ratingRepository.calculateWeightedAverageRating(sitterId);
        long count = ratingRepository.countBySitterId(sitterId);

        Sitter sitter = sitterRepository.findById(sitterId)
                .orElseThrow(() -> new ResourceNotFoundException("保母", "id", sitterId));

        sitter.setAverageRating(weightedAvg != null ? Math.round(weightedAvg * 100.0) / 100.0 : null);
        sitter.setRatingCount((int) count);
        sitterRepository.save(sitter);
    }

    private SitterRatingDto convertToDto(SitterRating rating) {
        String userName = rating.getIsAnonymous() ? "匿名用戶" : rating.getUser().getUsername();

        return new SitterRatingDto(
                rating.getId(),
                rating.getBooking().getId(),
                rating.getSitter().getId(),
                rating.getSitter().getName(),
                rating.getUser().getId(),
                userName,
                rating.getOverallRating(),
                rating.getProfessionalismRating(),
                rating.getCommunicationRating(),
                rating.getPunctualityRating(),
                rating.getComment(),
                rating.getSitterReply(),
                rating.getIsAnonymous(),
                rating.getWeightedScore(),
                rating.getCreatedAt()
        );
    }
}
