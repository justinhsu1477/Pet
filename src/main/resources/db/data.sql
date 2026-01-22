-- 插入 User 測試資料（密碼已使用 BCrypt 加密）
-- admin / admin123
SET @admin_id = RANDOM_UUID();
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@admin_id, 'admin', '$2a$10$ioaaP9Nongaib.l8BoFY1ehNWJBG7OvBW92w4/O1oADnWMF6PrY8O', 'admin@petcare.com', '0900-000-001', 'ADMIN');

-- user01 / password123
SET @user01_id = RANDOM_UUID();
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@user01_id, 'user01', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user01@example.com', '0911-222-333', 'USER');

-- user02 / password123 (另一個使用者，有自己的寵物)
SET @user02_id = RANDOM_UUID();
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@user02_id, 'user02', '$2a$10$6yBjB3V4XgBrAH04Ygo5M.hPdPg9f.G6I8IhQeZSRl79sAVgY6Nmi', 'user02@example.com', '0933-444-555', 'USER');

-- sitter01 / sitter123
SET @sitter_user_id = RANDOM_UUID();
INSERT INTO users (id, username, password, email, phone, role)
VALUES (@sitter_user_id, 'sitter01', '$2a$10$n3COcyIq.RwDo0jyMfYV1etGu47L4/2eRyyJ6UR9cBCYhSBdiytDS', 'sitter01@example.com', '0922-333-444', 'SITTER');

-- 插入 Dog 測試資料 (使用 JOINED 繼承策略)
SET @dog1_id = RANDOM_UUID();
SET @dog2_id = RANDOM_UUID();

-- 先插入 Pet 父類別資料（user01 的寵物）
INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog1_id, @user01_id, 'DOG', '阿福', 5, '黃金獵犬', 'MALE', '王小明', '0912-345-678', '需要每天散步兩次', true, '已完成年度疫苗');

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@dog2_id, @user01_id, 'DOG', '皮皮', 2, '柴犬', 'FEMALE', '陳小美', '0945-678-901', NULL, false, '已完成基本疫苗');

-- 再插入 Dog 子類別資料
INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog1_id, 'LARGE', true, 2, 'BASIC', true, true, true);

INSERT INTO dog (id, size, is_walk_required, walk_frequency_per_day, training_level, is_friendly_with_dogs, is_friendly_with_people, is_friendly_with_children)
VALUES (@dog2_id, 'MEDIUM', true, 3, 'INTERMEDIATE', true, true, false);

-- 插入 Cat 測試資料
SET @cat1_id = RANDOM_UUID();
SET @cat2_id = RANDOM_UUID();
SET @cat3_id = RANDOM_UUID();

-- 先插入 Pet 父類別資料（user01 的寵物）
INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat1_id, @user01_id, 'CAT', '喵喵', 3, '波斯貓', 'FEMALE', '李小華', '0923-456-789', '對海鮮過敏', true, '已完成年度疫苗');

-- user02 的寵物
INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat2_id, @user02_id, 'CAT', '咪咪', 4, '美國短毛貓', 'MALE', '林大明', '0956-789-012', '需要定期梳毛', true, '已完成基本疫苗');

INSERT INTO pet (id, user_id, pet_type, name, age, breed, gender, owner_name, owner_phone, special_needs, is_neutered, vaccine_status)
VALUES (@cat3_id, @user02_id, 'CAT', '小橘', 2, '橘貓', 'MALE', '張小英', '0967-890-123', NULL, false, NULL);

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

-- ============================================
-- 插入保母可用時段 (SitterAvailability)
-- ============================================

-- 張保母：週一到週五全天、週六上午
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'MONDAY', '08:00', '20:00', '台北市大安區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'TUESDAY', '08:00', '20:00', '台北市大安區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'WEDNESDAY', '08:00', '20:00', '台北市大安區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'THURSDAY', '08:00', '20:00', '台北市大安區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'FRIDAY', '08:00', '20:00', '台北市大安區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter1_id, 'SATURDAY', '09:00', '14:00', '台北市大安區', true);

