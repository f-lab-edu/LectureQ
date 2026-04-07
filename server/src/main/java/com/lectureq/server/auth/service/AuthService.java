package com.lectureq.server.auth.service;

import com.lectureq.server.auth.dto.LoginResponse;
import com.lectureq.server.auth.entity.RefreshToken;
import com.lectureq.server.auth.repository.RefreshTokenRepository;
import com.lectureq.server.global.error.BusinessException;
import com.lectureq.server.global.error.ErrorCode;
import com.lectureq.server.global.infra.kakao.KakaoClient;
import com.lectureq.server.global.infra.kakao.KakaoTokenResponse;
import com.lectureq.server.global.infra.kakao.KakaoUserResponse;
import com.lectureq.server.global.jwt.JwtProvider;
import com.lectureq.server.user.entity.User;
import com.lectureq.server.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoClient kakaoClient;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public LoginResult login(String code) {
        // 1. 카카오 토큰 발급
        KakaoTokenResponse tokenResponse = kakaoClient.getToken(code);

        // 2. 카카오 사용자 정보 조회
        KakaoUserResponse userResponse = kakaoClient.getUserInfo(tokenResponse.getAccessToken());

        // 3. 회원 확인/가입
        String kakaoId = String.valueOf(userResponse.getId());
        User user = userRepository.findByKakaoId(kakaoId)
                .map(existingUser -> {
                    existingUser.updateProfile(
                            userResponse.getNickname(),
                            userResponse.getProfileImageUrl());
                    return existingUser;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .kakaoId(kakaoId)
                        .nickname(userResponse.getNickname())
                        .profileImage(userResponse.getProfileImageUrl())
                        .build()));

        // 4. 기존 RefreshToken 삭제
        refreshTokenRepository.deleteByUser(user);

        // 5-6. JWT 생성
        String accessToken = jwtProvider.createAccessToken(user.getId());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 7. RefreshToken DB 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build());

        // 8. 응답
        return new LoginResult(accessToken, refreshToken, new LoginResponse(user));
    }

    public record LoginResult(String accessToken, String refreshToken, LoginResponse loginResponse) {}

    @Transactional
    public RefreshResult refresh(String refreshToken) {
        // 1. 토큰 유효성 검증
        if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh Token이 만료되었습니다. 다시 로그인해주세요.");
        }

        // 2. DB에서 존재 여부 확인
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh Token이 만료되었습니다. 다시 로그인해주세요."));

        // 3. 기존 Refresh Token 삭제 (Token Rotation)
        refreshTokenRepository.delete(storedToken);

        // 4. 새 토큰 쌍 생성
        Long userId = storedToken.getUser().getId();
        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        // 5. 새 Refresh Token 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .user(storedToken.getUser())
                .token(newRefreshToken)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build());

        return new RefreshResult(newAccessToken, newRefreshToken);
    }

    public record RefreshResult(String accessToken, String refreshToken) {}

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            refreshTokenRepository.findByToken(refreshToken)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }
}
