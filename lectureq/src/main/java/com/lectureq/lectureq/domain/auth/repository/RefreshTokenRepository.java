package com.lectureq.lectureq.domain.auth.repository;

import com.lectureq.lectureq.domain.entity.RefreshToken;
import com.lectureq.lectureq.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
