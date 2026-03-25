package com.lectureq.lectureq.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 Bad Request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 유효하지 않습니다."),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다."),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 리소스에 대한 접근 권한이 없습니다."),

    // 404 Not Found
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    RECORDING_NOT_FOUND(HttpStatus.NOT_FOUND, "녹음을 찾을 수 없습니다."),
    ANALYSIS_NOT_FOUND(HttpStatus.NOT_FOUND, "분석 결과를 찾을 수 없습니다."),

    // 409 Conflict
    ANALYSIS_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 분석이 진행 중입니다."),
    ANALYSIS_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 분석이 완료되었습니다."),

    // 410 Gone
    TEMPORARY_FILE_EXPIRED(HttpStatus.GONE, "임시 파일이 만료되었습니다. 분석을 새로 요청해주세요."),

    // 413 Payload Too Large
    FILE_SIZE_EXCEEDED(HttpStatus.PAYLOAD_TOO_LARGE, "파일 크기가 허용 범위를 초과했습니다."),

    // 429 Too Many Requests
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
