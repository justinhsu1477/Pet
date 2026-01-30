-- MSSQL Schema for Pet System
-- This script is for initial setup only, not for every restart

-- Drop tables in correct order (FK constraints)
-- 使用 MSSQL 2016+ 語法，相容 Spring ScriptUtils 分號分割
DROP TABLE IF EXISTS pet_photo;
DROP TABLE IF EXISTS sitter_rating;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS sitter_availability;
DROP TABLE IF EXISTS pet_activity;
DROP TABLE IF EXISTS sitter_record;
DROP TABLE IF EXISTS dog;
DROP TABLE IF EXISTS cat;
DROP TABLE IF EXISTS pet;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS sitter;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS idempotency_keys;
DROP TABLE IF EXISTS users;

-- Users table
-- MSSQL UNIQUE 約束只允許一個 NULL，改用 filtered unique index 允許多個 NULL
CREATE TABLE users (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20),
    line_user_id VARCHAR(100)
);
CREATE UNIQUE INDEX idx_users_line_user_id ON users(line_user_id) WHERE line_user_id IS NOT NULL;

-- RefreshToken table (JWT 認證用)
CREATE TABLE refresh_tokens (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    token_hash VARCHAR(64) UNIQUE NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    expiry_date DATETIME2 NOT NULL,
    revoked BIT NOT NULL DEFAULT 0,
    device_type VARCHAR(20) NOT NULL,
    device_info VARCHAR(200),
    ip_address VARCHAR(45),
    last_used_at DATETIME2,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- RefreshToken indexes
CREATE INDEX idx_token_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_user_device ON refresh_tokens(user_id, device_type);
CREATE INDEX idx_expiry ON refresh_tokens(expiry_date);

-- Idempotency keys table (防止重複提交)
CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(64) PRIMARY KEY,
    response_body TEXT,
    http_status INT DEFAULT 200,
    created_at DATETIME2 DEFAULT GETDATE(),
    expires_at DATETIME2
);

