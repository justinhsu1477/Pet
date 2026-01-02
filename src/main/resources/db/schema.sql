-- Cat 表
CREATE TABLE IF NOT EXISTS cat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    age INT
);

-- Pet 表
CREATE TABLE IF NOT EXISTS pet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    type VARCHAR(255),
    age INT,
    breed VARCHAR(255),
    owner_name VARCHAR(255),
    owner_phone VARCHAR(255),
    special_needs VARCHAR(500)
);

-- Sitter 表
CREATE TABLE IF NOT EXISTS sitter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    experience VARCHAR(500)
);

-- SitterRecord 表
CREATE TABLE IF NOT EXISTS sitter_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT,
    sitter_id BIGINT,
    record_time TIMESTAMP,
    activity VARCHAR(255),
    fed BOOLEAN,
    walked BOOLEAN,
    mood_status VARCHAR(255),
    notes VARCHAR(1000),
    photos VARCHAR(500),
    FOREIGN KEY (pet_id) REFERENCES pet(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id)
);