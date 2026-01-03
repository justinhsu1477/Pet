package com.pet.dto;

import java.util.UUID;

public record LoginResponseDto(
        UUID id,
        String username,
        String email,
        String phone,
        String role,
        String message
) {
}