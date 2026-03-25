package com.lectureq.lectureq.support;

import com.lectureq.lectureq.domain.entity.*;

import java.time.LocalDateTime;

public class TestFixture {

    public static User createUser() {
        return User.builder()
                .kakaoId("12345")
                .nickname("테스트유저")
                .email("test@kakao.com")
                .profileImage("https://example.com/profile.jpg")
                .build();
    }

    public static User createUser(String kakaoId, String nickname) {
        return User.builder()
                .kakaoId(kakaoId)
                .nickname(nickname)
                .email(nickname + "@kakao.com")
                .build();
    }

    public static Recording createRecording(User user) {
        return Recording.builder()
                .user(user)
                .title("테스트 녹음")
                .duration(3600)
                .fileSize(1024000L)
                .source("APP")
                .build();
    }

    public static Recording createRecording(User user, String title) {
        return Recording.builder()
                .user(user)
                .title(title)
                .duration(3600)
                .fileSize(1024000L)
                .source("APP")
                .build();
    }

    public static Analysis createAnalysis(Recording recording) {
        return Analysis.builder()
                .recording(recording)
                .status("PENDING")
                .build();
    }

    public static Question createQuestion(Analysis analysis, int orderNum) {
        return Question.builder()
                .analysis(analysis)
                .content("테스트 질문 " + orderNum)
                .orderNum(orderNum)
                .build();
    }

    public static RefreshToken createRefreshToken(User user) {
        return RefreshToken.builder()
                .user(user)
                .token("test-refresh-token")
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
    }
}
