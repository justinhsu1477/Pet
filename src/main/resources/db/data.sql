-- 插入 User 測試資料（密碼已使用 BCrypt 加密）
-- admin / admin123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (RANDOM_UUID(), 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (RANDOM_UUID(), 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'USER');

-- sitter01 / sitter123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (RANDOM_UUID(), 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

-- 插入 Dog 測試資料 (使用 JOINED 繼承策略)
SET @dog1_id = RANDOM_UUID();
SET @dog2_id = RANDOM_UUID();

-- 先插入 Pet 父類別資料
INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog1_id, 'DOG', '阿福', 5, '黃金獵犬', 'MALE', '王小明', '0912-345-678', '需要每天散步兩次', true, '已完成年度疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog2_id, 'DOG', '皮皮', 2, '柴犬', 'FEMALE', '陳小美', '0945-678-901', NULL, false, '已完成基本疫苗');

-- 再插入 Dog 子類別資料
INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog1_id, 'LARGE', true, 2, 'BASIC', true, true, true);

INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog2_id, 'MEDIUM', true, 3, 'INTERMEDIATE', true, true, false);

-- 插入 Cat 測試資料
SET @cat1_id = RANDOM_UUID();
SET @cat2_id = RANDOM_UUID();
SET @cat3_id = RANDOM_UUID();

-- 先插入 Pet 父類別資料
INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat1_id, 'CAT', '喵喵', 3, '波斯貓', 'FEMALE', '李小華', '0923-456-789', '對海鮮過敏', true, '已完成年度疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat2_id, 'CAT', '咪咪', 4, '美國短毛貓', 'MALE', '林大明', '0956-789-012', '需要定期梳毛', true, '已完成基本疫苗');

INSERT INTO pet (id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat3_id, 'CAT', '小橘', 2, '橘貓', 'MALE', '張小英', '0967-890-123', NULL, false, NULL);

-- 再插入 Cat 子類別資料
INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat1_id, true, 'COVERED', 'LOW');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat2_id, true, 'AUTOMATIC', 'MODERATE');

INSERT INTO cat (id, is_indoor, litter_box_type, scratching_habit)
VALUES (@cat3_id, false, 'OPEN', 'HIGH');

-- 設定用於 SitterRecord 的變數 (使用已存在的寵物)
SET @pet1_id = @dog1_id;
SET @pet2_id = @cat1_id;
SET @pet4_id = @dog2_id;
SET @pet5_id = @cat2_id;

-- 插入 Sitter 測試資料 (先存儲 UUID 以便在 SitterRecord 中引用)
SET @sitter1_id = RANDOM_UUID();
SET @sitter2_id = RANDOM_UUID();
SET @sitter3_id = RANDOM_UUID();
SET @sitter4_id = RANDOM_UUID();

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter1_id, '張保母', '0911-111-222', 'zhang@example.com', '5年寵物照護經驗，擅長照顧大型犬');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter2_id, '李保母', '0922-222-333', 'lee@example.com', '3年貓咪專業照護，有獸醫助理背景');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter3_id, '王保母', '0933-333-444', 'wang@example.com', '2年小型寵物照護經驗');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter4_id, '陳保母', '0944-444-555', 'chen@example.com', '7年全方位寵物照護，可處理特殊需求寵物');

-- 插入 SitterRecord 測試資料
INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet1_id, @sitter1_id, '2025-01-01 09:00:00', '晨間散步', true, true, '活潑開心', '阿福今天精神很好，在公園玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet1_id, @sitter1_id, '2025-01-01 18:00:00', '晚餐時間', true, false, '正常', '食慾良好，吃完整碗飼料', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet2_id, @sitter2_id, '2025-01-01 10:00:00', '餵食與梳毛', true, false, '慵懶', '喵喵今天比較想睡覺，梳毛時很配合', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @cat3_id, @sitter3_id, '2025-01-01 08:30:00', '早餐與清潔', true, false, '活潑', '小橘今天精神很好，活動力很足', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet4_id, @sitter4_id, '2025-01-01 14:00:00', '下午散步', true, true, '興奮', '皮皮在公園遇到其他柴犬，玩得很開心', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet5_id, @sitter2_id, '2025-01-01 16:00:00', '梳毛與互動', true, false, '親人', '咪咪今天特別黏人，梳了很多毛', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet1_id, @sitter1_id, '2025-01-02 09:30:00', '晨間散步', true, true, '活潑開心', '今天遇到鄰居的狗狗，互相打招呼', NULL);

INSERT INTO sitter_record (id, pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (RANDOM_UUID(), @pet2_id, @sitter2_id, '2025-01-02 11:00:00', '遊戲時間', true, false, '活潑', '用逗貓棒跟喵喵玩了30分鐘', NULL);