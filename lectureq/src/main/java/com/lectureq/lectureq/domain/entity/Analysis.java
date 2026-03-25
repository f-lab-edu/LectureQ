package com.lectureq.lectureq.domain.entity;

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
public class Analysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recording_id", unique = true, nullable = false)
    private Recording recording;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(columnDefinition = "LONGTEXT")
    private String transcript;

    @Column(columnDefinition = "JSON")
    private String summary;

    @Column(name = "failed_step", length = 20)
    private String failedStep;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Analysis(Recording recording, String status) {
        this.recording = recording;
        this.status = status;
    }
}
