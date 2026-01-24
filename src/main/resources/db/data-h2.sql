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

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
VALUES ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'DOG', '阿福', 5, '黃金獵犬', 'MALE', '王小明', '0912-345-678', '需要每天散步兩次', true, '已完成年度疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
VALUES ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'DOG', '皮皮', 2, '柴犬', 'FEMALE', '陳小美', '0945-678-901', NULL, false, '已完成基本疫苗');

INSERT INTO Dog (id, size, isWalkRequired, walkFrequencyPerDay, trainingLevel, isFriendlyWithDogs, isFriendlyWithPeople, isFriendlyWithChildren)
VALUES ('10000000-0000-0000-0000-000000000001', 'LARGE', true, 2, 'BASIC', true, true, true);

INSERT INTO Dog (id, size, isWalkRequired, walkFrequencyPerDay, trainingLevel, isFriendlyWithDogs, isFriendlyWithPeople, isFriendlyWithChildren)
VALUES ('10000000-0000-0000-0000-000000000002', 'MEDIUM', true, 3, 'INTERMEDIATE', true, true, false);

-- ============================================
-- 插入 Pet (Cats)
-- ============================================

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
VALUES ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 'CAT', '喵喵', 3, '波斯貓', 'FEMALE', '李小華', '0923-456-789', '對海鮮過敏', true, '已完成年度疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
VALUES ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 'CAT', '咪咪', 4, '美國短毛貓', 'MALE', '林大明', '0956-789-012', '需要定期梳毛', true, '已完成基本疫苗');

INSERT INTO Pet (id, user_id, pet_type, name, age, breed, gender, ownerName, ownerPhone, specialNeeds, isNeutered, vaccineStatus)
VALUES ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003', 'CAT', '小橘', 2, '橘貓', 'MALE', '張小英', '0967-890-123', NULL, false, NULL);

INSERT INTO Cat (id, isIndoor, litterBoxType, scratchingHabit)
VALUES ('20000000-0000-0000-0000-000000000001', true, 'COVERED', 'LOW');

INSERT INTO Cat (id, isIndoor, litterBoxType, scratchingHabit)
VALUES ('20000000-0000-0000-0000-000000000002', true, 'AUTOMATIC', 'MODERATE');

INSERT INTO Cat (id, isIndoor, litterBoxType, scratchingHabit)
VALUES ('20000000-0000-0000-0000-000000000003', false, 'OPEN', 'HIGH');

-- ============================================
-- 插入 Sitter (保母詳細資料)
-- 注意: phone 和 email 已統一在 Users 表,這裡不再重複
-- ============================================

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000004', '張保母', '5年寵物照護經驗,擅長照顧大型犬', 4.5, 4, 4);

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000005', '李保母', '3年貓咪專業照護,有獸醫助理背景', 4.7, 3, 3);

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES ('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000006', '王保母', '2年小型寵物照護經驗', 3.5, 2, 2);

INSERT INTO Sitter (id, user_id, name, experience, average_rating, rating_count, completed_bookings)
VALUES ('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000007', '陳保母', '7年全方位寵物照護,可處理特殊需求寵物', 4.7, 3, 3);

-- ============================================
-- 插入 sitter_availability (注意：表名用小寫加底線)
-- ============================================

-- 張保母
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, serviceArea, isActive)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'MONDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, serviceArea, isActive)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'TUESDAY', '08:00:00', '20:00:00', '台北市大安區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, serviceArea, isActive)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000001', 'FRIDAY', '08:00:00', '20:00:00', '台北市大安區', true);

-- 李保母
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, serviceArea, isActive)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'TUESDAY', '09:00:00', '18:00:00', '台北市信義區', true);

INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, serviceArea, isActive)
VALUES (RANDOM_UUID(), '30000000-0000-0000-0000-000000000002', 'SATURDAY', '10:00:00', '20:00:00', '台北市信義區', true);

-- ============================================
-- 插入 SitterRecord
-- ============================================

INSERT INTO SitterRecord (id, pet_id, sitter_id, recordTime, activity, fed, walked, moodStatus, notes, photos)
VALUES (RANDOM_UUID(), '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '2025-01-01 09:00:00', '晨間散步', true, true, '活潑開心', '阿福今天精神很好,在公園玩得很開心', NULL);

INSERT INTO SitterRecord (id, pet_id, sitter_id, recordTime, activity, fed, walked, moodStatus, notes, photos)
VALUES (RANDOM_UUID(), '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '2025-01-01 10:00:00', '餵食與梳毛', true, false, '慵懶', '喵喵今天比較想睡覺,梳毛時很配合', NULL);

-- ============================================
-- 插入 booking (注意：表名用小寫)
-- ============================================

-- 張保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, totalPrice, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-05 09:00:00', '2025-01-05 18:00:00', 'COMPLETED', '請幫忙遛狗兩次', 800.00, '2025-01-04 10:00:00', '2025-01-05 19:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, totalPrice, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-08 10:00:00', '2025-01-08 17:00:00', 'COMPLETED', '皮皮需要多運動', 700.00, '2025-01-07 14:00:00', '2025-01-08 18:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, totalPrice, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', '2025-01-12 09:00:00', '2025-01-12 18:00:00', 'COMPLETED', NULL, 800.00, '2025-01-11 08:00:00', '2025-01-12 19:00:00', 0);

-- 李保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, totalPrice, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', '2025-01-06 10:00:00', '2025-01-06 18:00:00', 'COMPLETED', '喵喵對海鮮過敏請注意', 750.00, '2025-01-05 15:00:00', '2025-01-06 19:00:00', 0);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, totalPrice, created_at, updated_at, version)
VALUES ('40000000-0000-0000-0000-000000000005', '20000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', '2025-01-10 09:00:00', '2025-01-10 17:00:00', 'COMPLETED', '需要定期梳毛', 700.00, '2025-01-09 12:00:00', '2025-01-10 18:00:00', 0);

-- ============================================
-- 插入 sitter_rating (注意：表名用小寫加底線)
-- ============================================

-- 張保母的評價
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overallRating, professionalismRating, communicationRating, punctualityRating, comment, sitterReply, isAnonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '張保母非常專業！阿福被照顧得很好', '謝謝您的肯定！', false, '2025-01-06 10:30:00', '2025-01-06 10:30:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overallRating, professionalismRating, communicationRating, punctualityRating, comment, sitterReply, isAnonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 4, 4, 5, 4, '皮皮玩得很開心', '感謝您的建議！', false, '2025-01-09 14:00:00', '2025-01-09 14:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overallRating, professionalismRating, communicationRating, punctualityRating, comment, sitterReply, isAnonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '服務一如既往的好', NULL, false, '2025-01-13 09:00:00', '2025-01-13 09:00:00');

-- 李保母的評價
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overallRating, professionalismRating, communicationRating, punctualityRating, comment, sitterReply, isAnonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 5, 5, 5, 5, '李保母對貓咪超級有愛！', '謝謝您！', false, '2025-01-07 19:00:00', '2025-01-07 19:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overallRating, professionalismRating, communicationRating, punctualityRating, comment, sitterReply, isAnonymous, created_at, updated_at)
VALUES (RANDOM_UUID(), '40000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000003', 5, 5, 4, 5, '咪咪被梳得很漂亮', NULL, false, '2025-01-11 18:30:00', '2025-01-11 18:30:00');
