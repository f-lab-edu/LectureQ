package com.lectureq.server.auth;

import com.lectureq.server.auth.config.CookieProperties;
import com.lectureq.server.global.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthCookieFactory {

    public static final String ACCESS_TOKEN = "accessToken";
    public static final String REFRESH_TOKEN = "refreshToken";

    private static final String ACCESS_TOKEN_PATH = "/";
    private static final String REFRESH_TOKEN_PATH = "/api/v1/auth";
    private static final String SAME_SITE = "Lax";

    private final CookieProperties cookieProperties;
    private final JwtProperties jwtProperties;

    public ResponseCookie accessToken(String value) {
        return build(ACCESS_TOKEN, value, ACCESS_TOKEN_PATH, jwtProperties.accessExpiration() / 1000);
    }

    public ResponseCookie refreshToken(String value) {
        return build(REFRESH_TOKEN, value, REFRESH_TOKEN_PATH, jwtProperties.refreshExpiration() / 1000);
    }

    public ResponseCookie expiredAccessToken() {
        return build(ACCESS_TOKEN, "", ACCESS_TOKEN_PATH, 0);
    }

    public ResponseCookie expiredRefreshToken() {
        return build(REFRESH_TOKEN, "", REFRESH_TOKEN_PATH, 0);
    }

    private ResponseCookie build(String name, String value, String path, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProperties.secure())
                .sameSite(SAME_SITE)
                .path(path)
                .maxAge(maxAgeSeconds)
                .build();
    }
}
