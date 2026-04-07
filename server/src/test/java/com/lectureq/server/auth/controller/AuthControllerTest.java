package com.lectureq.server.auth.controller;

import com.lectureq.server.auth.dto.LoginResponse;
import com.lectureq.server.auth.service.AuthService;
import com.lectureq.server.global.config.SecurityConfig;
import com.lectureq.server.global.error.GlobalExceptionHandler;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

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
}
