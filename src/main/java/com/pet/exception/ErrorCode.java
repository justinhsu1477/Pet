package com.pet.exception;

public enum ErrorCode {
    // 認證相關錯誤 (1xxx)
    USER_NOT_FOUND("1001", "用戶不存在"),
    INVALID_PASSWORD("1002", "密碼錯誤"),
    USERNAME_ALREADY_EXISTS("1003", "用戶名已存在"),
    AUTHENTICATION_FAILED("1004", "認證失敗"),

    // 資源不存在錯誤 (2xxx)
    RESOURCE_NOT_FOUND("2001", "資源不存在"),
    PET_NOT_FOUND("2002", "寵物不存在"),
    SITTER_NOT_FOUND("2003", "保母不存在"),
    RECORD_NOT_FOUND("2004", "記錄不存在"),

    // 驗證錯誤 (3xxx)
    VALIDATION_ERROR("3001", "參數驗證失敗"),
    INVALID_INPUT("3002", "無效的輸入"),

    // 業務邏輯錯誤 (4xxx)
    BUSINESS_ERROR("4001", "業務邏輯錯誤"),

    // 系統錯誤 (5xxx)
    INTERNAL_ERROR("5001", "系統內部錯誤"),
    DATABASE_ERROR("5002", "資料庫錯誤");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
