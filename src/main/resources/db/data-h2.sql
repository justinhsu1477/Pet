-- H2 Database Test Data
-- 注意：H2 使用 Hibernate 預設命名（camelCase），表名區分大小寫

-- ============================================
-- 插入 Users
-- ============================================

-- admin / admin123
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123 (一般用戶/飼主)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'CUSTOMER');

-- user02 / password123 (一般用戶/飼主)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000003', 'user02', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user02@example.com', '0933-444-555', 'CUSTOMER');

-- sitter01 / sitter123 (保母帳號)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000004', 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

-- sitter02 / sitter123 (保母帳號)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000005', 'sitter02', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter02@example.com', '0933-555-666', 'SITTER');

-- sitter03 / sitter123 (保母帳號)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000006', 'sitter03', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter03@example.com', '0944-666-777', 'SITTER');

-- sitter04 / sitter123 (保母帳號)
INSERT INTO Users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000007', 'sitter04', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter04@example.com', '0955-777-888', 'SITTER');

-- ============================================
-- 插入 Customer (一般用戶詳細資料)
-- 注意: phone 和 email 已統一在 Users 表,這裡不再重複
-- ============================================

INSERT INTO Customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES ('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '王小明', '台北市信義區信義路100號', '王大明', '0912-333-444', 'SILVER', 5, 15000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO Customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES ('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', '陳小美', '台北市大安區敦化南路200號', '陳小華', '0934-555-666', 'BRONZE', 2, 6000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- ============================================
-- 插入 Pet (Dogs)
-- ============================================

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'DOG', '阿福', 5, '黃金獵犬', 'MALE', '需要每天散步兩次', true, '已完成年度疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'DOG', '皮皮', 2, '柴犬', 'FEMALE', NULL, false, '已完成基本疫苗');

INSERT INTO Dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES ('10000000-0000-0000-0000-000000000001', 'LARGE', true, 2, 'BASIC', true, true, true);

INSERT INTO Dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES ('10000000-0000-0000-0000-000000000002', 'MEDIUM', true, 3, 'INTERMEDIATE', true, true, false);

-- ============================================
-- 插入 Pet (Cats)
-- ============================================

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'CAT', '喵喵', 3, '波斯貓', 'FEMALE', '對海鮮過敏', true, '已完成年度疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'CAT', '咪咪', 4, '美國短毛貓', 'MALE', '需要定期梳毛', true, '已完成基本疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', 'CAT', '小橘', 2, '橘貓', 'MALE', NULL, false, NULL);

INSERT INTO Cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000001', true, 'COVERED', 'LOW');

INSERT INTO Cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000002', true, 'AUTOMATIC', 'MODERATE');

INSERT INTO Cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000003', false, 'OPEN', 'HIGH');

-- ============================================
-- 插入 Sitter (保母詳細資料)
-- 注意: phone 和 email 已統一在 Users 表,這裡不再重複
-- ============================================

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', '張保母', '5年寵物照護經驗,擅長照顧大型犬', 4.5, 4, 4, 220.00, 'EXPERT');

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', '李保母', '3年貓咪專業照護,有獸醫助理背景', 4.7, 3, 3, 250.00, 'SENIOR');

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000006', '王保母', '2年小型寵物照護經驗', 3.5, 2, 2, 180.00, 'STANDARD');

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000007', '陳保母', '7年全方位寵物照護,可處理特殊需求寵物', 4.7, 3, 3, 350.00, 'EXPERT');

-- ============================================
-- 插入 sitter_availability (注意：表名用小寫加底線)
-- ============================================

-- 張保母 (MON-SAT)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'MONDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'TUESDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'WEDNESDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'THURSDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'FRIDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'SATURDAY', '08:00:00', '20:00:00', '台北市大安區', true);

-- 李保母 (TUE-SUN)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'TUESDAY', '09:00:00', '18:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'WEDNESDAY', '09:00:00', '18:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'THURSDAY', '09:00:00', '18:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'FRIDAY', '09:00:00', '18:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'SATURDAY', '10:00:00', '20:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'SUNDAY', '10:00:00', '18:00:00', '台北市信義區', true);

-- 王保母 (SAT, SUN, WED)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000003', 'WEDNESDAY', '09:00:00', '18:00:00', '台北市中山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000003', 'SATURDAY', '09:00:00', '18:00:00', '台北市中山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000003', 'SUNDAY', '09:00:00', '18:00:00', '台北市中山區', true);

-- 陳保母 (MON-SUN)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'MONDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'TUESDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'WEDNESDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'THURSDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'FRIDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'SATURDAY', '08:00:00', '20:00:00', '台北市松山區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000004', 'SUNDAY', '08:00:00', '20:00:00', '台北市松山區', true);

-- ============================================
-- 插入 SitterRecord
-- ============================================

INSERT INTO SitterRecord (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '2025-01-01 09:00:00', '晨間散步', true, true, '活潑開心', '阿福今天精神很好,在公園玩得很開心', NULL);

INSERT INTO SitterRecord (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '2025-01-01 10:00:00', '餵食與梳毛', true, false, '慵懶', '喵喵今天比較想睡覺,梳毛時很配合', NULL);

-- ============================================
-- 插入 booking (注意：表名用小寫)
-- ============================================

-- 張保母的預約 (使用動態日期：當前日期前7天內)
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -7, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -7, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', '請幫忙遛狗兩次', 1540.00, DATEADD('DAY', -8, CURRENT_TIMESTAMP), DATEADD('DAY', -7, CURRENT_TIMESTAMP) + INTERVAL '19' HOUR, 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -6, CURRENT_DATE) + INTERVAL '10' HOUR, DATEADD('DAY', -6, CURRENT_DATE) + INTERVAL '17' HOUR, 'COMPLETED', '皮皮需要多運動', 1400.00, DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP) + INTERVAL '18' HOUR, 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -5, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -5, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', NULL, 1540.00, DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP) + INTERVAL '19' HOUR, 0);

