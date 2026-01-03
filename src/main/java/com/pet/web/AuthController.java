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

        LoginResponseDto loginResponse = new LoginResponseDto(
                users.getId(),
                users.getUsername(),
                users.getEmail(),
                users.getPhone(),
                users.getRole(),
                "登入成功"
        );

        return ResponseEntity.ok(ApiResponse.success(loginResponse));
    }
}
