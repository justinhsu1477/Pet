-- MSSQL Test Data for Pet System (與 H2 版本完全一致)
-- 注意：使用固定 UUID 以維持參照完整性

-- ============================================
-- 插入 Users
-- ============================================

-- admin / admin123
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000001', 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123 (一般用戶/飼主)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000002', 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'CUSTOMER');

-- user02 / password123 (一般用戶/飼主)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000003', 'user02', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user02@example.com', '0933-444-555', 'CUSTOMER');

-- sitter01 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000004', 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

-- sitter02 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000005', 'sitter02', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter02@example.com', '0933-555-666', 'SITTER');

-- sitter03 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000006', 'sitter03', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter03@example.com', '0944-666-777', 'SITTER');

-- sitter04 / sitter123 (保母帳號)
INSERT INTO users (id, username, password, email, phone, role)
VALUES ('00000000-0000-0000-0000-000000000007', 'sitter04', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter04@example.com', '0955-777-888', 'SITTER');

-- ============================================
-- 插入 Customer (一般用戶詳細資料)
-- ============================================

INSERT INTO customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES ('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', N'王小明', N'台北市信義區信義路100號', N'王大明', '0912-333-444', 'SILVER', 5, 15000.0, GETDATE(), GETDATE());

INSERT INTO customer (id, user_id, name, address, emergency_contact, emergency_phone, member_level, total_bookings, total_spent, created_at, updated_at)
VALUES ('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', N'陳小美', N'台北市大安區敦化南路200號', N'陳小華', '0934-555-666', 'BRONZE', 2, 6000.0, GETDATE(), GETDATE());

-- ============================================
-- 插入 Pet (Dogs)
-- ============================================

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'DOG', N'阿福', 5, N'黃金獵犬', 'MALE', N'需要每天散步兩次', 1, N'已完成年度疫苗');

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'DOG', N'皮皮', 2, N'柴犬', 'FEMALE', NULL, 0, N'已完成基本疫苗');

INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES ('10000000-0000-0000-0000-000000000001', 'LARGE', 1, 2, 'BASIC', 1, 1, 1);

INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES ('10000000-0000-0000-0000-000000000002', 'MEDIUM', 1, 3, 'INTERMEDIATE', 1, 1, 0);

-- ============================================
-- 插入 Pet (Cats)
-- ============================================

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'CAT', N'喵喵', 3, N'波斯貓', 'FEMALE', N'對海鮮過敏', 1, N'已完成年度疫苗');

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'CAT', N'咪咪', 4, N'美國短毛貓', 'MALE', N'需要定期梳毛', 1, N'已完成基本疫苗');

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, special_needs, is_neutered, vaccine_status)
VALUES ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', 'CAT', N'小橘', 2, N'橘貓', 'MALE', NULL, 0, NULL);

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000001', 1, 'COVERED', 'LOW');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000002', 1, 'AUTOMATIC', 'MODERATE');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES ('20000000-0000-0000-0000-000000000003', 0, 'OPEN', 'HIGH');

-- ============================================
-- 插入 Sitter (保母詳細資料)
-- ============================================

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', N'張保母', N'5年寵物照護經驗,擅長照顧大型犬', 4.5, 4, 4, 220.00, 'EXPERT');

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', N'李保母', N'3年貓咪專業照護,有獸醫助理背景', 4.7, 3, 3, 250.00, 'SENIOR');

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000006', N'王保母', N'2年小型寵物照護經驗', 3.5, 2, 2, 180.00, 'STANDARD');

INSERT INTO sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings, hourly_rate, experience_level)
VALUES ('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000007', N'陳保母', N'7年全方位寵物照護,可處理特殊需求寵物', 4.7, 3, 3, 350.00, 'EXPERT');

-- ============================================
-- 插入 sitter_availability
-- ============================================

-- 張保母 (MON, TUE, WED, THU, FRI, SAT)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'MONDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'TUESDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'FRIDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000001', 'WEDNESDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000001', 'THURSDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000001', 'SATURDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

-- 李保母 (TUE, WED, THU, FRI, SAT, SUN)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', 'TUESDAY', '09:00:00', '18:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', 'SATURDAY', '10:00:00', '20:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000002', 'WEDNESDAY', '09:00:00', '18:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000002', 'THURSDAY', '09:00:00', '18:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000002', 'FRIDAY', '09:00:00', '18:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000002', 'SUNDAY', '10:00:00', '20:00:00', N'台北市信義區', 1);