-- 李保母的預約 (使用動態日期：當前日期前7天內)
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -4, CURRENT_DATE) + INTERVAL '10' HOUR, DATEADD('DAY', -4, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', '喵喵對海鮮過敏請注意', 1800.00, DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP) + INTERVAL '19' HOUR, 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', DATEADD('DAY', -3, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -3, CURRENT_DATE) + INTERVAL '17' HOUR, 'COMPLETED', '需要定期梳毛', 1800.00, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP) + INTERVAL '18' HOUR, 0);

-- 新增預約 (不同狀態、不同用戶)
-- booking 06: user01, 喵喵, 王保母, day -2, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -2, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -2, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'喵喵對海鮮過敏請留意', 1620.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP), 0);

-- booking 07: user01, 阿福, 陳保母, day -1, CONFIRMED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -1, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -1, CURRENT_DATE) + INTERVAL '18' HOUR, 'CONFIRMED', N'阿福需要遛兩次', 3150.00, DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP), 0);

-- booking 08: user01, 喵喵, 張保母, day +2, PENDING
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', 2, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', 2, CURRENT_DATE) + INTERVAL '18' HOUR, 'PENDING', N'喵喵對海鮮過敏', 1980.00, DATEADD('DAY', 1, CURRENT_TIMESTAMP), DATEADD('DAY', 2, CURRENT_TIMESTAMP), 0);

-- booking 09: user02, 小橘, 李保母, day -5, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', DATEADD('DAY', -5, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -5, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'小橘第一次寄託請多關照', 2250.00, DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP), 0);

-- booking 10: user02, 咪咪, 陳保母, day +1, PENDING
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000003', DATEADD('DAY', 1, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', 1, CURRENT_DATE) + INTERVAL '18' HOUR, 'PENDING', N'咪咪需要定期梳毛', 3150.00, DATEADD('DAY', 0, CURRENT_TIMESTAMP), DATEADD('DAY', 1, CURRENT_TIMESTAMP), 0);

