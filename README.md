# Pet System

寵物照護服務預約系統 - 後端 API

### 啟動後端服務

#### QAS 環境 (測試用)

```bash
# 1. 啟動 MSSQL
docker compose --profile qas up -d

# 2. 初始化資料庫 (首次執行)
./docker/mssql/init-db.sh

# 3. 啟動應用
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=qas"
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

## 技術棧

- **後端**: Spring Boot 3.x, Spring Data JPA, Hibernate
- **建構工具**: Maven
- **資料庫**: H2 (Dev), MSSQL (QAS)
- **Android**: Kotlin, Hilt, Retrofit
- **其他**: Docker
