-- MSSQL Test Data for Pet System

-- ============================================
-- Declare User IDs first
-- ============================================
DECLARE @admin_id UNIQUEIDENTIFIER = NEWID();
DECLARE @user01_id UNIQUEIDENTIFIER = NEWID();
DECLARE @user02_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter01_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter02_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter03_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter04_id UNIQUEIDENTIFIER = NEWID();

-- ============================================
-- Insert User test data (password encrypted with BCrypt)
-- ============================================

-- admin / admin123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@admin_id, 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123 (一般用戶/飼主)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@user01_id, 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'CUSTOMER');

-- user02 / password123 (一般用戶/飼主)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@user02_id, 'user02', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user02@example.com', '0933-444-555', 'CUSTOMER');

-- sitter01 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@sitter01_id, 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

-- sitter02 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@sitter02_id, 'sitter02', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter02@example.com', '0933-555-666', 'SITTER');

-- sitter03 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@sitter03_id, 'sitter03', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter03@example.com', '0944-666-777', 'SITTER');

-- sitter04 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@sitter04_id, 'sitter04', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter04@example.com', '0955-777-888', 'SITTER');

-- ============================================
-- Insert Customer (一般用戶詳細資料)
-- 注意: phone 和 email 已統一在 users 表,這裡不再重複
-- ============================================

INSERT INTO customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES (NEWID(), @user01_id, N'王小明', N'台北市信義區信義路100號', N'王大明', '0912-333-444', 'SILVER', 5, 15000.0, GETDATE(), GETDATE());

INSERT INTO customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES (NEWID(), @user02_id, N'陳小美', N'台北市大安區敦化南路200號', N'陳小華', '0934-555-666', 'BRONZE', 2, 6000.0, GETDATE(), GETDATE());

-- Declare variables for Pet IDs
DECLARE @dog1_id UNIQUEIDENTIFIER = NEWID();
DECLARE @dog2_id UNIQUEIDENTIFIER = NEWID();
DECLARE @cat1_id UNIQUEIDENTIFIER = NEWID();
DECLARE @cat2_id UNIQUEIDENTIFIER = NEWID();
DECLARE @cat3_id UNIQUEIDENTIFIER = NEWID();

-- Insert Dog test data (JOINED inheritance strategy)
-- First insert Pet parent class data
INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog1_id, 'DOG', N'阿福', 5, N'黃金獵犬', 'MALE', N'王小明', '0912-345-678', N'需要每天散步兩次', 1, N'已完成年度疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog2_id, 'DOG', N'皮皮', 2, N'柴犬', 'FEMALE', N'陳小美', '0945-678-901', NULL, 0, N'已完成基本疫苗');

-- Then insert Dog child class data
INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog1_id, 'LARGE', 1, 2, 'BASIC', 1, 1, 1);

INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog2_id, 'MEDIUM', 1, 3, 'INTERMEDIATE', 1, 1, 0);

-- Insert Cat test data
-- First insert Pet parent class data
INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat1_id, 'CAT', N'喵喵', 3, N'波斯貓', 'FEMALE', N'李小華', '0923-456-789', N'對海鮮過敏', 1, N'已完成年度疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat2_id, 'CAT', N'咪咪', 4, N'美國短毛貓', 'MALE', N'林大明', '0956-789-012', N'需要定期梳毛', 1, N'已完成基本疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat3_id, 'CAT', N'小橘', 2, N'橘貓', 'MALE', N'張小英', '0967-890-123', NULL, 0, NULL);

-- Then insert Cat child class data
INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat1_id, 1, 'COVERED', 'LOW');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat2_id, 1, 'AUTOMATIC', 'MODERATE');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat3_id, 0, 'OPEN', 'HIGH');

-- ============================================
-- Insert Sitter (保母詳細資料)
-- 注意: phone 和 email 已統一在 users 表,這裡不再重複
-- ============================================

-- Declare variables for Sitter entity IDs
DECLARE @sitter1_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter2_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter3_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter4_id UNIQUEIDENTIFIER = NEWID();

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES (@sitter1_id, @sitter01_id, N'張保母', N'5年寵物照護經驗，擅長照顧大型犬', 4.8, 15, 12);

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES (@sitter2_id, @sitter02_id, N'李保母', N'3年貓咪專業照護，有獸醫助理背景', 4.9, 20, 18);

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES (@sitter3_id, @sitter03_id, N'王保母', N'2年小型寵物照護經驗', 4.5, 8, 6);

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES (@sitter4_id, @sitter04_id, N'陳保母', N'7年全方位寵物照護，可處理特殊需求寵物', 5.0, 25, 22);

-- Insert SitterRecord test data
INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @dog1_id, @sitter1_id, '2025-01-01 09:00:00', N'晨間散步', 1, 1, N'活潑開心', N'阿福今天精神很好，在公園玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @dog1_id, @sitter1_id, '2025-01-01 18:00:00', N'晚餐時間', 1, 0, N'正常', N'食慾良好，吃完整碗飼料', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @cat1_id, @sitter2_id, '2025-01-01 10:00:00', N'餵食與梳毛', 1, 0, N'慵懶', N'喵喵今天比較想睡覺，梳毛時很配合', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @cat3_id, @sitter3_id, '2025-01-01 08:30:00', N'早餐與清潔', 1, 0, N'活潑', N'小橘今天精神很好，活動力很足', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @dog2_id, @sitter4_id, '2025-01-01 14:00:00', N'下午散步', 1, 1, N'興奮', N'皮皮在公園遇到其他柴犬，玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @cat2_id, @sitter2_id, '2025-01-01 16:00:00', N'梳毛與互動', 1, 0, N'親人', N'咪咪今天特別黏人，梳了很多毛', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @dog1_id, @sitter1_id, '2025-01-02 09:30:00', N'晨間散步', 1, 1, N'活潑開心', N'今天遇到鄰居的狗狗，互相打招呼', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (NEWID(), @cat1_id, @sitter2_id, '2025-01-02 11:00:00', N'遊戲時間', 1, 0, N'活潑', N'用逗貓棒跟喵喵玩了30分鐘', NULL);