-- 王保母 (SAT, SUN, WED)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000003', 'SATURDAY', '09:00:00', '18:00:00', N'台北市中山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000003', 'SUNDAY', '09:00:00', '18:00:00', N'台北市中山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000015', '30000000-0000-0000-0000-000000000003', 'WEDNESDAY', '09:00:00', '18:00:00', N'台北市中山區', 1);

-- 陳保母 (MON-SUN, all 7 days)
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000016', '30000000-0000-0000-0000-000000000004', 'MONDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000017', '30000000-0000-0000-0000-000000000004', 'TUESDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000018', '30000000-0000-0000-0000-000000000004', 'WEDNESDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000019', '30000000-0000-0000-0000-000000000004', 'THURSDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000020', '30000000-0000-0000-0000-000000000004', 'FRIDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000021', '30000000-0000-0000-0000-000000000004', 'SATURDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000022', '30000000-0000-0000-0000-000000000004', 'SUNDAY', '08:00:00', '20:00:00', N'台北市松山區', 1);

-- ============================================
-- 插入 SitterRecord
-- ============================================

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES ('60000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '2025-01-01 09:00:00', N'晨間散步', 1, 1, N'活潑開心', N'阿福今天精神很好,在公園玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES ('60000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '2025-01-01 10:00:00', N'餵食與梳毛', 1, 0, N'慵懶', N'喵喵今天比較想睡覺,梳毛時很配合', NULL);

-- ============================================
-- 插入 booking (動態日期)
-- ============================================

-- booking 01: day -7, 張保母, 阿福, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -7, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -7, GETDATE()) AS DATE)),
  'COMPLETED', N'請幫忙遛狗兩次', 1540.00,
  DATEADD(DAY, -8, GETDATE()),
  DATEADD(DAY, -7, GETDATE()),
  0);

-- booking 02: day -6, 張保母, 皮皮, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -6, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -6, GETDATE()) AS DATE)),
  'COMPLETED', N'皮皮需要多運動', 1400.00,
  DATEADD(DAY, -7, GETDATE()),
  DATEADD(DAY, -6, GETDATE()),
  0);

-- booking 03: day -5, 張保母, 阿福, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -5, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -5, GETDATE()) AS DATE)),
  'COMPLETED', NULL, 1540.00,
  DATEADD(DAY, -6, GETDATE()),
  DATEADD(DAY, -5, GETDATE()),
  0);

-- booking 04: day -4, 李保母, 喵喵, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -4, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -4, GETDATE()) AS DATE)),
  'COMPLETED', N'喵喵對海鮮過敏請注意', 1800.00,
  DATEADD(DAY, -5, GETDATE()),
  DATEADD(DAY, -4, GETDATE()),
  0);

-- booking 05: day -3, 李保母, 咪咪, user02, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -3, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -3, GETDATE()) AS DATE)),
  'COMPLETED', N'需要定期梳毛', 1800.00,
  DATEADD(DAY, -4, GETDATE()),
  DATEADD(DAY, -3, GETDATE()),
  0);

-- booking 06: day -2, 王保母, 喵喵, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000006', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -2, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -2, GETDATE()) AS DATE)),
  'COMPLETED', N'王保母照顧喵喵很細心', 1440.00,
  DATEADD(DAY, -3, GETDATE()),
  DATEADD(DAY, -2, GETDATE()),
  0);

-- booking 07: day -1, 陳保母, 阿福, user01, CONFIRMED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -1, GETDATE()) AS DATE)),
  'CONFIRMED', N'阿福需要特別注意飲食', 3150.00,
  DATEADD(DAY, -2, GETDATE()),
  DATEADD(DAY, -1, GETDATE()),
  0);

-- booking 08: day +2, 張保母, 喵喵, user01, PENDING
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, 2, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, 2, GETDATE()) AS DATE)),
  'PENDING', N'下週預約', 1980.00,
  DATEADD(DAY, 1, GETDATE()),
  DATEADD(DAY, 2, GETDATE()),
  0);

-- booking 09: day -5, 李保母, 小橘, user02, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000009', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -5, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -5, GETDATE()) AS DATE)),
  'COMPLETED', N'小橘需要多陪伴', 2000.00,
  DATEADD(DAY, -6, GETDATE()),
  DATEADD(DAY, -5, GETDATE()),
  0);

-- booking 10: day +1, 陳保母, 咪咪, user02, PENDING
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000010', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000003',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, 1, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, 1, GETDATE()) AS DATE)),
  'PENDING', NULL, 3150.00,
  DATEADD(DAY, 0, GETDATE()),
  DATEADD(DAY, 1, GETDATE()),
  0);

