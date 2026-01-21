# Claude 專案配置目錄

這個目錄包含了 Claude Code 的專案上下文配置，讓 AI 助手能夠更好地理解專案結構。

## 檔案說明

### `PROJECT_CONTEXT.md`
完整的專案架構文檔，包含：
- 後端 Domain Models 詳細說明
- API 端點定義
- 前端架構說明
- 前後端資料模型對應
- 開發規範和注意事項

### `prompts/project-context.md`
簡化的專案上下文提示，會在每次對話時自動載入，讓 Claude 快速了解：
- 關鍵檔案位置
- 重要概念
- 當前已知問題
- 開發規範

## 使用方式

當您使用 Claude Code 時，AI 會自動讀取這些檔案來理解專案結構。

### 對於開發者

1. **保持文檔更新**: 當架構變更時，請更新這些文檔
2. **參考開發規範**: 開發前先查看文檔中的規範
3. **記錄重要決策**: 在文檔中記錄重要的架構決策

### 對於 Claude

在回答問題或生成代碼時，請參考這些文檔以確保：
- 前後端資料結構一致
- 遵循專案的命名規範
- 使用正確的設計模式

## 專案概覽

```
Pet Sitter 系統
├── 後端 (Spring Boot)
│   ├── Domain Models (JPA Entities)
│   ├── DTOs (Data Transfer Objects)
│   ├── Services (業務邏輯)
│   ├── Controllers (REST API)
│   └── Repositories (資料存取)
│
└── 前端 (Android)
    ├── Data Models (Kotlin data classes)
    ├── UI (Activities, Fragments)
    ├── ViewModels (MVVM)
    └── Adapters (RecyclerView)
```

## 快速連結

- **後端入口**: `/src/main/java/com/pet/`
- **Android 入口**: `/android-app/app/src/main/java/com/pet/android/`
- **API 文檔**: 查看 `PROJECT_CONTEXT.md` 的 API Endpoint 章節
- **資料庫結構**: 查看 `PROJECT_CONTEXT.md` 的資料庫關係圖

---
**維護者**: Development Team
**最後更新**: 2026-01-21
