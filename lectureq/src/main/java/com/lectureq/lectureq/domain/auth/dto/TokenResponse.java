package com.lectureq.lectureq.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
