package com.pet.dto;

import java.util.UUID;

public record LoginResponseDto(
        UUID userId,           // User 帳號 ID
        String username,       // 用戶名
        String email,          // Email
        String phone,          // 電話
        String role,           // 角色 (CUSTOMER, SITTER, ADMIN)
        UUID roleId,           // 角色資料 ID (Customer.id 或 Sitter.id)
        String roleName,       // 角色名稱 (Customer.name 或 Sitter.name)
        String message         // 登入訊息
) {
}