package com.lectureq.lectureq.domain.auth.controller;

import com.lectureq.lectureq.domain.auth.dto.RefreshTokenRequest;
import com.lectureq.lectureq.domain.auth.repository.RefreshTokenRepository;
import com.lectureq.lectureq.domain.entity.RefreshToken;
import com.lectureq.lectureq.domain.entity.User;
import com.lectureq.lectureq.global.auth.JwtTokenProvider;
import com.lectureq.lectureq.support.IntegrationTestBase;
import com.lectureq.lectureq.support.TestFixture;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest extends IntegrationTestBase {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("유효한 Refresh Token으로 토큰을 재발급한다")
    void refresh_withValidToken_returnsNewTokens() throws Exception {
        User user = TestFixture.createUser();
        entityManager.persist(user);
        entityManager.flush();

        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        RefreshToken storedToken = TestFixture.createRefreshToken(user, refreshToken, expiresAt);
        refreshTokenRepository.save(storedToken);
        entityManager.flush();

        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("토큰이 갱신되었습니다"))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());

        RefreshToken updatedToken = refreshTokenRepository.findByUser(user).orElseThrow();
        assertThat(updatedToken.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("만료된 Refresh Token으로 재발급 시 401을 반환한다")
    void refresh_withExpiredToken_returns401() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("expired-invalid-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("DB와 불일치하는 Refresh Token으로 재발급 시 401을 반환하고 토큰을 삭제한다")
    void refresh_withMismatchedToken_returns401AndDeletesToken() throws Exception {
        User user = TestFixture.createUser();
        entityManager.persist(user);
        entityManager.flush();

        String requestRefreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        String storedRefreshTokenValue = "different-stored-token-value-that-does-not-match-request";
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(30);
        RefreshToken storedToken = TestFixture.createRefreshToken(user, storedRefreshTokenValue, expiresAt);
        refreshTokenRepository.save(storedToken);
        entityManager.flush();

        RefreshTokenRequest request = new RefreshTokenRequest(requestRefreshToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        assertThat(refreshTokenRepository.findByUser(user)).isEmpty();
    }

    @Test
    @DisplayName("Access Token으로 재발급 시 401을 반환한다")
    void refresh_withAccessToken_returns401() throws Exception {
        String accessToken = jwtTokenProvider.createAccessToken(1L);
        RefreshTokenRequest request = new RefreshTokenRequest(accessToken);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("빈 Refresh Token으로 재발급 시 400을 반환한다")
    void refresh_withBlankToken_returns400() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
