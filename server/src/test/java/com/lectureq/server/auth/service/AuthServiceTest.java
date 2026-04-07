package com.lectureq.server.auth.service;

import com.lectureq.server.auth.entity.RefreshToken;
import com.lectureq.server.auth.repository.RefreshTokenRepository;
import com.lectureq.server.global.error.BusinessException;
import com.lectureq.server.global.infra.kakao.KakaoClient;
import com.lectureq.server.global.infra.kakao.KakaoTokenResponse;
import com.lectureq.server.global.infra.kakao.KakaoUserResponse;
import com.lectureq.server.global.jwt.JwtProvider;
import com.lectureq.server.user.entity.User;
import com.lectureq.server.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private KakaoClient kakaoClient;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private KakaoTokenResponse kakaoTokenResponse;

    @Mock
    private KakaoUserResponse kakaoUserResponse;

    @Test
    void 신규_사용자_로그인_성공() {
        // given
        given(kakaoClient.getToken("test-code")).willReturn(kakaoTokenResponse);
        given(kakaoTokenResponse.getAccessToken()).willReturn("kakao-access-token");
        given(kakaoClient.getUserInfo("kakao-access-token")).willReturn(kakaoUserResponse);
        given(kakaoUserResponse.getId()).willReturn(12345L);
        given(kakaoUserResponse.getNickname()).willReturn("홍길동");
        given(kakaoUserResponse.getProfileImageUrl()).willReturn("https://profile.jpg");
        given(userRepository.findByKakaoId("12345")).willReturn(Optional.empty());

        User savedUser = User.builder()
                .kakaoId("12345")
                .nickname("홍길동")
                .profileImage("https://profile.jpg")
                .build();
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtProvider.createAccessToken(any())).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("refresh-token");
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

        // when
        AuthService.LoginResult result = authService.login("test-code");

        // then
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.loginResponse().getUser().getNickname()).isEqualTo("홍길동");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 기존_사용자_로그인_프로필_업데이트() {
        // given
        given(kakaoClient.getToken("test-code")).willReturn(kakaoTokenResponse);
        given(kakaoTokenResponse.getAccessToken()).willReturn("kakao-access-token");
        given(kakaoClient.getUserInfo("kakao-access-token")).willReturn(kakaoUserResponse);
        given(kakaoUserResponse.getId()).willReturn(12345L);
        given(kakaoUserResponse.getNickname()).willReturn("새닉네임");
        given(kakaoUserResponse.getProfileImageUrl()).willReturn("https://new-profile.jpg");

        User existingUser = User.builder()
                .kakaoId("12345")
                .nickname("기존닉네임")
                .profileImage("https://old-profile.jpg")
                .build();
        given(userRepository.findByKakaoId("12345")).willReturn(Optional.of(existingUser));
        given(jwtProvider.createAccessToken(any())).willReturn("access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("refresh-token");
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

        // when
        AuthService.LoginResult result = authService.login("test-code");

        // then
        assertThat(existingUser.getNickname()).isEqualTo("새닉네임");
        assertThat(existingUser.getProfileImage()).isEqualTo("https://new-profile.jpg");
        verify(userRepository, never()).save(any(User.class));
        verify(refreshTokenRepository).deleteByUser(existingUser);
    }

    @Test
    void 토큰_갱신_성공() {
        // given
        String oldRefreshToken = "old-refresh-token";
        User user = User.builder()
                .kakaoId("12345")
                .nickname("홍길동")
                .profileImage("https://profile.jpg")
                .build();
        RefreshToken storedToken = RefreshToken.builder()
                .user(user)
                .token(oldRefreshToken)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build();

        given(jwtProvider.validateToken(oldRefreshToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(oldRefreshToken)).willReturn(Optional.of(storedToken));
        given(jwtProvider.createAccessToken(any())).willReturn("new-access-token");
        given(jwtProvider.createRefreshToken(any())).willReturn("new-refresh-token");
        given(refreshTokenRepository.save(any(RefreshToken.class))).willReturn(null);

        // when
        AuthService.RefreshResult result = authService.refresh(oldRefreshToken);

        // then
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void 토큰_갱신_실패_DB에_없는_토큰() {
        // given
        String invalidToken = "invalid-token";
        given(jwtProvider.validateToken(invalidToken)).willReturn(true);
        given(refreshTokenRepository.findByToken(invalidToken)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.refresh(invalidToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 토큰_갱신_실패_null_토큰() {
        // when & then
        assertThatThrownBy(() -> authService.refresh(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 로그아웃_성공() {
        // given
        String refreshToken = "refresh-token";
        User user = User.builder()
                .kakaoId("12345")
                .nickname("홍길동")
                .profileImage("https://profile.jpg")
                .build();
        RefreshToken storedToken = RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiredAt(LocalDateTime.now().plusDays(14))
                .build();
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(storedToken));

        // when
        authService.logout(refreshToken);

        // then
        verify(refreshTokenRepository).delete(storedToken);
    }

    @Test
    void 로그아웃_토큰_없어도_성공() {
        // when
        authService.logout(null);

        // then
        verify(refreshTokenRepository, never()).findByToken(any());
    }
}
