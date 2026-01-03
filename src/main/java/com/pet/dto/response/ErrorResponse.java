package com.pet.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String code, // 錯誤代碼 (例如: PET_NOT_FOUND)
        String error, // HTTP 錯誤名稱 (例如: Not Found)
        String message, // 技術錯誤訊息
        String userMessage, // 使用者友善訊息
        String path,
        List<FieldErrorDto> fieldErrors // 結構化的欄位錯誤
) {
    // 簡化構造函數
    public ErrorResponse(LocalDateTime timestamp, int status, String code, String error, String message,
            String userMessage, String path) {
        this(timestamp, status, code, error, message, userMessage, path, null);
    }
}
