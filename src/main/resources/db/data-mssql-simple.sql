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

-- 張保母
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'MONDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'TUESDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'FRIDAY', '08:00:00', '20:00:00', N'台北市大安區', 1);

-- 李保母
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', 'TUESDAY', '09:00:00', '18:00:00', N'台北市信義區', 1);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES ('50000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', 'SATURDAY', '10:00:00', '20:00:00', N'台北市信義區', 1);

-- ============================================
-- 插入 SitterRecord
-- ============================================

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES ('60000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '2025-01-01 09:00:00', N'晨間散步', 1, 1, N'活潑開心', N'阿福今天精神很好,在公園玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES ('60000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '2025-01-01 10:00:00', N'餵食與梳毛', 1, 0, N'慵懶', N'喵喵今天比較想睡覺,梳毛時很配合', NULL);

-- ============================================
-- 插入 booking
-- ============================================

-- 張保母的預約 (靜態日期：2025-01-19 ~ 2025-01-23，最近7天內)
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-19 09:00:00', '2025-01-19 18:00:00', 'COMPLETED', N'請幫忙遛狗兩次', 1540.00, '2025-01-18 10:00:00', '2025-01-19 19:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-20 10:00:00', '2025-01-20 17:00:00', 'COMPLETED', N'皮皮需要多運動', 1400.00, '2025-01-19 10:00:00', '2025-01-20 18:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-21 09:00:00', '2025-01-21 18:00:00', 'COMPLETED', NULL, 1540.00, '2025-01-20 10:00:00', '2025-01-21 19:00:00', 0);

-- 李保母的預約 (靜態日期：2025-01-22 ~ 2025-01-23，最近7天內)
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', '2025-01-22 10:00:00', '2025-01-22 18:00:00', 'COMPLETED', N'喵喵對海鮮過敏請注意', 1800.00, '2025-01-21 10:00:00', '2025-01-22 19:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', '2025-01-23 09:00:00', '2025-01-23 17:00:00', 'COMPLETED', N'需要定期梳毛', 1800.00, '2025-01-22 10:00:00', '2025-01-23 18:00:00', 0);

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
