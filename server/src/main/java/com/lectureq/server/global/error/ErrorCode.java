package com.lectureq.server.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(400, "잘못된 요청입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    NOT_FOUND(404, "요청한 리소스가 존재하지 않습니다"),
    CONFLICT(409, "리소스 상태가 충돌합니다"),
    GONE(410, "요청한 리소스가 더 이상 존재하지 않습니다"),
    TOO_MANY_REQUESTS(429, "요청이 너무 많습니다"),
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다");

    private final int status;
    private final String message;
}
