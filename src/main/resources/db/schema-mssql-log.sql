-- MSSQL Schema for Log Database (petdb_log)
-- This script creates the booking_log table for reporting and analysis

-- Drop table if exists
IF OBJECT_ID('booking_log', 'U') IS NOT NULL DROP TABLE booking_log;

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
