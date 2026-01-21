-- MSSQL Test Data for Pet System

-- Insert User test data (password encrypted with BCrypt)
-- admin / admin123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (NEWID(), 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (NEWID(), 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'USER');

-- sitter01 / sitter123
INSERT INTO users (id, username, password, email, phone, role)
VALUES (NEWID(), 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

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

-- Declare variables for Sitter IDs
DECLARE @sitter1_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter2_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter3_id UNIQUEIDENTIFIER = NEWID();
DECLARE @sitter4_id UNIQUEIDENTIFIER = NEWID();

-- Insert Sitter test data
INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter1_id, N'張保母', '0911-111-222', 'zhang@example.com', N'5年寵物照護經驗，擅長照顧大型犬');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter2_id, N'李保母', '0922-222-333', 'lee@example.com', N'3年貓咪專業照護，有獸醫助理背景');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter3_id, N'王保母', '0933-333-444', 'wang@example.com', N'2年小型寵物照護經驗');

INSERT INTO sitter (id, name, phone, email, experience)
VALUES (@sitter4_id, N'陳保母', '0944-444-555', 'chen@example.com', N'7年全方位寵物照護，可處理特殊需求寵物');

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
