package com.pet.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceName;
    private final String fieldName;
    private final Object fieldValue;
    private final ErrorCode errorCode;

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s 不存在: %s='%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorCode = determineErrorCode(resourceName);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, ErrorCode errorCode) {
        super(String.format("%s 不存在: %s='%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.errorCode = errorCode;
    }

    private ErrorCode determineErrorCode(String resourceName) {
        if (resourceName.contains("寵物")) {
            return ErrorCode.PET_NOT_FOUND;
        } else if (resourceName.contains("保母") && !resourceName.contains("記錄")) {
            return ErrorCode.SITTER_NOT_FOUND;
        } else if (resourceName.contains("記錄")) {
            return ErrorCode.RECORD_NOT_FOUND;
        }
        return ErrorCode.RESOURCE_NOT_FOUND;
    }
}
