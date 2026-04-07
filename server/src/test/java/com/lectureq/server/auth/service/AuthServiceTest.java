package com.lectureq.server.auth.service;

import com.lectureq.server.auth.entity.RefreshToken;
import com.lectureq.server.auth.repository.RefreshTokenRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
}
