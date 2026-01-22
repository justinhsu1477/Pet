-- PostgreSQL Schema for Log DB
-- Booking Log 表，用於存放 Booking 完整複製，供報表/分析使用

CREATE TABLE IF NOT EXISTS booking_log (
    id UUID PRIMARY KEY,
    booking_id UUID NOT NULL,
    pet_id UUID,
    pet_name VARCHAR(100),
    sitter_id UUID,
    sitter_name VARCHAR(100),
    user_id UUID,
    username VARCHAR(100),
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status VARCHAR(20),
    notes VARCHAR(500),
    sitter_response VARCHAR(500),
    total_price DECIMAL(10,2),
    booking_created_at TIMESTAMP,
    booking_updated_at TIMESTAMP,
    sync_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 建立索引
CREATE INDEX IF NOT EXISTS idx_booking_log_booking_id ON booking_log(booking_id);
CREATE INDEX IF NOT EXISTS idx_booking_log_sitter_id ON booking_log(sitter_id);
CREATE INDEX IF NOT EXISTS idx_booking_log_user_id ON booking_log(user_id);
CREATE INDEX IF NOT EXISTS idx_booking_log_status ON booking_log(status);
CREATE INDEX IF NOT EXISTS idx_booking_log_sync_time ON booking_log(sync_time);

-- 為報表查詢建立複合索引
CREATE INDEX IF NOT EXISTS idx_booking_log_sitter_status ON booking_log(sitter_id, status);
CREATE INDEX IF NOT EXISTS idx_booking_log_time_range ON booking_log(booking_created_at, status);
