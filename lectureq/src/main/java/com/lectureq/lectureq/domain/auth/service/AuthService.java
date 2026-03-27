package com.lectureq.lectureq.domain.auth.service;

import com.lectureq.lectureq.domain.auth.dto.TokenResponse;
import com.lectureq.lectureq.domain.entity.RefreshToken;
import com.lectureq.lectureq.domain.entity.User;
import com.lectureq.lectureq.global.auth.JwtProperties;
import com.lectureq.lectureq.global.auth.JwtTokenProvider;
import com.lectureq.lectureq.global.exception.ErrorCode;
import com.lectureq.lectureq.global.exception.NotFoundException;
import com.lectureq.lectureq.global.exception.UnauthorizedException;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final EntityManager entityManager;

    @Transactional
    public TokenResponse refresh(String requestToken) {
        if (!jwtTokenProvider.validateToken(requestToken) || !jwtTokenProvider.isRefreshToken(requestToken)) {
            throw new UnauthorizedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.getUserId(requestToken);
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND);
        }

        RefreshToken storedToken = refreshTokenService.findByUser(user)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.getToken().equals(requestToken)) {
            refreshTokenService.deleteByUser(user);
            throw new UnauthorizedException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.refreshExpiration() / 1000);
        refreshTokenService.saveOrUpdate(user, newRefreshToken, expiresAt);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
