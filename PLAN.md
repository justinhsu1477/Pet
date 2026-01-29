# 飼主（CUSTOMER）專屬頁面實作計畫

## 架構

- 新建 `frontend/customer.html` — 飼主登入後導向此頁面
- 新建 `frontend/js/customer.js` — 飼主頁面邏輯
- 修改 `frontend/js/login.js` — CUSTOMER 登入後導向 `customer.html`（ADMIN 維持 `dashboard.html`）
- 共用 `config.js`、`api.js`

## 五個模組

### 1. 首頁總覽
- 我的寵物數量
- 進行中預約數量
- 歷史預約數量 / 總花費
- 最近一筆預約狀態

### 2. 我的寵物
- 寵物卡片列表（名字、品種、年齡、照片佔位）
- 新增寵物（狗/貓，含專屬欄位）
- 編輯/刪除寵物
- API: `GET /api/pets/user/{userId}`, `POST /api/dogs`, `POST /api/cats`, `PUT`, `DELETE`

### 3. 預約服務（新預約流程）
- Step 1: 瀏覽可用保母列表（評分、經驗、時薪）
- Step 2: 選擇保母 → 顯示保母詳情
- Step 3: 選擇日期時間
- Step 4: 選擇要託付的寵物
- Step 5: 填寫備註 → 送出預約
- API: `GET /api/sitters/with-rating`, `GET /api/sitters/available`, `POST /api/bookings`

### 4. 我的預約
- 預約列表（卡片式，按狀態分類）
- 狀態篩選：全部 / 待確認 / 已確認 / 已完成 / 已取消
- 查看預約詳情
- 取消預約（PENDING / CONFIRMED 狀態）
- API: `GET /api/bookings/user/{userId}`, `POST /api/bookings/{id}/cancel`

### 5. 評價保母
- 已完成預約顯示「評價」按鈕
- 評分（1-5 星）+ 評論文字
- 已評價的顯示評價內容
- API: 使用現有 SitterRating 相關 endpoint

## 頁面導航
- 側邊欄或 Tab：總覽 | 我的寵物 | 預約服務 | 我的預約 | 評價

## 實作順序
1. 建立 `customer.html` 基本框架 + 導航 + 登入導向
2. 首頁總覽
3. 我的寵物（CRUD）
4. 預約服務（瀏覽保母 + 建立預約）
5. 我的預約（列表 + 取消）
6. 評價保母
