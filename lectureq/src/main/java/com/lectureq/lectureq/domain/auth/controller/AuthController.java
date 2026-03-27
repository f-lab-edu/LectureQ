package com.lectureq.lectureq.domain.auth.controller;

import com.lectureq.lectureq.domain.auth.dto.RefreshTokenRequest;
import com.lectureq.lectureq.domain.auth.dto.TokenResponse;
import com.lectureq.lectureq.domain.auth.service.AuthService;
import com.lectureq.lectureq.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        TokenResponse tokenResponse = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("토큰이 갱신되었습니다", tokenResponse));
    }
}
