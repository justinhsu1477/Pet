# Pet Care Admin UI - 使用指南

## 🎯 快速開始

### 步驟 1: 啟動後端服務

確保 Pet Care 後端服務運行在 `http://localhost:8080`

```bash
# 在專案根目錄執行
cd /Users/justin/Desktop/pet/Pet
mvn spring-boot:run
```

### 步驟 2: 啟動前端服務

```bash
# 方法 1: 使用啟動腳本 (推薦)
cd /Users/justin/Desktop/pet/Pet/admin-ui
./start.sh

# 方法 2: 手動啟動 Python 伺服器
cd /Users/justin/Desktop/pet/Pet/admin-ui
python3 -m http.server 8000

# 方法 3: 使用 Node.js http-server
cd /Users/justin/Desktop/pet/Pet/admin-ui
npx http-server -p 8000
```

### 步驟 3: 開啟瀏覽器

訪問: `http://localhost:8000`

### 步驟 4: 登入

使用測試帳號:
- 帳號: `admin`
- 密碼: `admin123`

或按 `Ctrl + Enter` 自動填入並登入

## 📋 功能說明

### 1. 登入頁面 (index.html)

**功能**:
- JWT Token 認證登入
- 自動檢查是否已登入
- 角色權限驗證 (僅限 ADMIN)
- 錯誤訊息顯示

**操作**:
1. 輸入帳號密碼
2. 點擊「登入」按鈕
3. 系統驗證後跳轉到主頁

**快捷鍵**:
- `Ctrl + Enter`: 自動填入測試帳號並登入

### 2. 儀表板 (Dashboard)

**功能**:
- 顯示統計數據
  - 保母總數
  - 寵物總數
  - 用戶總數
- 顯示熱門保母列表
  - 保母名稱
  - 專長
  - 評分
  - 狀態

**操作**:
- 自動載入,無需手動操作

### 3. 用戶管理 (Users)

**功能**:
- 顯示所有用戶列表
- 查看用戶詳情

**操作**:
1. 點擊側邊欄「用戶管理」
2. 瀏覽用戶列表
3. 點擊「查看詳情」按鈕查看完整資訊

**用戶詳情包含**:
- 基本資訊 (姓名、Email、電話、地址)
- 會員等級
- 預約統計 (總預約數、總消費)
- 緊急聯絡人資訊
- 寵物列表
- 最近訂單

### 4. 保母管理 (Sitters)

**功能**:
- 顯示所有保母列表
- 查看保母詳情

**操作**:
1. 點擊側邊欄「保母管理」
2. 瀏覽保母列表
3. 點擊「查看詳情」按鈕查看完整資訊

**保母詳情包含**:
- 基本資訊 (姓名、專長、經驗年數、簡介)
- 評分統計 (平均評分、評價數量、完成訂單)
- 最近訂單

### 5. 登出

**操作**:
1. 點擊側邊欄底部的登出按鈕 (🚪)
2. 確認登出
3. 系統清除 Token 並跳轉到登入頁

## 🔐 認證機制

### Token 類型

**Access Token**:
- 用途: API 請求認證
- 儲存: localStorage
- 有效期: 短期 (通常 15-30 分鐘)
- 自動刷新: 是

**Refresh Token**:
- 用途: 刷新 Access Token
- 儲存: localStorage
- 有效期: 長期 (通常 7-30 天)
- 自動使用: 是

### 自動處理機制

**Token 過期處理**:
1. API 請求收到 401 錯誤
2. 自動使用 Refresh Token 刷新 Access Token
3. 重試原始請求
4. 如果刷新失敗,自動登出並跳轉登入頁

**權限檢查**:
1. 頁面載入時檢查是否已登入
2. 檢查用戶角色是否為 ADMIN
3. 不符合條件自動跳轉登入頁

## 🧪 測試功能

### 使用測試頁面

開啟 `http://localhost:8000/test.html`

**測試項目**:
1. **登入測試**: 測試 JWT 登入功能
2. **Token 檢查**: 查看 localStorage 中的 Token
3. **API 請求測試**: 測試帶 Token 的 API 請求
4. **Token 刷新測試**: 測試 Token 刷新機制
5. **登出測試**: 測試登出功能

### 使用瀏覽器開發者工具

**查看 Token**:
1. 按 F12 開啟開發者工具
2. 選擇 Application 標籤
3. 展開 Local Storage
4. 查看:
   - `pet_admin_access_token`
   - `pet_admin_refresh_token`
   - `pet_admin_user`

**查看 API 請求**:
1. 按 F12 開啟開發者工具
2. 選擇 Network 標籤
3. 篩選 XHR 請求
4. 查看請求 Headers 中的 `Authorization: Bearer {token}`

**查看 Console Log**:
1. 按 F12 開啟開發者工具
2. 選擇 Console 標籤
3. 查看所有 log 訊息和錯誤

## 🐛 常見問題

### Q1: 登入後頁面空白?

**可能原因**:
- 後端服務未啟動
- CORS 設定錯誤
- API URL 配置錯誤

**解決方法**:
1. 確認後端服務運行在 `http://localhost:8080`
2. 檢查瀏覽器 Console 是否有 CORS 錯誤
3. 檢查 `js/config.js` 中的 `API_BASE_URL`

### Q2: 一直顯示「登入失敗」?

**可能原因**:
- 帳號密碼錯誤
- 後端認證 API 異常
- 網路連線問題

**解決方法**:
1. 確認帳號是 `admin`,密碼是 `admin123`
2. 檢查後端 Console log
3. 檢查瀏覽器 Network 標籤的 API 響應

### Q3: API 請求失敗 (401 錯誤)?

