package com.lectureq.server.analysis.entity;

import com.lectureq.server.global.entity.BaseEntity;
import com.lectureq.server.recording.entity.Recording;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Analysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_id", unique = true, nullable = false)
    private Recording recording;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    @Column(columnDefinition = "JSON")
    private String summary;

    @Column(length = 20)
    private String failedStep;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime completedAt;

    @Builder
    public Analysis(Recording recording, String status) {
        this.recording = recording;
        this.status = status;
    }
}
