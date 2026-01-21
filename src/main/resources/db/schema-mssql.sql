-- MSSQL Schema for Pet System
-- This script is for initial setup only, not for every restart

-- Drop tables in correct order (FK constraints)
IF OBJECT_ID('sitter_rating', 'U') IS NOT NULL DROP TABLE sitter_rating;
IF OBJECT_ID('booking', 'U') IS NOT NULL DROP TABLE booking;
IF OBJECT_ID('sitter_availability', 'U') IS NOT NULL DROP TABLE sitter_availability;
IF OBJECT_ID('pet_activity', 'U') IS NOT NULL DROP TABLE pet_activity;
IF OBJECT_ID('sitter_record', 'U') IS NOT NULL DROP TABLE sitter_record;
IF OBJECT_ID('dog', 'U') IS NOT NULL DROP TABLE dog;
IF OBJECT_ID('cat', 'U') IS NOT NULL DROP TABLE cat;
IF OBJECT_ID('pet', 'U') IS NOT NULL DROP TABLE pet;
IF OBJECT_ID('sitter', 'U') IS NOT NULL DROP TABLE sitter;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;

-- Users table
CREATE TABLE users (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20)
);

-- Pet table (parent class, JOINED inheritance strategy)
CREATE TABLE pet (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_type VARCHAR(31) NOT NULL,
    name VARCHAR(100),
    age INT,
    breed VARCHAR(100),
    gender VARCHAR(10),
    owner_name VARCHAR(100),
    owner_phone VARCHAR(20),
    special_needs VARCHAR(500),
    is_neutered BIT,
    vaccine_status VARCHAR(255)
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
    name VARCHAR(255),
    phone VARCHAR(255),
    email VARCHAR(255),
    experience VARCHAR(500),
    average_rating FLOAT,
    rating_count INT DEFAULT 0,
    completed_bookings INT DEFAULT 0
);

-- SitterRecord table
CREATE TABLE sitter_record (
    id UNIQUEIDENTIFIER DEFAULT NEWID() PRIMARY KEY,
    pet_id UNIQUEIDENTIFIER,
    sitter_id UNIQUEIDENTIFIER,
    record_time DATETIME2,
    activity VARCHAR(255),
    fed BIT,
    walked BIT,
    mood_status VARCHAR(255),
    notes VARCHAR(1000),
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
    notes VARCHAR(500),
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
    service_area VARCHAR(100),
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
    notes VARCHAR(500),
    sitter_response VARCHAR(500),
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
    comment VARCHAR(1000),
    sitter_reply VARCHAR(500),
    is_anonymous BIT DEFAULT 0,
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (booking_id) REFERENCES booking(id),
    FOREIGN KEY (sitter_id) REFERENCES sitter(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
