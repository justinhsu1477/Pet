package com.pet.repository;

import com.pet.domain.PetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetPhotoRepository extends JpaRepository<PetPhoto, UUID> {

    @Query("SELECT p FROM PetPhoto p " +
           "LEFT JOIN FETCH p.pet " +
           "JOIN FETCH p.sitter " +
           "WHERE p.pet.id = :petId " +
           "ORDER BY p.uploadedAt DESC")
    List<PetPhoto> findByPetIdOrderByUploadedAtDesc(@Param("petId") UUID petId);

    @Query("SELECT p FROM PetPhoto p " +
           "LEFT JOIN FETCH p.pet " +
           "JOIN FETCH p.sitter " +
           "WHERE p.sitter.id = :sitterId " +
           "ORDER BY p.uploadedAt DESC")
    List<PetPhoto> findBySitterIdOrderByUploadedAtDesc(@Param("sitterId") UUID sitterId);

    Optional<PetPhoto> findByMessageId(String messageId);
}
