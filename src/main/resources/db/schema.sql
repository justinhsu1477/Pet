-- Users 表
CREATE TABLE IF NOT EXISTS users (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20)
);

-- Cat 表
CREATE TABLE IF NOT EXISTS cat (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(255),
    age INT
);

-- Pet 表
CREATE TABLE IF NOT EXISTS pet (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
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
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    experience VARCHAR(500)
);

-- SitterRecord 表
CREATE TABLE IF NOT EXISTS sitter_record (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    pet_id UUID,
    sitter_id UUID,
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