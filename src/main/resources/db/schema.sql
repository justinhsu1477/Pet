-- 刪除舊表（注意順序，先刪除有 FK 的表）
DROP TABLE IF EXISTS sitter_record;
DROP TABLE IF EXISTS dog;
DROP TABLE IF EXISTS cat;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS sitter;
DROP TABLE IF EXISTS users;

-- Users 表
CREATE TABLE IF NOT EXISTS users (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20)
);

-- Pet 表 (父類別，使用 JOINED 繼承策略)
CREATE TABLE IF NOT EXISTS pet (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    pet_type VARCHAR(31) NOT NULL,
    name VARCHAR(100),
    age INT,
    breed VARCHAR(100),
    gender VARCHAR(10),
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20),
    special_needs VARCHAR(500),
    is_neutered BOOLEAN,
    vaccine_status VARCHAR(255)
);

-- Dog 表 (子類別，繼承 Pet)
CREATE TABLE IF NOT EXISTS dog (
    id UUID PRIMARY KEY,
    size VARCHAR(20),
    is_walk_required BOOLEAN,
    walk_frequency_per_day INT,
    training_level VARCHAR(20),
    is_friendly_with_dogs BOOLEAN,
    is_friendly_with_people BOOLEAN,
    is_friendly_with_children BOOLEAN,
    FOREIGN KEY (id) REFERENCES pet(id)
);

-- Cat 表 (子類別，繼承 Pet)
CREATE TABLE IF NOT EXISTS cat (
    id UUID PRIMARY KEY,
    is_indoor BOOLEAN,
    litter_box_type VARCHAR(20),
    scratching_habit VARCHAR(20),
    FOREIGN KEY (id) REFERENCES pet(id)
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