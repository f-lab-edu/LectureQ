package com.lectureq.server.global.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    void 유효한_토큰으로_보호된_API_접근_인증_통과() throws Exception {
        String token = jwtProvider.createAccessToken(1L);

        int status = mockMvc.perform(get("/api/v1/users/me")
                        .cookie(new Cookie("accessToken", token)))
                .andReturn().getResponse().getStatus();

        assertThat(status).isNotEqualTo(401);
    }

    @Test
    void 토큰_없이_보호된_API_접근_401() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void 만료된_토큰으로_보호된_API_접근_401() throws Exception {
        JwtProvider shortLived = new JwtProvider(new JwtProperties(
                "dGVzdC1zZWNyZXQta2V5LW11c3QtYmUtYXQtbGVhc3QtMzItYnl0ZXMtbG9uZw==", 0, 0));
        shortLived.init();
        String expired = shortLived.createAccessToken(1L);

        mockMvc.perform(get("/api/v1/users/me")
                        .cookie(new Cookie("accessToken", expired)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permitAll_경로는_토큰_없이_접근_가능() throws Exception {
        int status = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content("{\"code\": \"\"}"))
                .andReturn().getResponse().getStatus();

        // 인증 단에서는 막히지 않고, 검증 단에서 400 반환됨
        assertThat(status).isNotEqualTo(401);
    }
}