-- booking 11: day -10, 李保母, 皮皮, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -10, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -10, GETDATE()) AS DATE)),
  'COMPLETED', NULL, 2000.00,
  DATEADD(DAY, -11, GETDATE()),
  DATEADD(DAY, -10, GETDATE()),
  0);

-- booking 12: day -8, 張保母, 小橘, user02, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000012', '20000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -8, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -8, GETDATE()) AS DATE)),
  'COMPLETED', NULL, 1980.00,
  DATEADD(DAY, -9, GETDATE()),
  DATEADD(DAY, -8, GETDATE()),
  0);

-- booking 13: day -12, 張保母, 阿福, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000013', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -12, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -12, GETDATE()) AS DATE)),
  'COMPLETED', NULL, 1980.00,
  DATEADD(DAY, -13, GETDATE()),
  DATEADD(DAY, -12, GETDATE()),
  0);

-- booking 14: day -9, 李保母, 喵喵, user01, COMPLETED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000014', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -9, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -9, GETDATE()) AS DATE)),
  'COMPLETED', NULL, 2000.00,
  DATEADD(DAY, -10, GETDATE()),
  DATEADD(DAY, -9, GETDATE()),
  0);

-- booking 15: day -3, 王保母, 皮皮, user01, CANCELLED
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000015', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002',
  DATEADD(HOUR, 9, CAST(DATEADD(DAY, -3, GETDATE()) AS DATE)),
  DATEADD(HOUR, 18, CAST(DATEADD(DAY, -3, GETDATE()) AS DATE)),
  'CANCELLED', NULL, 1440.00,
  DATEADD(DAY, -4, GETDATE()),
  DATEADD(DAY, -3, GETDATE()),
  0);

-- ============================================
-- 插入 sitter_rating
-- ============================================

-- 張保母的評價 (預約完成後一天)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, N'張保母非常專業！阿福被照顧得很好', N'謝謝您的肯定！', 0, DATEADD(DAY, -6, GETDATE()), DATEADD(DAY, -6, GETDATE()));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 4, 4, 5, 4, N'皮皮玩得很開心', N'感謝您的建議！', 0, DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -5, GETDATE()));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, N'服務一如既往的好', NULL, 0, DATEADD(DAY, -4, GETDATE()), DATEADD(DAY, -4, GETDATE()));

-- 李保母的評價
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, N'李保母對貓咪超級有愛！', N'謝謝您！', 0, DATEADD(DAY, -3, GETDATE()), DATEADD(DAY, -3, GETDATE()));

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 5, 5, 4, 5, N'咪咪被梳得很漂亮', NULL, 0, DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, -2, GETDATE()));

-- 新增評價 (for new COMPLETED bookings)

-- booking 06: 王保母, day -2, rating at day -1
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000006', '40000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 4, 4, 4, 5, N'王保母很有耐心，喵喵很喜歡她', N'謝謝您的信任！', 0, DATEADD(DAY, -1, GETDATE()), DATEADD(DAY, -1, GETDATE()));

-- booking 09: 李保母, day -5, rating at day -4
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000007', '40000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 5, 5, 5, 4, N'小橘在李保母那邊很開心', NULL, 0, DATEADD(DAY, -4, GETDATE()), DATEADD(DAY, -4, GETDATE()));

-- booking 11: 李保母, day -10, rating at day -9
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000008', '40000000-0000-0000-0000-000000000011', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 4, 4, 5, 4, N'皮皮回來很乾淨，照顧得不錯', N'皮皮很乖巧！', 0, DATEADD(DAY, -9, GETDATE()), DATEADD(DAY, -9, GETDATE()));

-- booking 12: 張保母, day -8, rating at day -7
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000009', '40000000-0000-0000-0000-000000000012', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003', 4, 5, 4, 4, N'小橘第一次給張保母照顧，表現很好', NULL, 1, DATEADD(DAY, -7, GETDATE()), DATEADD(DAY, -7, GETDATE()));

-- booking 13: 張保母, day -12, rating at day -11
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000010', '40000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, N'阿福每次給張保母照顧都很放心', N'阿福是我最喜歡的大狗狗！', 0, DATEADD(DAY, -11, GETDATE()), DATEADD(DAY, -11, GETDATE()));

-- booking 14: 李保母, day -9, rating at day -8
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at, updated_at)
VALUES ('70000000-0000-0000-0000-000000000011', '40000000-0000-0000-0000-000000000014', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 5, 5, 4, 5, N'喵喵的過敏問題李保母處理得很好', N'會特別注意喵喵的飲食！', 0, DATEADD(DAY, -8, GETDATE()), DATEADD(DAY, -8, GETDATE()));
