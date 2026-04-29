CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_id VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50),
    profile_image TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE recordings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    duration INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recording_id BIGINT NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL,
    transcript LONGTEXT,
    summary JSON,
    failed_step VARCHAR(20),
    error_message TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    completed_at DATETIME,
    FOREIGN KEY (recording_id) REFERENCES recordings(id) ON DELETE CASCADE
);

CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    analysis_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    order_num INT NOT NULL,
    created_at DATETIME NOT NULL,
    FOREIGN KEY (analysis_id) REFERENCES analyses(id) ON DELETE CASCADE
);

CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    expired_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_refresh_tokens_token (token),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
