# Pet System

寵物照護服務預約系統 - 後端 API

## 快速開始

### 前置需求

- Java 17
- Maven 3.x
- Docker (僅 QAS 環境需要)

### 啟動後端服務

#### Dev 環境 (開發用)

使用 H2 檔案資料庫,不需要 Docker:

```bash
mvn spring-boot:run
```

或指定 profile:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

服務啟動後會顯示:
```
========================================
🚀 Pet System Started Successfully!
========================================
📌 Active Profile: DEV
📊 Primary DB: jdbc:h2:file:./data/petdb
📝 Log DB: jdbc:h2:file:./data/petdb_log
========================================
```

訪問:
- API: http://localhost:8080
- H2 Console: http://localhost:8080/h2-console

#### QAS 環境 (測試用)

使用 MSSQL 資料庫,需要 Docker:

```bash
# 1. 啟動 MSSQL
docker compose --profile qas up -d

# 2. 初始化資料庫 (首次執行)
./docker/mssql/init-db.sh

# 3. 啟動應用
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=qas"
```

服務啟動後會顯示:
```
========================================
🚀 Pet System Started Successfully!
========================================
📌 Active Profile: QAS
📊 Primary DB: jdbc:sqlserver://localhost:1433;databaseName=petdb
📝 Log DB: jdbc:sqlserver://localhost:1433;databaseName=petdb_log
========================================
```

### 停止服務

#### 停止 Spring Boot
按 `Ctrl+C` 終止應用程式

#### 停止 Docker (QAS 環境)
```bash
docker compose --profile qas down
```

## 專案架構

```
Pet/
├── src/main/java/com/pet/          # 後端 Java 程式碼
│   ├── config/                     # 資料庫配置
│   ├── domain/                     # Primary DB 實體
│   ├── repository/                 # Primary DB Repository
│   ├── log/                        # Log DB 相關
│   │   ├── domain/                 # Log 實體
│   │   └── repository/             # Log Repository
│   └── web/                        # REST Controllers
├── src/main/resources/
│   ├── application.yml             # 主配置
│   ├── application-dev.yml         # Dev 環境配置
│   ├── application-qas.yml         # QAS 環境配置
│   └── db/                         # 資料庫腳本
├── android-app/                    # Android 客戶端
└── docker/                         # Docker 相關檔案

```

## 資料庫說明

本系統使用**雙資料庫架構**:
- **Primary DB**: 主要業務資料 (用戶、寵物、預約等)
- **Log DB**: 預約操作日誌 (用於報表分析)

| 環境 | Primary DB | Log DB |
|------|-----------|--------|
| Dev  | H2 檔案資料庫 | H2 檔案資料庫 |
| QAS  | MSSQL | MSSQL |

詳細資料庫設定請參考 [DATABASE_SETUP.md](DATABASE_SETUP.md)

## API 文件

Sitter Booking API 文件: [SITTER_BOOKING_API_DOC.md](SITTER_BOOKING_API_DOC.md)

## 常見問題

### 如何重置 Dev 環境資料?

```bash
# 刪除 H2 資料庫檔案
rm -rf ./data

# 重新啟動應用即可自動重建
mvn spring-boot:run
```

### 如何查看當前執行環境?

應用程式啟動時會在 log 中顯示當前環境資訊,包括:
- Active Profile (dev/qas)
- Primary Database URL
- Log Database URL

### 如何切換環境?

使用 `--spring.profiles.active` 參數:

```bash
# Dev 環境
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# QAS 環境
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=qas"
```

## 開發相關

### 編譯專案

```bash
mvn clean install
```

### 執行測試

```bash
mvn test
```

### 打包 JAR

```bash
mvn clean package
```

執行打包後的 JAR:
```bash
java -jar target/practice-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### Android App 編譯

```bash
cd android-app
./gradlew assembleDebug
```

## 技術棧

- **後端**: Spring Boot 3.x, Spring Data JPA, Hibernate
- **建構工具**: Maven
- **資料庫**: H2 (Dev), MSSQL (QAS)
- **Android**: Kotlin, Hilt, Retrofit
- **其他**: Docker
