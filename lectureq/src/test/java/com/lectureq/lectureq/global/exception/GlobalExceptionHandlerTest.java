package com.lectureq.lectureq.global.exception;

import com.lectureq.lectureq.global.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("BusinessException 처리 시 해당 ErrorCode의 상태코드와 메시지를 반환한다")
    void handleBusinessException() {
        BusinessException exception = new BusinessException(ErrorCode.BAD_REQUEST);

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("잘못된 요청입니다.");
        assertThat(response.getBody().getData()).isNull();
    }

    @Test
    @DisplayName("NotFoundException 처리 시 404 상태코드를 반환한다")
    void handleNotFoundException() {
        NotFoundException exception = new NotFoundException(ErrorCode.RECORDING_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("녹음을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("UnauthorizedException 처리 시 401 상태코드를 반환한다")
    void handleUnauthorizedException() {
        UnauthorizedException exception = new UnauthorizedException();

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().getMessage()).isEqualTo("인증 토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("ForbiddenException 처리 시 403 상태코드를 반환한다")
    void handleForbiddenException() {
        ForbiddenException exception = new ForbiddenException();

        ResponseEntity<ApiResponse<Void>> response = handler.handleBusinessException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getMessage()).isEqualTo("해당 리소스에 대한 접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("Validation 에러 처리 시 첫 번째 필드 에러 메시지를 반환한다")
    void handleValidationException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.addError(new FieldError("test", "field", "필수 입력값입니다."));
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("필수 입력값입니다.");
    }

    @Test
    @DisplayName("처리되지 않은 예외는 500 상태코드를 반환한다")
    void handleUnhandledException() {
        Exception exception = new RuntimeException("알 수 없는 오류");

        ResponseEntity<ApiResponse<Void>> response = handler.handleException(exception);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getMessage()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }
}
