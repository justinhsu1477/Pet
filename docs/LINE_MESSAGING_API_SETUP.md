# LINE Messaging API 設定指南

本文件記錄 LINE Messaging API 的申請與設定流程，供未來參考或面試說明使用。

---

## 目錄

1. [LINE Developers Console 設定](#1-line-developers-console-設定)
2. [取得必要的憑證](#2-取得必要的憑證)
3. [專案整合設定](#3-專案整合設定)
4. [環境變數設定](#4-環境變數設定)
5. [Demo 模式說明](#5-demo-模式說明)
6. [測試與驗證](#6-測試與驗證)

---

## 1. LINE Developers Console 設定

### 1.1 建立 Provider

1. 前往 [LINE Developers Console](https://developers.line.biz/console/)
2. 使用 LINE 帳號登入
3. 點選「Create a new provider」
4. 輸入 Provider 名稱（例如：`Pet Care System`）
5. 點選「Create」完成建立

### 1.2 建立 Messaging API Channel

1. 在 Provider 頁面點選「Create a new channel」
2. 選擇 Channel 類型：**Messaging API**
3. 填寫必要資訊：
   - **Channel name**：頻道名稱（例如：`寵物保母通知`）
   - **Channel description**：頻道描述
   - **Category**：選擇適合的類別
   - **Subcategory**：選擇適合的子類別
   - **Email address**：聯絡信箱
4. 勾選同意條款後點選「Create」

### 1.3 重要設定項目

在 Channel 設定頁面，需要注意以下設定：

| 設定項目 | 說明 | 建議值 |
|---------|------|--------|
| **Webhook URL** | 接收 LINE 事件的端點 | 本專案未使用（僅 Push 訊息） |
| **Use webhook** | 是否啟用 Webhook | OFF（本專案僅使用 Push API） |
| **Allow bot to join group chats** | 允許加入群組 | 依需求設定 |
| **Auto-reply messages** | 自動回覆訊息 | 建議關閉 |
| **Greeting messages** | 加入好友時的歡迎訊息 | 可自訂 |

---

## 2. 取得必要的憑證

### 2.1 Channel 基本資訊

在 Channel 的「Basic settings」頁面可取得：

| 項目 | 值 | 說明 |
|------|-----|------|
| **Channel ID** | `你的Channel_ID` | 頻道唯一識別碼（10位數字） |
| **Channel Secret** | `你的Channel_Secret` | 用於驗證請求來源（32字元十六進位） |

### 2.2 Channel Access Token

Channel Access Token 是呼叫 LINE Messaging API 的必要憑證。

#### 取得方式一：透過 Console（推薦）

1. 進入 Channel 的「Messaging API」頁籤
2. 捲動到「Channel access token」區塊
3. 點選「Issue」按鈕產生 Token
4. 複製產生的 Long-lived channel access token

> **注意**：每次點選 Issue 都會產生新的 Token，舊 Token 會失效

#### 取得方式二：透過 API

```bash
curl -X POST https://api.line.me/v2/oauth/accessToken \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=你的Channel_ID' \
  -d 'client_secret=你的Channel_Secret'
```

### 2.3 User ID 取得方式

User ID 用於指定訊息推送對象，格式為 `U` 開頭的 33 字元字串。

| 項目 | 值 |
|------|-----|
| **Demo User ID** | `U` 開頭 + 32字元（例如：`Uxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`） |

#### 取得 User ID 的方法

1. **透過 Webhook 事件**（生產環境）
   - 當用戶與 Bot 互動時，Webhook 事件中會包含 `userId`
   - 將 userId 儲存於資料庫，與系統用戶建立關聯

2. **透過 Console 查看自己的 ID**（開發/測試用）
   - 進入 Channel 的「Basic settings」頁籤
   - 查看「Your user ID」欄位

3. **透過 LINE Login 取得**
   - 整合 LINE Login 後，可在登入流程中取得用戶的 LINE User ID

---

## 3. 專案整合設定

### 3.1 設定類別 - LineMessagingConfig

檔案位置：`src/main/java/com/pet/config/LineMessagingConfig.java`

```java
@Configuration
@ConfigurationProperties(prefix = "line.messaging")
@Getter
@Setter
public class LineMessagingConfig {

    private String channelToken;    // Channel Access Token
    private String channelSecret;   // Channel Secret（備用）
    private String demoUserId;      // Demo 模式的固定收件人
    private boolean enabled = true; // 是否啟用通知

    public boolean isConfigured() {
        return channelToken != null && !channelToken.isEmpty()
            && demoUserId != null && !demoUserId.isEmpty();
    }
}
```

### 3.2 服務類別 - LineMessagingService

檔案位置：`src/main/java/com/pet/service/LineMessagingService.java`

主要功能：
- 使用 LINE Push Message API 發送通知
- 支援多種預約狀態通知（確認、取消、拒絕、完成）
- Demo 模式下所有通知都發送到固定的 User ID

```java
// API 端點
private static final String LINE_API_URL = "https://api.line.me/v2/bot/message/push";

// 發送通知的核心方法
private void sendNotification(String message) {
    // 1. 檢查是否啟用
    // 2. 檢查設定是否完整
    // 3. 組裝 HTTP 請求
    // 4. 呼叫 LINE API
}
```

#### 支援的通知類型

| 方法 | 觸發時機 | 訊息內容 |
|------|---------|---------|
| `sendBookingConfirmedNotification` | 預約確認時 | 寵物、保母、時間、費用 |
| `sendBookingCancelledNotification` | 預約取消時 | 寵物、保母、原訂時間、取消原因 |
| `sendBookingRejectedNotification` | 預約被拒時 | 寵物、保母、申請時間、拒絕原因 |
| `sendBookingCompletedNotification` | 服務完成時 | 寵物、保母、服務時間、費用 |

### 3.3 Application YAML 設定

#### DEV 環境 (`application-dev.yml`)

```yaml
# LINE Messaging API Configuration (DEV)
# 環境變數來源：IDE Run Configuration 或 .env 檔案
line:
  messaging:
    channel-token: ${LINE_CHANNEL_TOKEN:}
    channel-secret: ${LINE_CHANNEL_SECRET:}
    demo-user-id: ${LINE_DEMO_USER_ID:}
    enabled: true
```

#### QAS 環境 (`application-qas.yml`)

```yaml
# LINE Messaging API Configuration (QAS)
# 環境變數來源：Docker Compose --env-file 或 IDE Run Configuration
line:
  messaging:
    channel-token: ${LINE_CHANNEL_TOKEN:}
    channel-secret: ${LINE_CHANNEL_SECRET:}
    demo-user-id: ${LINE_DEMO_USER_ID:}
    enabled: true
```

---

## 4. 環境變數設定

### 4.1 環境變數清單

| 環境變數 | 說明 | 範例值 |
|---------|------|--------|
| `LINE_CHANNEL_TOKEN` | Channel Access Token | `xxxxxxxxxxxxxxxx...` |
| `LINE_CHANNEL_SECRET` | Channel Secret | `你的32字元Channel_Secret` |
| `LINE_DEMO_USER_ID` | Demo 模式的收件人 | `U` + 32字元（從 Console 取得） |

### 4.2 .env 檔案格式

建立 `.env.dev` 或 `.env.qas` 檔案：

```bash
# =================================
# LINE Messaging API
# =================================
LINE_CHANNEL_TOKEN=從LINE_Console取得的Channel_Access_Token（約172字元）
LINE_CHANNEL_SECRET=從LINE_Console取得的Channel_Secret（32字元）
LINE_DEMO_USER_ID=從LINE_Console取得的Your_User_ID（U開頭+32字元）

# =================================
# 其他設定（JWT 等）
# =================================
JWT_SECRET=your_jwt_secret_here
```

> **安全提醒**：`.env` 檔案應加入 `.gitignore`，不可提交至版本控制

### 4.3 IDE Run Configuration 設定（IntelliJ IDEA）

1. 開啟 Run/Debug Configurations
2. 選擇 Spring Boot 應用程式
3. 在「Environment variables」欄位加入：

```
LINE_CHANNEL_TOKEN=你的Token;LINE_CHANNEL_SECRET=你的Secret;LINE_DEMO_USER_ID=你的UserID
```

或使用「Modify options」>「Environment variables from file」載入 `.env` 檔案。

### 4.4 Docker Compose 使用方式

#### 方式一：使用 --env-file 參數

```bash
# DEV 環境
docker compose -f docker-compose.dev.yml --env-file .env.dev up -d

# QAS 環境
docker compose -f docker-compose.qas.yml --env-file .env.qas up -d
```

#### 方式二：Docker Compose 內建 .env

Docker Compose 會自動載入同目錄下的 `.env` 檔案。

#### Docker Compose 環境變數傳遞

`docker-compose.dev.yml` 和 `docker-compose.qas.yml` 中的設定：

```yaml
services:
  backend:
    environment:
      # LINE Messaging API
      - LINE_CHANNEL_TOKEN=${LINE_CHANNEL_TOKEN}
      - LINE_CHANNEL_SECRET=${LINE_CHANNEL_SECRET}
      - LINE_DEMO_USER_ID=${LINE_DEMO_USER_ID}
```

### 4.5 各種執行方式的環境變數來源

| 執行方式 | 環境變數來源 | 說明 |
|----------|-------------|------|
| **IDE 直接執行** | Run Configuration 設定的環境變數 | 需手動在 IDE 設定 |
| **IDE + EnvFile 外掛** | `.env` 檔案 | 需安裝 EnvFile 外掛 |
| **Maven 指令** | 系統環境變數或 `-D` 參數 | `export LINE_CHANNEL_TOKEN=xxx && mvn spring-boot:run` |
| **Docker Compose** | `--env-file` 指定的檔案 | 自動載入到容器內 |
| **JAR 直接執行** | 系統環境變數 | `export LINE_CHANNEL_TOKEN=xxx && java -jar app.jar` |

#### IDE 執行時的 Profile 切換

```bash
# application.yml 中設定 active profile
spring:
  profiles:
    active: dev  # 或 qas
```

IntelliJ IDEA 設定方式：
1. Run > Edit Configurations
2. Active profiles: `dev` 或 `qas`
3. Environment variables: 設定 LINE 相關變數

#### Maven 指令執行

```bash
# DEV 環境
export LINE_CHANNEL_TOKEN=你的Token
export LINE_CHANNEL_SECRET=你的Secret
export LINE_DEMO_USER_ID=你的UserID
mvn spring-boot:run -Dspring.profiles.active=dev

# 或一行指令
LINE_CHANNEL_TOKEN=xxx LINE_CHANNEL_SECRET=xxx LINE_DEMO_USER_ID=xxx mvn spring-boot:run -Dspring.profiles.active=dev
```

### 4.6 換電腦時的注意事項

> ⚠️ **重要提醒**：`.env` 檔案不會提交到 Git，換電腦時需要手動處理！

#### 換電腦必做清單

1. **複製 .env 檔案**
   ```bash
   # 從舊電腦複製以下檔案到新電腦專案根目錄
   .env.dev
   .env.qas
   ```

2. **或重新建立 .env 檔案**
   ```bash
   # .env.dev 內容範例
   LINE_CHANNEL_TOKEN=從LINE_Console取得的Channel_Access_Token
   LINE_CHANNEL_SECRET=從LINE_Console取得的Channel_Secret
   LINE_DEMO_USER_ID=從LINE_Console取得的Your_User_ID
   JWT_SECRET=你的JWT密鑰
   ```

3. **重新設定 IDE Run Configuration**
   - 環境變數不會隨專案複製
   - 需要在新電腦的 IntelliJ IDEA 重新設定

4. **驗證設定**
   ```bash
   # 啟動應用後觀察 log
   # 應該看到 "LINE 通知發送成功" 而非 "LINE 設定不完整"
   ```

#### 建議的備份方式

1. **安全儲存 .env 檔案**（不要放在公開的雲端）
   - 使用密碼管理器（如 1Password、Bitwarden）
   - 使用加密的雲端儲存
   - 使用私人的 Gist（設為 Secret）

2. **使用 .env.example 作為範本**
   ```bash
   # .env.example（可以提交到 Git）
   LINE_CHANNEL_TOKEN=請填入你的Token
   LINE_CHANNEL_SECRET=請填入你的Secret
   LINE_DEMO_USER_ID=請填入你的UserID
   JWT_SECRET=請填入JWT密鑰
   ```

---

## 5. Demo 模式說明

### 5.1 為何使用固定的 User ID

本專案採用 **Demo 模式**，所有 LINE 通知都發送到固定的 `LINE_DEMO_USER_ID`，原因如下：

1. **面試展示用途**
   - 快速展示 LINE 通知功能
   - 無需真實用戶註冊流程
   - 面試官可即時看到通知效果

2. **簡化開發流程**
   - 不需要實作 LINE Login 整合
   - 不需要建立用戶與 LINE User ID 的對應關係
   - 專注於核心業務邏輯

3. **測試便利性**
   - 開發者可以直接收到所有測試通知
   - 方便驗證訊息內容格式

### 5.2 生產環境應如何修改

在實際生產環境中，應進行以下修改：

#### Step 1：整合 LINE Login

```java
// 新增 LINE Login 相關設定
line:
  login:
    channel-id: ${LINE_LOGIN_CHANNEL_ID}
    channel-secret: ${LINE_LOGIN_CHANNEL_SECRET}
    redirect-uri: ${LINE_LOGIN_REDIRECT_URI}
```

#### Step 2：用戶資料表新增欄位

```sql
ALTER TABLE users ADD COLUMN line_user_id VARCHAR(50);
CREATE INDEX idx_users_line_user_id ON users(line_user_id);
```

#### Step 3：修改 LineMessagingService

```java
// 修改前（Demo 模式）
body.put("to", config.getDemoUserId());

// 修改後（生產模式）
body.put("to", booking.getCustomer().getLineUserId());
```

#### Step 4：新增用戶綁定流程

1. 用戶透過 LINE Login 登入/註冊
2. 取得用戶的 LINE User ID
3. 將 LINE User ID 儲存至用戶資料表
4. 發送通知時，查詢用戶對應的 LINE User ID

#### 生產環境架構圖

```
[用戶操作預約]
      ↓
[系統處理預約狀態變更]
      ↓
[LineMessagingService]
      ↓
[查詢用戶的 LINE User ID]  ← 從資料庫取得
      ↓
[呼叫 LINE Push Message API]
      ↓
[用戶的 LINE App 收到通知]
```

---

## 6. 測試與驗證

### 6.1 驗證設定是否正確

啟動應用程式後，觀察 Log：

```
# 設定完整時
INFO  LINE 通知發送成功

# 設定不完整時
WARN  LINE 設定不完整，無法發送通知。請檢查 .env 檔案

# 功能停用時
INFO  LINE 通知已停用，跳過發送
```

### 6.2 手動測試 API

使用 curl 直接測試 LINE Push Message API：

```bash
curl -X POST https://api.line.me/v2/bot/message/push \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer 你的Channel_Access_Token' \
  -d '{
    "to": "你的LINE_User_ID",
    "messages": [
      {
        "type": "text",
        "text": "測試訊息：LINE Messaging API 設定成功！"
      }
    ]
  }'
```

### 6.3 常見問題排除

| 問題 | 可能原因 | 解決方式 |
|------|---------|---------|
| 收不到通知 | Token 過期或無效 | 重新 Issue Channel Access Token |
| 401 Unauthorized | Token 設定錯誤 | 檢查環境變數是否正確設定 |
| 400 Bad Request | User ID 格式錯誤 | 確認 User ID 為 U 開頭的 33 字元 |
| 用戶未加好友 | Bot 未被加為好友 | 請用戶先加 Bot 為好友 |

---

## 參考資源

- [LINE Developers Console](https://developers.line.biz/console/)
- [LINE Messaging API 官方文件](https://developers.line.biz/en/docs/messaging-api/)
- [Push Message API Reference](https://developers.line.biz/en/reference/messaging-api/#send-push-message)
- [LINE Login 整合指南](https://developers.line.biz/en/docs/line-login/)

---

*文件建立日期：2026-01-27*
*適用專案：Pet Care System*
