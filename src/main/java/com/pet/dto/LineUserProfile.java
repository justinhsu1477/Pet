package com.pet.dto;

public record LineUserProfile(
        String userId,
        String displayName,
        String pictureUrl,
        String email
) {}