**可能原因**:
- Token 已過期
- Token 格式錯誤
- 後端 JWT 驗證失敗

**解決方法**:
1. 系統會自動嘗試刷新 Token
2. 如果仍失敗,手動登出再登入
3. 檢查後端 JWT 配置

### Q4: 樣式顯示異常?

**可能原因**:
- CSS 文件未載入
- 使用 file:// 協議開啟

**解決方法**:
1. 確保使用 http-server 啟動
2. 檢查瀏覽器 Network 標籤 CSS 是否載入成功
3. 清除瀏覽器快取

### Q5: Token 自動刷新不工作?

**可能原因**:
- Refresh Token 已過期
- 後端刷新 API 異常
- localStorage 被清除

**解決方法**:
1. 檢查 localStorage 中是否有 `pet_admin_refresh_token`
2. 檢查瀏覽器 Console 是否有錯誤
3. 重新登入

## 📊 API 請求流程

### 正常請求流程

```
用戶操作 (如點擊「用戶管理」)
    ↓
App.loadUsers()
    ↓
API.users.getAll()
    ↓
API.request('/api/customers?role=CUSTOMER', { method: 'GET' })
    ↓
添加 Headers:
  - Authorization: Bearer {accessToken}
  - X-Device-Type: WEB
    ↓
fetch('http://localhost:8080/api/customers?role=CUSTOMER')
    ↓
收到 200 OK 響應
    ↓
返回數據並渲染 UI
```

### 401 錯誤處理流程

```
API 請求
    ↓
收到 401 錯誤
    ↓
檢查是否為重試請求?
    ↓ 否
Auth.refreshToken()
    ↓
POST /api/auth/jwt/refresh
  Body: { refreshToken: ... }
    ↓
收到新的 Access Token
    ↓
更新 localStorage
    ↓
重試原始請求 (retry = true)
    ↓
成功 → 返回數據
失敗 → 清除認證 → 跳轉登入頁
```

## 🎨 UI 自訂

### 修改配色

編輯 `css/style.css` 中的 CSS 變數:

```css
:root {
    --color-primary: #2563eb;      /* 主色 */
    --color-success: #10b981;      /* 成功色 */
    --color-warning: #f59e0b;      /* 警告色 */
    --color-error: #ef4444;        /* 錯誤色 */
    --color-background: #f8fafc;   /* 背景色 */
}
```

### 修改 Logo

替換以下元素中的 emoji:
- 登入頁: `.login-logo` (🐾)
- 側邊欄: `.sidebar-logo` (🐾)

### 修改導航項目

編輯 `app.html` 中的 `.sidebar-nav`:

```html
<a href="#" class="nav-item" data-page="new-page">
    <span class="nav-icon">🆕</span>
    <span>新頁面</span>
</a>
```

## 🔧 進階配置

### 修改 API URL

編輯 `js/config.js`:

```javascript
const CONFIG = {
    API_BASE_URL: 'https://your-api-domain.com',
    // ...
};
```

### 修改 Token 刷新緩衝時間

編輯 `js/config.js`:

```javascript
const CONFIG = {
    // ...
    TOKEN_REFRESH_BUFFER: 5 * 60 * 1000, // 5分鐘
};
```

### 修改請求超時時間

編輯 `js/config.js`:

```javascript
const CONFIG = {
    // ...
    REQUEST_TIMEOUT: 60000, // 60秒
};
```

## 📝 開發提示

### 添加新的 API 端點

1. 在 `js/config.js` 添加端點:

```javascript
API_ENDPOINTS: {
    // ...
    NEW_RESOURCE: '/api/new-resource'
}
```

2. 在 `js/api.js` 添加 API 方法:

```javascript
// ===== 新資源 API =====
newResource: {
    getAll() {
        return API.get(CONFIG.API_ENDPOINTS.NEW_RESOURCE);
    }
}
```

### 添加新頁面

1. 在 `app.html` 添加頁面 HTML:

```html
<div id="page-new" class="page">
    <h2>新頁面</h2>
    <!-- 內容 -->
</div>
```

2. 在 `js/app.js` 添加載入邏輯:

```javascript
case 'new':
    this.loadNewPage();
    break;
```

3. 實作載入方法:

```javascript
async loadNewPage() {
    try {
        const res = await API.newResource.getAll();
        // 處理資料
    } catch (error) {
        console.error('Load error:', error);
    }
}
```

## 🚀 部署

### 前端部署

1. **靜態檔案託管**:
   - 上傳整個 `admin-ui` 目錄到靜態託管服務
   - 支援平台: Netlify, Vercel, GitHub Pages, AWS S3

2. **Nginx 配置**:

```nginx
server {
    listen 80;
    server_name admin.yoursite.com;
    root /path/to/admin-ui;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

3. **修改 API URL**:
   - 編輯 `js/config.js`
   - 將 `API_BASE_URL` 改為正式環境 API 地址

### 安全建議

1. **使用 HTTPS**:
   - 前端和後端都應使用 HTTPS
   - 防止 Token 被攔截

2. **設定 CSP Headers**:

```nginx
add_header Content-Security-Policy "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';" always;
```

3. **移除測試帳號提示**:
   - 編輯 `index.html`
   - 移除 `.form-footer` 中的測試帳號資訊

4. **移除 Console Log**:
   - 在正式環境移除所有 `console.log()`

## 📚 相關文件

- [README.md](README.md) - 專案說明
- [API 文件](../docs/API.md) - 後端 API 文件
- [JWT 規範](https://jwt.io/) - JWT Token 標準

---

**需要幫助?**
- 查看 [常見問題](#🐛-常見問題)
- 使用 [測試頁面](#使用測試頁面) 除錯
- 檢查瀏覽器 Console 和 Network 標籤
