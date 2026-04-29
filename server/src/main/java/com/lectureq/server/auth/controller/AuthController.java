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

    private static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String ACCESS_TOKEN_COOKIE_PATH = "/";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/auth";
    private static final String COOKIE_SAME_SITE = "Lax";

    private final AuthService authService;

    @Value("${cookie.secure}")
    private boolean cookieSecure;

    @Value("${jwt.access-expiration}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshTokenExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthService.LoginResult result = authService.login(request.getCode());

        ResponseCookie accessTokenCookie = buildAccessTokenCookie(result.accessToken(), accessTokenExpirationMs / 1000);
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenExpirationMs / 1000);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("로그인 성공", result.loginResponse()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        AuthService.RefreshResult result = authService.refresh(refreshToken);

        ResponseCookie accessTokenCookie = buildAccessTokenCookie(result.accessToken(), accessTokenExpirationMs / 1000);
        ResponseCookie refreshTokenCookie = buildRefreshTokenCookie(result.refreshToken(), refreshTokenExpirationMs / 1000);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.successMessage("토큰 재발급 성공"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        authService.logout(userId);

        ResponseCookie expiredAccessToken = buildAccessTokenCookie("", 0);
        ResponseCookie expiredRefreshToken = buildRefreshTokenCookie("", 0);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessToken.toString())
                .header(HttpHeaders.SET_COOKIE, expiredRefreshToken.toString())
                .body(ApiResponse.successMessage("로그아웃 성공"));
    }

    private ResponseCookie buildAccessTokenCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(COOKIE_SAME_SITE)
                .path(ACCESS_TOKEN_COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(COOKIE_SAME_SITE)
                .path(REFRESH_TOKEN_COOKIE_PATH)
                .maxAge(maxAgeSeconds)
                .build();
    }
}
