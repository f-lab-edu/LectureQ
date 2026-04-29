package com.lectureq.server.auth.repository;

import com.lectureq.server.auth.entity.RefreshToken;
import com.lectureq.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUser(User user);
}
