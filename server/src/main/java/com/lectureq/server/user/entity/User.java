package com.lectureq.server.user.entity;

import com.lectureq.server.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String kakaoId;

    @Column(length = 50)
    private String nickname;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    public void updateProfile(String nickname, String profileImage) {
        this.nickname = nickname;
        this.profileImage = profileImage;
    }

    @Builder
    public User(String kakaoId, String nickname, String profileImage) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.profileImage = profileImage;
    }
}