-- 李保母：週二到週日
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'TUESDAY', '09:00', '18:00', '台北市信義區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'WEDNESDAY', '09:00', '18:00', '台北市信義區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'THURSDAY', '09:00', '18:00', '台北市信義區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'FRIDAY', '09:00', '18:00', '台北市信義區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'SATURDAY', '10:00', '20:00', '台北市信義區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter2_id, 'SUNDAY', '10:00', '20:00', '台北市信義區', true);

-- 王保母：週末全天
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter3_id, 'SATURDAY', '08:00', '22:00', '台北市中山區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter3_id, 'SUNDAY', '08:00', '22:00', '台北市中山區', true);

-- 陳保母：每天都可用（全年無休）
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'MONDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'TUESDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'WEDNESDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'THURSDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'FRIDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'SATURDAY', '07:00', '22:00', '台北市全區', true);
INSERT INTO sitter_availability (id, sitter_id, day_of_week, start_time, end_time, service_area, is_active)
VALUES (RANDOM_UUID(), @sitter4_id, 'SUNDAY', '07:00', '22:00', '台北市全區', true);

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

-- ============================================
-- 假資料：預約紀錄 (Booking)
-- ============================================

-- 建立預約紀錄（使用上面已定義的 @user01_id）
SET @booking1_id = RANDOM_UUID();
SET @booking2_id = RANDOM_UUID();
SET @booking3_id = RANDOM_UUID();
SET @booking4_id = RANDOM_UUID();
SET @booking5_id = RANDOM_UUID();
SET @booking6_id = RANDOM_UUID();
SET @booking7_id = RANDOM_UUID();
SET @booking8_id = RANDOM_UUID();
SET @booking9_id = RANDOM_UUID();
SET @booking10_id = RANDOM_UUID();
SET @booking11_id = RANDOM_UUID();
SET @booking12_id = RANDOM_UUID();

-- 張保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking1_id, @pet1_id, @sitter1_id, @user01_id, '2025-01-05 09:00:00', '2025-01-05 18:00:00', 'COMPLETED', '請幫忙遛狗兩次', 800.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking2_id, @pet4_id, @sitter1_id, @user01_id, '2025-01-08 10:00:00', '2025-01-08 17:00:00', 'COMPLETED', '皮皮需要多運動', 700.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking3_id, @pet1_id, @sitter1_id, @user01_id, '2025-01-12 09:00:00', '2025-01-12 18:00:00', 'COMPLETED', NULL, 800.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking4_id, @pet4_id, @sitter1_id, @user01_id, '2025-01-15 08:00:00', '2025-01-15 20:00:00', 'COMPLETED', '長時間照顧', 1200.00);

-- 李保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking5_id, @pet2_id, @sitter2_id, @user01_id, '2025-01-06 10:00:00', '2025-01-06 18:00:00', 'COMPLETED', '喵喵對海鮮過敏請注意', 750.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking6_id, @pet5_id, @sitter2_id, @user01_id, '2025-01-10 09:00:00', '2025-01-10 17:00:00', 'COMPLETED', '需要定期梳毛', 700.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking7_id, @pet2_id, @sitter2_id, @user01_id, '2025-01-14 10:00:00', '2025-01-14 18:00:00', 'COMPLETED', NULL, 750.00);

-- 王保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking8_id, @cat3_id, @sitter3_id, @user01_id, '2025-01-07 09:00:00', '2025-01-07 17:00:00', 'COMPLETED', '小橘活潑好動', 650.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking9_id, @cat3_id, @sitter3_id, @user01_id, '2025-01-11 10:00:00', '2025-01-11 16:00:00', 'COMPLETED', NULL, 550.00);

-- 陳保母的預約
INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking10_id, @pet1_id, @sitter4_id, @user01_id, '2025-01-09 08:00:00', '2025-01-09 20:00:00', 'COMPLETED', '阿福需要特別注意', 1000.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking11_id, @pet2_id, @sitter4_id, @user01_id, '2025-01-13 09:00:00', '2025-01-13 18:00:00', 'COMPLETED', '喵喵過敏體質', 850.00);

INSERT INTO booking (id, pet_id, sitter_id, user_id, start_time, end_time, status, notes, total_price)
VALUES (@booking12_id, @pet4_id, @sitter4_id, @user01_id, '2025-01-16 09:00:00', '2025-01-16 18:00:00', 'COMPLETED', NULL, 800.00);

-- ============================================
-- 假資料：保母評價 (SitterRating)
-- ============================================

