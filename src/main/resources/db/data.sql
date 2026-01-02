-- 插入 Cat 測試資料
INSERT INTO cat (name, age) VALUES ('小咪', 2);
INSERT INTO cat (name, age) VALUES ('大橘', 3);
INSERT INTO cat (name, age) VALUES ('花花', 1);

-- 插入 Pet 測試資料
INSERT INTO pet (name, type, age, breed, owner_name, owner_phone, special_needs)
VALUES ('阿福', '狗', 5, '黃金獵犬', '王小明', '0912-345-678', '需要每天散步兩次');

INSERT INTO pet (name, type, age, breed, owner_name, owner_phone, special_needs)
VALUES ('喵喵', '貓', 3, '波斯貓', '李小華', '0923-456-789', '對海鮮過敏');

INSERT INTO pet (name, type, age, breed, owner_name, owner_phone, special_needs)
VALUES ('毛毛', '兔子', 1, '荷蘭侏儒兔', '張大同', '0934-567-890', '每天需要新鮮蔬菜');

INSERT INTO pet (name, type, age, breed, owner_name, owner_phone, special_needs)
VALUES ('皮皮', '狗', 2, '柴犬', '陳小美', '0945-678-901', NULL);

INSERT INTO pet (name, type, age, breed, owner_name, owner_phone, special_needs)
VALUES ('咪咪', '貓', 4, '美國短毛貓', '林大明', '0956-789-012', '需要定期梳毛');

-- 插入 Sitter 測試資料
INSERT INTO sitter (name, phone, email, experience)
VALUES ('張保母', '0911-111-222', 'zhang@example.com', '5年寵物照護經驗，擅長照顧大型犬');

INSERT INTO sitter (name, phone, email, experience)
VALUES ('李保母', '0922-222-333', 'lee@example.com', '3年貓咪專業照護，有獸醫助理背景');

INSERT INTO sitter (name, phone, email, experience)
VALUES ('王保母', '0933-333-444', 'wang@example.com', '2年小型寵物照護經驗');

INSERT INTO sitter (name, phone, email, experience)
VALUES ('陳保母', '0944-444-555', 'chen@example.com', '7年全方位寵物照護，可處理特殊需求寵物');

-- 插入 SitterRecord 測試資料
INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (1, 1, '2025-01-01 09:00:00', '晨間散步', true, true, '活潑開心', '阿福今天精神很好，在公園玩得很開心', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (1, 1, '2025-01-01 18:00:00', '晚餐時間', true, false, '正常', '食慾良好，吃完整碗飼料', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (2, 2, '2025-01-01 10:00:00', '餵食與梳毛', true, false, '慵懶', '喵喵今天比較想睡覺，梳毛時很配合', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (3, 3, '2025-01-01 08:30:00', '早餐與清潔', true, false, '活潑', '毛毛吃了新鮮胡蘿蔔和生菜，活動力很好', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (4, 4, '2025-01-01 14:00:00', '下午散步', true, true, '興奮', '皮皮在公園遇到其他柴犬，玩得很開心', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (5, 2, '2025-01-01 16:00:00', '梳毛與互動', true, false, '親人', '咪咪今天特別黏人，梳了很多毛', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (1, 1, '2025-01-02 09:30:00', '晨間散步', true, true, '活潑開心', '今天遇到鄰居的狗狗，互相打招呼', NULL);

INSERT INTO sitter_record (pet_id, sitter_id, record_time, activity, fed, walked, mood_status, notes, photos)
VALUES (2, 2, '2025-01-02 11:00:00', '遊戲時間', true, false, '活潑', '用逗貓棒跟喵喵玩了30分鐘', NULL);