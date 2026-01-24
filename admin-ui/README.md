# Pet Care Admin UI - JWT 認證系統

完整的 JWT 認證管理後台,使用現代化的 UI 設計和安全的 Token 機制。

## 📁 文件結構

```
admin-ui/
├── index.html          # 登入頁面
├── app.html            # 主應用頁面
├── css/
│   └── style.css       # 全域樣式
└── js/
    ├── config.js       # API 配置
    ├── auth.js         # JWT 認證邏輯
    ├── api.js          # API 請求包裝器
    └── app.js          # 主應用邏輯
```

## 🚀 功能特性

### 認證系統
- ✅ JWT Token 認證 (Access Token + Refresh Token)
- ✅ Token 自動刷新機制
- ✅ 401 錯誤自動處理
- ✅ 登出時清除所有認證資料
- ✅ 角色權限檢查 (僅限 ADMIN)

### API 包裝器
- ✅ 所有請求自動添加 `Authorization: Bearer {token}`
- ✅ 自動設置 `X-Device-Type: WEB`
- ✅ Token 過期自動刷新並重試請求
- ✅ 刷新失敗自動跳轉登入頁

### UI 功能
- ✅ 儀表板 - 統計數據和熱門保母
- ✅ 用戶管理 - 列表和詳情查看
- ✅ 保母管理 - 列表和詳情查看
- ✅ 響應式設計 (支援手機)
- ✅ 模態框詳情查看

## 🔐 認證流程

### 1. 登入流程
```
用戶輸入帳密 → Auth.login()
    ↓
呼叫 /api/auth/jwt/login
    ↓
儲存 accessToken, refreshToken, user 到 localStorage
    ↓
檢查角色是否為 ADMIN
    ↓
跳轉到 app.html
```

### 2. API 請求流程
```
發送請求 → API.request()
    ↓
添加 Authorization header
    ↓
收到 401 錯誤?
    ↓ 是
嘗試刷新 Token → Auth.refreshToken()
    ↓ 成功
重試原始請求
    ↓ 失敗
清除認證 → 跳轉登入頁
```

### 3. 登出流程
```
點擊登出 → Auth.logout()
    ↓
呼叫 /api/auth/jwt/logout
    ↓
清除 localStorage
    ↓
跳轉到 index.html
```

## 🎨 UI 設計

### 配色方案
- **Primary**: `#2563eb` (藍色)
- **Success**: `#10b981` (綠色)
- **Warning**: `#f59e0b` (橙色)
- **Error**: `#ef4444` (紅色)
- **Background**: `#f8fafc` (淺灰)

### 組件設計
- **登入卡片**: 居中設計,陰影效果
- **側邊欄**: 固定寬度 260px,導航列表
- **統計卡片**: 網格佈局,hover 效果
- **表格**: 條紋背景,hover 高亮
- **模態框**: 彈出式詳情查看

## 📝 使用說明

### 1. 啟動後端服務
確保後端 API 服務運行在 `http://localhost:8080`

### 2. 開啟前端
```bash
# 方法 1: 使用 Python 簡易伺服器
cd /Users/justin/Desktop/pet/Pet/admin-ui
python3 -m http.server 8000

# 方法 2: 使用 Node.js http-server
npx http-server -p 8000

# 方法 3: 直接用瀏覽器開啟 (可能有 CORS 問題)
open index.html
```

### 3. 登入
開啟瀏覽器訪問 `http://localhost:8000`

**測試帳號**:
- 帳號: `admin`
- 密碼: `admin123`

**快捷鍵**: `Ctrl + Enter` 自動填入測試帳號並登入

### 4. 使用功能
- **儀表板**: 查看統計數據
- **用戶管理**: 查看用戶列表,點擊"查看詳情"查看完整資訊
- **保母管理**: 查看保母列表,評分和完成訂單數

## 🔧 配置說明

### API 端點配置 (`js/config.js`)
```javascript
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080',
    API_ENDPOINTS: {
        AUTH: {
            LOGIN: '/api/auth/jwt/login',
            REFRESH: '/api/auth/jwt/refresh',
            LOGOUT: '/api/auth/jwt/logout'
        },
        // ... 其他端點
    }
};
```

### 修改 API URL
如果後端運行在不同的地址,修改 `js/config.js` 中的 `API_BASE_URL`

## 🛡️ 安全機制

### Token 儲存
- **Access Token**: localStorage (自動過期)
- **Refresh Token**: localStorage (長期有效)
- **User Info**: localStorage (包含角色資訊)

### 權限檢查
1. **頁面載入時**: 檢查是否已登入和角色
2. **API 請求時**: 自動添加 Token
3. **Token 過期時**: 自動刷新
4. **刷新失敗時**: 自動登出

### CSRF 防護
- 使用 JWT Token 而非 Session Cookie
- 設置 `X-Device-Type` header

## 🐛 除錯提示

### 常見問題

**1. CORS 錯誤**
- 確保後端有正確設置 CORS headers
- 使用 http-server 而非直接開啟 HTML

**2. Token 過期**
- 系統會自動刷新,無需手動處理
- 如果刷新失敗,會自動跳轉登入頁

**3. 401 錯誤**
- 檢查 Token 是否正確儲存
- 檢查後端 JWT 驗證邏輯

**4. 樣式未載入**
- 檢查 CSS 路徑是否正確
- 確保使用 http-server 而非 file://

### 查看 Console Log
開啟瀏覽器開發者工具 (F12),查看:
- Network 標籤: API 請求和響應
- Console 標籤: 錯誤訊息和除錯 log
- Application → Local Storage: 查看儲存的 Token

## 📊 API 端點

### 認證 API
- `POST /api/auth/jwt/login` - 登入
- `POST /api/auth/jwt/refresh` - 刷新 Token
- `POST /api/auth/jwt/logout` - 登出

### 資料 API
- `GET /api/customers?role=CUSTOMER` - 獲取用戶列表
- `GET /api/customers/{id}` - 獲取用戶詳情
- `GET /api/sitters/with-rating` - 獲取保母列表 (含評分)
- `GET /api/sitters/{id}` - 獲取保母詳情
- `GET /api/pets/user/{userId}` - 獲取用戶的寵物
- `GET /api/bookings/user/{userId}` - 獲取用戶的訂單
- `GET /api/sitter-ratings/sitter/{sitterId}/stats` - 獲取保母評分統計

## 🎯 後續優化建議

1. **功能增強**
   - [ ] 添加搜尋和篩選功能
   - [ ] 添加分頁功能
   - [ ] 添加數據圖表
   - [ ] 添加編輯和刪除功能

2. **安全增強**
   - [ ] 實作 HTTPS
   - [ ] 添加驗證碼
   - [ ] 實作 Rate Limiting
   - [ ] 添加日誌記錄

3. **UI/UX 優化**
   - [ ] 添加載入動畫
   - [ ] 添加成功/錯誤提示
   - [ ] 優化手機版佈局
   - [ ] 添加暗色模式

## 📄 授權

MIT License

---

**開發者**: Pet Care Team
**版本**: 1.0.0
**最後更新**: 2026-01-25
