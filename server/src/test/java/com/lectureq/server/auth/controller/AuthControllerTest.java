package com.lectureq.server.auth.controller;

import com.lectureq.server.auth.dto.LoginResponse;
import com.lectureq.server.auth.service.AuthService;
import com.lectureq.server.global.config.SecurityConfig;
import com.lectureq.server.global.error.GlobalExceptionHandler;
import com.lectureq.server.global.jwt.JwtAuthenticationEntryPoint;
import com.lectureq.server.global.jwt.JwtAuthenticationFilter;
import com.lectureq.server.global.jwt.JwtProvider;
import com.lectureq.server.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.lectureq.server.global.error.BusinessException;
import com.lectureq.server.global.error.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    void 로그인_성공() throws Exception {
        // given
        User user = User.builder()
                .kakaoId("12345")
                .nickname("홍길동")
                .profileImage("https://profile.jpg")
                .build();
        LoginResponse loginResponse = new LoginResponse(user);
        AuthService.LoginResult result = new AuthService.LoginResult(
                "access-token", "refresh-token", loginResponse);
        given(authService.login(anyString())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"test-code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.user.nickname").value("홍길동"))
                .andExpect(header().exists("Set-Cookie"))
                .andDo(result1 -> {
                    var cookies = result1.getResponse().getHeaders("Set-Cookie");
                    String accessCookie = cookies.stream()
                            .filter(c -> c.startsWith("accessToken="))
                            .findFirst().orElseThrow();
                    assertThat(accessCookie).contains("HttpOnly");
                    assertThat(accessCookie).contains("SameSite=Lax");
                    assertThat(accessCookie).contains("Path=/");

                    String refreshCookie = cookies.stream()
                            .filter(c -> c.startsWith("refreshToken="))
                            .findFirst().orElseThrow();
                    assertThat(refreshCookie).contains("HttpOnly");
                    assertThat(refreshCookie).contains("SameSite=Lax");
                    assertThat(refreshCookie).contains("Path=/api/v1/auth");
                });
    }

    @Test
    void 인가코드_누락시_400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void 유효하지_않은_카카오_토큰_401() throws Exception {
        // given
        given(authService.login(anyString()))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "카카오 인가 코드가 유효하지 않습니다."));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"invalid-code\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("카카오 인가 코드가 유효하지 않습니다."));
    }

    @Test
    void 토큰_갱신_성공() throws Exception {
        // given
        AuthService.RefreshResult result = new AuthService.RefreshResult("new-access", "new-refresh");
        given(authService.refresh(anyString())).willReturn(result);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "old-refresh")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("토큰 재발급 성공"))
                .andDo(result1 -> {
                    var cookies = result1.getResponse().getHeaders("Set-Cookie");
                    assertThat(cookies.stream().anyMatch(c -> c.startsWith("accessToken="))).isTrue();
                    assertThat(cookies.stream().anyMatch(c -> c.startsWith("refreshToken="))).isTrue();
                });
    }

    @Test
    void 토큰_갱신_실패_401() throws Exception {
        // given
        given(authService.refresh(any()))
                .willThrow(new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh Token이 만료되었습니다. 다시 로그인해주세요."));

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void 로그아웃_성공() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "some-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("로그아웃 성공"))
                .andDo(result1 -> {
                    var cookies = result1.getResponse().getHeaders("Set-Cookie");
                    String accessCookie = cookies.stream()
                            .filter(c -> c.startsWith("accessToken="))
                            .findFirst().orElseThrow();
                    assertThat(accessCookie).contains("Max-Age=0");

                    String refreshCookie = cookies.stream()
                            .filter(c -> c.startsWith("refreshToken="))
                            .findFirst().orElseThrow();
                    assertThat(refreshCookie).contains("Max-Age=0");
                });
    }
}
