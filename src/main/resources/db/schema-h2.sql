-- 刪除舊表（注意順序，先刪除有 FK 的表）
DROP TABLE IF EXISTS sitter_rating;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS sitter_availability;
DROP TABLE IF EXISTS pet_activity;
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
    user_id UUID,
    pet_type VARCHAR(31) NOT NULL,
    name VARCHAR(100),
    age INT,
    breed VARCHAR(100),
    gender VARCHAR(10),
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20),
    special_needs VARCHAR(500),
    is_neutered BOOLEAN,
    vaccine_status VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
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
    experience VARCHAR(500),
    average_rating DOUBLE,
    rating_count INT DEFAULT 0,
    completed_bookings INT DEFAULT 0
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

-- PetActivity 表 (寵物每日活動紀錄)
CREATE TABLE IF NOT EXISTS pet_activity (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    pet_id UUID NOT NULL,
    activity_date DATE NOT NULL,
    walked BOOLEAN DEFAULT FALSE,
    walk_time TIMESTAMP,
    fed BOOLEAN DEFAULT FALSE,
    feed_time TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE
);

-- ============================================
-- 新增功能：預約系統 & 評價系統
-- ============================================

-- SitterAvailability 表 (保母可用時段)
CREATE TABLE IF NOT EXISTS sitter_availability (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    sitter_id UUID NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    service_area VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (sitter_id) REFERENCES sitter(id) ON DELETE CASCADE,
    UNIQUE (sitter_id, day_of_week, start_time, end_time)
);

-- Booking 表 (預約訂單)
CREATE TABLE IF NOT EXISTS booking (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    pet_id UUID NOT NULL,
    sitter_id UUID NOT NULL,
    user_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT DEFAULT 0,
    notes VARCHAR(500),
    sitter_response VARCHAR(500),
    total_price DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pet(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Booking 索引
CREATE INDEX IF NOT EXISTS idx_booking_sitter_time ON booking(sitter_id, start_time, end_time);
CREATE INDEX IF NOT EXISTS idx_booking_status ON booking(status);

-- SitterRating 表 (保母評價)
CREATE TABLE IF NOT EXISTS sitter_rating (
    id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    booking_id UUID NOT NULL UNIQUE,
    sitter_id UUID NOT NULL,
    user_id UUID NOT NULL,
    overall_rating INT NOT NULL,
    professionalism_rating INT,
    communication_rating INT,
    punctuality_rating INT,
    comment VARCHAR(1000),
    sitter_reply VARCHAR(500),
    is_anonymous BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);