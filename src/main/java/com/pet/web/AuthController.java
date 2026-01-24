package com.pet.web;

import com.pet.domain.Users;
import com.pet.dto.LoginRequestDto;
import com.pet.dto.LoginResponseDto;
import com.pet.dto.response.ApiResponse;
import com.pet.exception.AuthenticationException;
import com.pet.exception.ErrorCode;
import com.pet.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        Users users = authenticationService.authenticate(loginRequest.username(), loginRequest.password());

        if (users == null) {
            throw new AuthenticationException(ErrorCode.INVALID_PASSWORD);
        }

        // 根據角色取得對應的 ID 和名稱
        UUID roleId = null;
        String roleName = null;

        switch (users.getRole()) {
            case CUSTOMER:
                if (users.getCustomer() != null) {
                    roleId = users.getCustomer().getId();
                    roleName = users.getCustomer().getName();
                }
                break;
            case SITTER:
                if (users.getSitter() != null) {
                    roleId = users.getSitter().getId();
                    roleName = users.getSitter().getName();
                }
                break;
            case ADMIN:
                // 管理員可能沒有對應的角色資料
                roleId = users.getId();
                roleName = users.getUsername();
                break;
        }

        LoginResponseDto loginResponse = new LoginResponseDto(
                users.getId(),
                users.getUsername(),
                users.getEmail(),
                users.getPhone(),
                users.getRole().name(),
                roleId,
                roleName,
                "登入成功"
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }
}
