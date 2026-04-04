package com.lectureq.server.recording.entity;

import com.lectureq.server.global.entity.BaseEntity;
import com.lectureq.server.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recording")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recording extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String title;

    private Integer duration;

    @Builder
    public Recording(User user, String title, Integer duration) {
        this.user = user;
        this.title = title;
        this.duration = duration;
    }
}
