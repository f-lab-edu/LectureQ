package com.lectureq.server.global.response;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void success_with_data() {
        ApiResponse<Integer> response = ApiResponse.success(42);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("성공");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    void success_with_message_only() {
        ApiResponse<Void> response = ApiResponse.successMessage("삭제 성공");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("삭제 성공");
        assertThat(response.getData()).isNull();
    }

    @Test
    void success_with_message_and_data() {
        ApiResponse<Integer> response = ApiResponse.success("조회 성공", 42);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("조회 성공");
        assertThat(response.getData()).isEqualTo(42);
    }

    @Test
    void success_with_custom_status() {
        ApiResponse<String> response = ApiResponse.success(201, "생성 성공", "new-id");

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getMessage()).isEqualTo("생성 성공");
        assertThat(response.getData()).isEqualTo("new-id");
    }

    @Test
    void data_included_in_json() throws Exception {
        ApiResponse<Integer> response = ApiResponse.success(42);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"data\":42");
    }

    @Test
    void null_data_excluded_from_json() throws Exception {
        ApiResponse<Void> response = ApiResponse.successMessage("삭제 성공");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"status\"");
        assertThat(json).contains("\"message\"");
        assertThat(json).doesNotContain("\"data\"");
    }
}
