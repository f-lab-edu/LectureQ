package com.lectureq.server.recording.repository;

import com.lectureq.server.recording.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
}