-- Customer table (一般用戶/飼主詳細資料)
CREATE TABLE customer (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    user_id UNIQUEIDENTIFIER UNIQUE NOT NULL,
    name NVARCHAR(255) NOT NULL,
    address NVARCHAR(500),
    emergency_contact NVARCHAR(255),
    emergency_phone VARCHAR(20),
    member_level VARCHAR(20) DEFAULT 'BRONZE',
    total_bookings INT DEFAULT 0,
    total_spent FLOAT DEFAULT 0.0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Pet table (parent class, JOINED inheritance strategy)
CREATE TABLE pet (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    user_id UNIQUEIDENTIFIER,
    pet_type VARCHAR(31) NOT NULL,
    name NVARCHAR(100),
    age INT,
    breed NVARCHAR(100),
    gender VARCHAR(10),
    special_needs NVARCHAR(500),
    is_neutered BIT,
    vaccine_status NVARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Dog table (child class, inherits Pet)
CREATE TABLE dog (
    id UNIQUEIDENTIFIER PRIMARY KEY,
    size VARCHAR(20),
    is_walk_required BIT,
    walk_frequency_per_day INT,
    training_level VARCHAR(20),
    is_friendly_with_dogs BIT,
    is_friendly_with_people BIT,
    is_friendly_with_children BIT,
    FOREIGN KEY (id) REFERENCES pet(id)
);

-- Cat table (child class, inherits Pet)
CREATE TABLE cat (
    id UNIQUEIDENTIFIER PRIMARY KEY,
    is_indoor BIT,
    litter_box_type VARCHAR(20),
    scratching_habit VARCHAR(20),
    FOREIGN KEY (id) REFERENCES pet(id)
);

-- Sitter table
CREATE TABLE sitter (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    user_id UNIQUEIDENTIFIER UNIQUE,
    name NVARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    experience NVARCHAR(500),
    average_rating FLOAT,
    rating_count INT DEFAULT 0,
    completed_bookings INT DEFAULT 0,
    hourly_rate DECIMAL(10,2) DEFAULT 200.00,
    experience_level VARCHAR(20) DEFAULT 'STANDARD',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- SitterRecord table
CREATE TABLE sitter_record (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_id UNIQUEIDENTIFIER,
    sitter_id UNIQUEIDENTIFIER,
    record_time DATETIME2,
    activity NVARCHAR(255),
    fed BIT,
    walked BIT,
    mood_status NVARCHAR(255),
    notes NVARCHAR(1000),
    photos VARCHAR(500),
    FOREIGN KEY (pet_id) REFERENCES pet(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id)
);

-- PetActivity table
CREATE TABLE pet_activity (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_id UNIQUEIDENTIFIER NOT NULL,
    activity_date DATE NOT NULL,
    walked BIT DEFAULT 0,
    walk_time DATETIME2,
    fed BIT DEFAULT 0,
    feed_time DATETIME2,
    notes NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE CASCADE
);

-- ============================================
-- 新增功能：預約系統 & 評價系統
-- ============================================

-- SitterAvailability table (保母可用時段)
CREATE TABLE sitter_availability (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    sitter_id UNIQUEIDENTIFIER NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    service_area NVARCHAR(100),
    is_active BIT DEFAULT 1,
    FOREIGN KEY (sitter_id) REFERENCES sitter(id) ON DELETE CASCADE,
    CONSTRAINT uk_sitter_availability UNIQUE (sitter_id, day_of_week, start_time, end_time)
);

-- Booking table (預約訂單)
CREATE TABLE booking (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_id UNIQUEIDENTIFIER NOT NULL,
    sitter_id UNIQUEIDENTIFIER NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    version BIGINT DEFAULT 0,
    notes NVARCHAR(500),
    sitter_response NVARCHAR(500),
    total_price FLOAT,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (pet_id) REFERENCES pet(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Booking indexes
CREATE INDEX idx_booking_sitter_time ON booking(sitter_id, start_time, end_time);
CREATE INDEX idx_booking_status ON booking(status);

-- SitterRating table (保母評價)
CREATE TABLE sitter_rating (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    booking_id UNIQUEIDENTIFIER NOT NULL UNIQUE,
    sitter_id UNIQUEIDENTIFIER NOT NULL,
    user_id UNIQUEIDENTIFIER NOT NULL,
    overall_rating INT NOT NULL,
    professionalism_rating INT,
    communication_rating INT,
    punctuality_rating INT,
    comment NVARCHAR(1000),
    sitter_reply NVARCHAR(500),
    is_anonymous BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- ============================================
-- PetPhoto table (寵物照片，LINE/Web 上傳)
-- ============================================
CREATE TABLE pet_photo (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_id UNIQUEIDENTIFIER,
    sitter_id UNIQUEIDENTIFIER NOT NULL,
    photo_url NVARCHAR(500) NOT NULL,
    message_id VARCHAR(100),
    upload_source VARCHAR(20) NOT NULL DEFAULT 'LINE',
    caption NVARCHAR(500),
    uploaded_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (pet_id) REFERENCES pet(id) ON DELETE SET NULL,
    FOREIGN KEY (sitter_id) REFERENCES sitter(id) ON DELETE CASCADE
);

CREATE INDEX idx_pet_photo_pet ON pet_photo(pet_id);
CREATE INDEX idx_pet_photo_sitter ON pet_photo(sitter_id);
CREATE INDEX idx_pet_photo_uploaded_at ON pet_photo(uploaded_at DESC);

-- ============================================
-- BookingLog table (預約日誌，用於報表/分析)
-- 注意：此表不使用外鍵約束，允許獨立存在
-- ============================================
CREATE TABLE booking_log (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    booking_id UNIQUEIDENTIFIER NOT NULL,
    pet_id UNIQUEIDENTIFIER,
    pet_name NVARCHAR(100),
    sitter_id UNIQUEIDENTIFIER,
    sitter_name NVARCHAR(100),
    user_id UNIQUEIDENTIFIER,
    username NVARCHAR(100),
    start_time DATETIME2,
    end_time DATETIME2,
    status VARCHAR(20),
    notes NVARCHAR(500),
    sitter_response NVARCHAR(500),
    total_price DECIMAL(10,2),
    booking_created_at DATETIME2,
    booking_updated_at DATETIME2,
    sync_time DATETIME2 DEFAULT GETDATE()
);

-- BookingLog indexes
CREATE INDEX idx_booking_log_booking_id ON booking_log(booking_id);
CREATE INDEX idx_booking_log_sitter_id ON booking_log(sitter_id);
CREATE INDEX idx_booking_log_user_id ON booking_log(user_id);
CREATE INDEX idx_booking_log_status ON booking_log(status);
CREATE INDEX idx_booking_log_sync_time ON booking_log(sync_time);

-- 為報表查詢建立複合索引
CREATE INDEX idx_booking_log_sitter_status ON booking_log(sitter_id, status);
CREATE INDEX idx_booking_log_time_range ON booking_log(booking_created_at, status);
