package com.lectureq.server.auth.controller;

import com.lectureq.server.auth.dto.LoginRequest;
import com.lectureq.server.auth.dto.LoginResponse;
import com.lectureq.server.auth.service.AuthService;
import com.lectureq.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.getCode());

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", result.accessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(1800)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(1209600)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인 성공", result.loginResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        AuthService.RefreshResult result = authService.refresh(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", result.accessToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(1800)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", result.refreshToken())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(1209600)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.successMessage("토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        authService.logout(refreshToken);

        ResponseCookie expiredAccessToken = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie expiredRefreshToken = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRefreshToken.toString())
                .body(ApiResponse.successMessage("로그아웃 성공"));
    }
}
