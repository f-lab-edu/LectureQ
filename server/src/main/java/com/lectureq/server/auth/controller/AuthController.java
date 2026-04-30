package com.lectureq.server.auth.controller;

import com.lectureq.server.auth.AuthCookieFactory;
import com.lectureq.server.auth.dto.LoginRequest;
import com.lectureq.server.auth.dto.LoginResponse;
import com.lectureq.server.auth.service.AuthService;
import com.lectureq.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieFactory cookieFactory;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.getCode());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.accessToken(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieFactory.refreshToken(result.refreshToken()).toString())
                .body(ApiResponse.success("로그인 성공", result.loginResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = AuthCookieFactory.REFRESH_TOKEN, required = false) String refreshToken) {
        AuthService.RefreshResult result = authService.refresh(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.accessToken(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieFactory.refreshToken(result.refreshToken()).toString())
                .body(ApiResponse.successMessage("토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.logout(userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expiredAccessToken().toString())
                .header(HttpHeaders.SET_COOKIE, cookieFactory.expiredRefreshToken().toString())
                .body(ApiResponse.successMessage("로그아웃 성공"));
    }
}
