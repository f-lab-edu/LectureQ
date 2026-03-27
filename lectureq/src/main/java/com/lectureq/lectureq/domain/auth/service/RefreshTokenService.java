package com.lectureq.lectureq.domain.auth.service;

import com.lectureq.lectureq.domain.auth.repository.RefreshTokenRepository;
import com.lectureq.lectureq.domain.entity.RefreshToken;
import com.lectureq.lectureq.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveOrUpdate(User user, String token, LocalDateTime expiresAt) {
        Optional<RefreshToken> existing = refreshTokenRepository.findByUser(user);

        if (existing.isPresent()) {
            existing.get().update(token, expiresAt);
        } else {
            RefreshToken refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(expiresAt)
                    .build();
            refreshTokenRepository.save(refreshToken);
        }
    }

    public Optional<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
