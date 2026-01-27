# PetCare Android App

這是一個寵物照護管理系統的 Android 客戶端應用程式。

## 技術架構

- **語言**: Kotlin
- **架構**: MVVM (Model-View-ViewModel)
- **依賴注入**: Hilt
- **網路請求**: Retrofit + OkHttp
- **非同步處理**: Coroutines
- **UI**: ViewBinding + Material Design

## 專案結構

```
app/
├── src/main/java/com/pet/android/
│   ├── data/                    # 資料層
│   │   ├── api/                # API 介面定義
│   │   ├── model/              # 資料模型
│   │   └── repository/         # Repository 實作
│   ├── di/                      # 依賴注入模組
│   ├── ui/                      # UI 層
│   │   ├── base/               # 基礎 UI 組件
│   │   ├── login/              # 登入功能
│   │   ├── pet/                # 寵物管理
│   │   └── sitter/             # 保母管理
│   ├── util/                    # 工具類
│   └── PetCareApplication.kt   # Application 類
└── src/main/res/                # 資源檔案
    ├── layout/                  # Layout XML
    ├── values/                  # 字串、顏色、主題等
    └── drawable/                # 圖片資源
```

## 主要功能

### 1. 使用者認證
- 登入功能
- 與後端 Spring Boot API 整合

### 2. 寵物管理
- 查看寵物列表
- 寵物詳細資訊
- 新增/編輯/刪除寵物（待實作）

### 3. 保母管理
- 查看保母列表
- 保母詳細資訊
- 新增/編輯保母（待實作）

## 後端 API 連接設定

App 支援多環境切換，透過 `EnvironmentManager.kt` 管理：

| 環境 | Base URL | 說明 |
|------|----------|------|
| 開發環境 | `http://172.20.10.2:8080/` | iPhone 熱點下電腦的固定 IP |
| 測試環境 | `https://staging-api.petcare.com/` | Staging 伺服器 |
| 正式環境 | `https://api.petcare.com/` | Production 伺服器 |

**注意**：
- `172.20.10.2` 是 iPhone 熱點下電腦的固定 IP
- 若使用 Android Emulator 連接 localhost，需改為 `10.0.2.2`
- 環境設定存在 DataStore，App 重啟後會保留

## 建置專案

### 環境需求
- Android Studio Hedgehog | 2023.1.1 或更新版本
- JDK 17
- Android SDK 34
- Kotlin 1.9.20

### 建置步驟

1. 開啟 Android Studio
2. 選擇 "Open an Existing Project"
3. 選擇 `android-app` 資料夾
4. 等待 Gradle 同步完成
5. 點擊 Run 按鈕或使用快捷鍵 Shift+F10

## 測試帳號

```
帳號: admin
密碼: admin123
```

## 架構說明

### BaseActivity
所有 Activity 繼承自 `BaseActivity<VB: ViewBinding>`，提供：
- ViewBinding 自動初始化
- 統一的返回按鈕處理
- 固定螢幕方向為直向
- 系統視窗邊距處理

### Repository 模式
- 資料來源的抽象層
- 處理 API 呼叫和錯誤處理
- 返回 `Resource<T>` 封裝結果

### ViewModel
- 持有 UI 狀態
- 處理業務邏輯
- 使用 LiveData 更新 UI

## 待實作功能

- [ ] 寵物詳細頁面
- [ ] 新增/編輯寵物
- [ ] 保母記錄管理
- [ ] 使用者註冊
- [ ] Token 認證管理
- [ ] 離線快取
- [ ] 圖片上傳

## 注意事項

1. 確保後端 Spring Boot 服務已啟動
2. iPhone 熱點：電腦 IP 固定為 `172.20.10.2`
3. Android Emulator：使用 `10.0.2.2` 連接本機 localhost
4. 同一 WiFi：使用電腦的實際 IP 位址
5. 確保後端 CORS 設定允許來自 Android 的請求