-- booking 11: user01, 皮皮, 李保母, day -10, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -10, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -10, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'皮皮需要多運動', 2250.00, DATEADD('DAY', -11, CURRENT_TIMESTAMP), DATEADD('DAY', -10, CURRENT_TIMESTAMP), 0);

-- booking 12: user02, 小橘, 張保母, day -8, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003', DATEADD('DAY', -8, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -8, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'小橘活潑好動', 1980.00, DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -8, CURRENT_TIMESTAMP), 0);

-- booking 13: user01, 阿福, 張保母, day -12, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -12, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -12, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'阿福老客戶了', 1980.00, DATEADD('DAY', -13, CURRENT_TIMESTAMP), DATEADD('DAY', -12, CURRENT_TIMESTAMP), 0);

-- booking 14: user01, 喵喵, 李保母, day -9, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -9, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -9, CURRENT_DATE) + INTERVAL '18' HOUR, 'COMPLETED', N'喵喵過敏體質請注意飲食', 2250.00, DATEADD('DAY', -10, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP), 0);

-- booking 15: user01, 皮皮, 王保母, day -3, CANCELLED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000015', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', DATEADD('DAY', -3, CURRENT_DATE) + INTERVAL '9' HOUR, DATEADD('DAY', -3, CURRENT_DATE) + INTERVAL '18' HOUR, 'CANCELLED', N'臨時有事取消', 1620.00, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP), 0);

-- ============================================
-- 插入 sitter_rating (注意：表名用小寫加底線)
-- ============================================

-- 張保母的評價 (預約完成後一天)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '張保母非常專業！阿福被照顧得很好', '謝謝您的肯定！', false, DATEADD('DAY', -6, CURRENT_TIMESTAMP), DATEADD('DAY', -6, CURRENT_TIMESTAMP));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 4, 4, 5, 4, '皮皮玩得很開心', '感謝您的建議！', false, DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -5, CURRENT_TIMESTAMP));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '服務一如既往的好', NULL, false, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP));

-- 李保母的評價
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '李保母對貓咪超級有愛！', '謝謝您！', false, DATEADD('DAY', -3, CURRENT_TIMESTAMP), DATEADD('DAY', -3, CURRENT_TIMESTAMP));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 5, 5, 4, 5, '咪咪被梳得很漂亮', NULL, false, DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP));

-- 新增預約的評價
-- booking 06 評價: 王保母照顧喵喵
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 4, 4, 4, 4, '王保母照顧喵喵還不錯', '謝謝支持！', false, DATEADD('DAY', -1, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP));

-- booking 09 評價: user02 對李保母
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 5, 5, 5, 5, 'user02 對李保母很滿意', '感謝您的信任！', false, DATEADD('DAY', -4, CURRENT_TIMESTAMP), DATEADD('DAY', -4, CURRENT_TIMESTAMP));

-- booking 11 評價: 李保母照顧皮皮
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '李保母照顧皮皮很用心', '皮皮很乖！', false, DATEADD('DAY', -9, CURRENT_TIMESTAMP), DATEADD('DAY', -9, CURRENT_TIMESTAMP));

-- booking 12 評價: user02 覺得張保母不錯
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003', 4, 4, 4, 4, 'user02 覺得張保母不錯', '謝謝！', false, DATEADD('DAY', -7, CURRENT_TIMESTAMP), DATEADD('DAY', -7, CURRENT_TIMESTAMP));

-- booking 13 評價: 張保母長期服務
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '張保母長期服務很穩定', '感謝長期支持！', false, DATEADD('DAY', -11, CURRENT_TIMESTAMP), DATEADD('DAY', -11, CURRENT_TIMESTAMP));

-- booking 14 評價: 李保母對喵喵過敏處理
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 4, 4, 5, 4, '李保母對喵喵過敏體質處理得好', '喵喵很配合！', false, DATEADD('DAY', -8, CURRENT_TIMESTAMP), DATEADD('DAY', -8, CURRENT_TIMESTAMP));
