package com.lectureq.server.auth.dto;

import com.lectureq.server.user.entity.User;
import lombok.Getter;

@Getter
public class LoginResponse {

    private final UserInfo user;

    public LoginResponse(User user) {
        this.user = new UserInfo(user);
    }

    @Getter
    public static class UserInfo {
        private final Long id;
        private final String nickname;
        /** 카카오 프로필 이미지 URL (kakao_account.profile.profile_image_url, base64 아님) */
        private final String profileImage;

        public UserInfo(User user) {
            this.id = user.getId();
            this.nickname = user.getNickname();
            this.profileImage = user.getProfileImage();
        }
    }
}
