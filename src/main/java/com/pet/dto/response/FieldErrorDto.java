package com.pet.dto.response;

public record FieldErrorDto(
        String field,
        String code,
        String message,
        Object rejectedValue) {
}
