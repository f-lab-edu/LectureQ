package com.lectureq.lectureq.global.exception;

import com.lectureq.lectureq.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.fail(errorCode.getHttpStatus().value(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("입력값이 유효하지 않습니다.");
        log.warn("ValidationException: {}", message);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.fail(400, message));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.fail(500, "서버 내부 오류가 발생했습니다."));
    }
}
