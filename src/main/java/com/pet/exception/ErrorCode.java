package com.pet.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 認證相關錯誤
    USER_NOT_FOUND("USER_NOT_FOUND", "用戶不存在"),
    INVALID_PASSWORD("INVALID_PASSWORD", "密碼錯誤"),
    USERNAME_ALREADY_EXISTS("USERNAME_ALREADY_EXISTS", "用戶名已存在"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "認證失敗"),

    // 資源不存在錯誤
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "資源不存在"),
    PET_NOT_FOUND("PET_NOT_FOUND", "找不到指定的寵物"),
    SITTER_NOT_FOUND("SITTER_NOT_FOUND", "找不到指定的保母"),
    RECORD_NOT_FOUND("RECORD_NOT_FOUND", "找不到指定的記錄"),

    // 驗證錯誤
    VALIDATION_FAILED("VALIDATION_FAILED", "請檢查輸入的資料是否正確"),
    INVALID_INPUT("INVALID_INPUT", "輸入資料格式不正確"),

    // 欄位驗證錯誤
    FIELD_REQUIRED("FIELD_REQUIRED", "此欄位為必填"),
    FIELD_INVALID_FORMAT("FIELD_INVALID_FORMAT", "格式不正確"),
    FIELD_TOO_LONG("FIELD_TOO_LONG", "內容長度超過限制"),
    FIELD_OUT_OF_RANGE("OUT_OF_RANGE", "數值超出允許範圍"),

    // 系統錯誤
    INTERNAL_ERROR("INTERNAL_ERROR", "系統發生錯誤,請稍後再試"),
    DATABASE_ERROR("DATABASE_ERROR", "資料庫操作失敗"),

    // 業務邏輯錯誤
    BUSINESS_LOGIC_ERROR("BUSINESS_LOGIC_ERROR", "操作不符合業務規則"),

    // 預約相關錯誤
    BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", "找不到指定的預約"),
    BOOKING_CONFLICT("BOOKING_CONFLICT", "該時段已有其他預約"),
    BOOKING_INVALID_TIME("BOOKING_INVALID_TIME", "預約時間無效"),
    BOOKING_INVALID_STATUS_TRANSITION("BOOKING_INVALID_STATUS_TRANSITION", "無法進行此狀態變更"),
    BOOKING_ALREADY_PROCESSED("BOOKING_ALREADY_PROCESSED", "預約已被處理"),
    SITTER_NOT_AVAILABLE("SITTER_NOT_AVAILABLE", "保母在該時段不可用"),

    // 評價相關錯誤
    RATING_NOT_FOUND("RATING_NOT_FOUND", "找不到指定的評價"),
    RATING_ALREADY_EXISTS("RATING_ALREADY_EXISTS", "此預約已經評價過"),
    RATING_BOOKING_NOT_COMPLETED("RATING_BOOKING_NOT_COMPLETED", "只能對已完成的預約進行評價"),
    RATING_UNAUTHORIZED("RATING_UNAUTHORIZED", "您無權對此預約進行評價");

    private final String code;
    private final String userMessage;

    ErrorCode(String code, String userMessage) {
        this.code = code;
        this.userMessage = userMessage;
    }

    // Compatibility method
    public String getMessage() {
        return userMessage;
    }
}
