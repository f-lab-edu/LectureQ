package com.lectureq.lectureq.global.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                3600000L,
                2592000000L
        );
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("Access Token을 생성하고 userId를 추출할 수 있다")
    void createAccessToken_and_getUserId() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("Refresh Token을 생성하고 userId를 추출할 수 있다")
    void createRefreshToken_and_getUserId() {
        String token = jwtTokenProvider.createRefreshToken(1L);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(1L);
    }

    @Test
    @DisplayName("유효한 토큰은 validateToken이 true를 반환한다")
    void validateToken_withValidToken_returnsTrue() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("잘못된 토큰은 validateToken이 false를 반환한다")
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse();
    }

    @Test
    @DisplayName("빈 토큰은 validateToken이 false를 반환한다")
    void validateToken_withEmptyToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 validateToken이 false를 반환한다")
    void validateToken_withExpiredToken_returnsFalse() {
        JwtProperties shortLivedProperties = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm",
                -1000L,
                -1000L
        );
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLivedProperties);

        String token = shortLivedProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("다른 키로 서명된 토큰은 validateToken이 false를 반환한다")
    void validateToken_withDifferentKey_returnsFalse() {
        JwtProperties otherProperties = new JwtProperties(
                "other-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithms",
                3600000L,
                2592000000L
        );
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProperties);

        String token = otherProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("Access Token은 isAccessToken이 true를 반환한다")
    void isAccessToken_withAccessToken_returnsTrue() {
        String token = jwtTokenProvider.createAccessToken(1L);

        assertThat(jwtTokenProvider.isAccessToken(token)).isTrue();
        assertThat(jwtTokenProvider.isRefreshToken(token)).isFalse();
    }

    @Test
    @DisplayName("Refresh Token은 isRefreshToken이 true를 반환한다")
    void isRefreshToken_withRefreshToken_returnsTrue() {
        String token = jwtTokenProvider.createRefreshToken(1L);

        assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
        assertThat(jwtTokenProvider.isAccessToken(token)).isFalse();
    }
}