-- 張保母的評價 (4筆，平均 4.5)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking1_id, @sitter1_id, @user01_id, 5, 5, 5, 5, '張保母非常專業！阿福被照顧得很好，每次遛狗都拍照回報，讓我很放心。下次還會再預約！', '謝謝您的肯定！阿福很乖很可愛，期待下次再見！', false, '2025-01-06 10:30:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking2_id, @sitter1_id, @user01_id, 4, 4, 5, 4, '皮皮玩得很開心，保母很有耐心。唯一小建議是希望遛狗時間可以再長一點。', '感謝您的建議！下次會注意讓皮皮多運動一些。', false, '2025-01-09 14:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking3_id, @sitter1_id, @user01_id, 5, 5, 5, 5, '服務一如既往的好，很推薦給有大型犬的飼主！', NULL, false, '2025-01-13 09:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking4_id, @sitter1_id, @user01_id, 4, 4, 4, 5, '長時間照顧也很細心，皮皮回來精神很好。', '謝謝您的信任，照顧皮皮是我的榮幸！', false, '2025-01-16 20:30:00');

-- 李保母的評價 (3筆，平均 4.7)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking5_id, @sitter2_id, @user01_id, 5, 5, 5, 5, '李保母對貓咪超級有愛！喵喵平常很怕生，但保母很有技巧地讓牠放鬆。而且完全沒有餵到過敏的食物，非常細心！', '喵喵真的很可愛！照顧牠是我的榮幸，期待下次見面～', false, '2025-01-07 19:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking6_id, @sitter2_id, @user01_id, 5, 5, 4, 5, '咪咪被梳得很漂亮，保母還拍了很多可愛的照片給我看。專業又貼心！', NULL, false, '2025-01-11 18:30:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking7_id, @sitter2_id, @user01_id, 4, 4, 4, 4, '服務很好，貓咪照顧得不錯。', '感謝您的評價，期待再次為您服務！', false, '2025-01-15 10:00:00');

-- 王保母的評價 (2筆，平均 3.5)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking8_id, @sitter3_id, @user01_id, 4, 4, 3, 4, '整體還可以，小橘回來狀態不錯。但溝通上回覆有點慢，希望可以改進。', '感謝您的回饋，我會改進溝通效率的！', false, '2025-01-08 18:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking9_id, @sitter3_id, @user01_id, 3, 3, 3, 3, '普通，沒有特別問題但也沒有特別驚喜。', NULL, true, '2025-01-12 17:00:00');

-- 陳保母的評價 (3筆，平均 4.7)
INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking10_id, @sitter4_id, @user01_id, 5, 5, 5, 5, '陳保母經驗豐富，處理特殊需求寵物很專業。阿福有時候會緊張，但保母處理得很好，完全沒有問題！', '謝謝您的信任！阿福很棒，是隻很聰明的狗狗。', false, '2025-01-10 21:00:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking11_id, @sitter4_id, @user01_id, 5, 5, 5, 5, '喵喵的過敏體質被照顧得很好，完全沒有任何不適。非常推薦給有特殊需求寵物的飼主！', '感謝您的好評！喵喵是隻很可愛的貓咪，照顧牠很開心。', false, '2025-01-14 19:30:00');

INSERT INTO sitter_rating (id, booking_id, sitter_id, user_id, overall_rating, professionalism_rating, communication_rating, punctuality_rating, comment, sitter_reply, is_anonymous, created_at)
VALUES (RANDOM_UUID(), @booking12_id, @sitter4_id, @user01_id, 4, 4, 4, 5, '皮皮照顧得不錯，準時接送很加分。', NULL, false, '2025-01-17 10:00:00');

-- 更新保母的評分統計
UPDATE sitter SET average_rating = 4.5, rating_count = 4, completed_bookings = 4 WHERE id = @sitter1_id;
UPDATE sitter SET average_rating = 4.7, rating_count = 3, completed_bookings = 3 WHERE id = @sitter2_id;
UPDATE sitter SET average_rating = 3.5, rating_count = 2, completed_bookings = 2 WHERE id = @sitter3_id;
UPDATE sitter SET average_rating = 4.7, rating_count = 3, completed_bookings = 3 WHERE id = @sitter4_id;