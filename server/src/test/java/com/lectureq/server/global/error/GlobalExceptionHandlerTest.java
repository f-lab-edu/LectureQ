package com.lectureq.server.global.error;

import com.lectureq.server.auth.service.AuthService;
import com.lectureq.server.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({GlobalExceptionHandler.class, GlobalExceptionHandlerTest.TestController.class})
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void businessException_default_message() throws Exception {
        mockMvc.perform(get("/test/business-default"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("요청한 리소스가 존재하지 않습니다"));
    }

    @Test
    void businessException_custom_message() throws Exception {
        mockMvc.perform(get("/test/business-custom"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("분석이 진행 중인 녹음은 삭제할 수 없습니다."));
    }

    @Test
    void validationException() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("제목은 필수입니다"));
    }

    @Test
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/business-default")
        public ApiResponse<Void> businessDefault() {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        @GetMapping("/test/business-custom")
        public ApiResponse<Void> businessCustom() {
            throw new BusinessException(ErrorCode.CONFLICT, "분석이 진행 중인 녹음은 삭제할 수 없습니다.");
        }

        @PostMapping("/test/validation")
        public ApiResponse<Void> validation(@Valid @RequestBody TestRequest request) {
            return ApiResponse.successMessage("성공");
        }

        @GetMapping("/test/unexpected")
        public ApiResponse<Void> unexpected() {
            throw new RuntimeException("unexpected error");
        }

        record TestRequest(@NotBlank(message = "제목은 필수입니다") String title) {}
    }
}
