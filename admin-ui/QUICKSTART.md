# 🚀 快速開始指南

## 1️⃣ 啟動後端 (必須)

```bash
cd /Users/justin/Desktop/pet/Pet
mvn spring-boot:run
```

確保後端運行在: `http://localhost:8080`

## 2️⃣ 啟動前端

### 選項 A: 使用腳本 (最簡單)

```bash
cd /Users/justin/Desktop/pet/Pet/admin-ui
./start.sh
```

### 選項 B: 使用 Python

```bash
cd /Users/justin/Desktop/pet/Pet/admin-ui
python3 -m http.server 8000
```

### 選項 C: 使用 Node.js

```bash
cd /Users/justin/Desktop/pet/Pet/admin-ui
npx http-server -p 8000
```

## 3️⃣ 開啟瀏覽器

訪問: **http://localhost:8000**

## 4️⃣ 登入

```
帳號: admin
密碼: admin123
```

或按 **Ctrl + Enter** 快速登入

---

## 📂 文件結構

```
admin-ui/
├── index.html          # 登入頁面
├── app.html            # 主應用頁面
├── test.html           # 測試頁面
├── css/
│   └── style.css       # 樣式
└── js/
    ├── config.js       # API 配置
    ├── auth.js         # JWT 認證
    ├── api.js          # API 請求包裝
    └── app.js          # 主應用邏輯
```

---

## 🔑 核心功能

### JWT 認證
- ✅ Access Token + Refresh Token
- ✅ 自動刷新機制
- ✅ 401 錯誤自動處理
- ✅ 角色權限檢查

### 管理功能
- 📊 儀表板 - 統計數據
- 👥 用戶管理 - 列表和詳情
- 🧑‍⚕️ 保母管理 - 列表和詳情

---

## 🐛 快速除錯

### 查看 Token
```
F12 → Application → Local Storage
```

### 查看 API 請求
```
F12 → Network → XHR
```

### 查看錯誤
```
F12 → Console
```

---

## 🧪 測試頁面

訪問: **http://localhost:8000/test.html**

測試功能:
1. 登入測試
2. Token 檢查
3. API 請求測試
4. Token 刷新測試
5. 登出測試

---

## 📝 API 端點

### 認證
- `POST /api/auth/jwt/login` - 登入
- `POST /api/auth/jwt/refresh` - 刷新 Token
- `POST /api/auth/jwt/logout` - 登出

### 數據
- `GET /api/customers?role=CUSTOMER` - 用戶列表
- `GET /api/sitters/with-rating` - 保母列表
- `GET /api/pets` - 寵物列表
- `GET /api/bookings` - 訂單列表

---

## 🎨 配色方案

```css
Primary:    #2563eb  (藍色)
Success:    #10b981  (綠色)
Warning:    #f59e0b  (橙色)
Error:      #ef4444  (紅色)
Background: #f8fafc  (淺灰)
```

---

## ⚙️ 快速配置

### 修改 API URL

編輯 `js/config.js`:

```javascript
API_BASE_URL: 'http://localhost:8080'
```

### 修改 Token 刷新時間

編輯 `js/config.js`:

```javascript
TOKEN_REFRESH_BUFFER: 60 * 1000  // 60秒
```

---

## 📚 更多文件

- [README.md](README.md) - 完整專案說明
- [USAGE.md](USAGE.md) - 詳細使用指南

---

**開發日期**: 2026-01-25
**版本**: 1.0.0
