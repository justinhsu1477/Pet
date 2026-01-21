-- MSSQL Schema for Pet System
-- This script is for initial setup only, not for every restart

-- Drop tables in correct order (FK constraints)
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
    experience VARCHAR(500)
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
