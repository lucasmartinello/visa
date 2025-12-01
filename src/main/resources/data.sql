CREATE DATABASE IF NOT EXISTS carddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE carddb;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar usuário padrão (senha: 123456 codificada em BCrypt)
INSERT IGNORE INTO users (username, password)
VALUES ('admin', '$2a$10$m4HFSxgL5naa1ZVjn/jR0uSekVRjawv.QWL.HOy4cVyeiZ2pC0SUi');

CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_hash CHAR(64) NOT NULL UNIQUE,
    encrypted_card VARBINARY(512) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_card_hash (card_hash)
);

CREATE TABLE IF NOT EXISTS api_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    endpoint VARCHAR(255),
    http_method VARCHAR(10),
    request_payload TEXT,
    response_payload TEXT,
    status_code INT,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);