package com.lectureq.server.global.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String secret = "dGVzdC1zZWNyZXQta2V5LW11c3QtYmUtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==";
        jwtProvider = new JwtProvider(secret, 1800000, 1209600000);
    }

    @Test
    void 유효한_토큰_검증_성공() {
        String token = jwtProvider.createAccessToken(1L);
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    void 만료된_토큰_검증_실패() {
        JwtProvider shortLived = new JwtProvider(
                "dGVzdC1zZWNyZXQta2V5LW11c3QtYmUtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==", 0, 0);
        String token = shortLived.createAccessToken(1L);
        assertThat(jwtProvider.validateToken(token)).isFalse();
    }

    @Test
    void 변조된_토큰_검증_실패() {
        String token = jwtProvider.createAccessToken(1L);
        String tampered = token + "tampered";
        assertThat(jwtProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void 토큰에서_userId_추출() {
        String token = jwtProvider.createAccessToken(42L);
        assertThat(jwtProvider.getUserId(token)).isEqualTo(42L);
    }
}
