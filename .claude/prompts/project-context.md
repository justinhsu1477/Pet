# Pet Sitter 專案上下文

這個提示會自動載入，讓 Claude 理解專案的整體架構。

## 專案結構

這是一個寵物保母預約系統，包含：
1. **後端**: Spring Boot REST API (`/src/main/java/com/pet`)
2. **前端**: Android 應用 (`/android-app`)

## 關鍵檔案位置

### 後端 Domain Models
- `/src/main/java/com/pet/domain/` - 所有實體類別
  - `Sitter.java` - 保母（包含 averageRating, ratingCount）
  - `SitterRating.java` - 評價（加權計算，1-5星）
  - `Booking.java` - 預約訂單（有狀態流轉）
  - `Users.java` - 使用者
  - `Pet.java` - 寵物（抽象類，有 Dog/Cat 子類）

### 後端 DTOs
- `/src/main/java/com/pet/dto/`
  - `SitterDto.java` - 保母傳輸物件
  - `SitterRatingStatsDto.java` - 評分統計

### 後端 API
- `/src/main/java/com/pet/web/SitterController.java`
  - `GET /api/sitters` - 列表
  - `GET /api/sitters/{id}` - 單筆
  - POST/PUT/DELETE 端點

### Android Models
- `/android-app/app/src/main/java/com/pet/android/data/model/Sitter.kt`

### Android UI
- `/android-app/app/src/main/java/com/pet/android/ui/sitter/`
  - `SitterListActivity.kt` - 列表頁面
  - `SitterAdapter.kt` - RecyclerView 適配器
  - `SitterViewModel.kt` - MVVM ViewModel

### Android Layouts
- `/android-app/app/src/main/res/layout/`
  - `activity_sitter_list.xml` - 列表頁面佈局
  - `item_sitter.xml` - 列表項目佈局

## 重要概念

### 後端資料模型
- 使用 **UUID** 作為主鍵
- `Sitter` 包含反正規化的 `averageRating` 和 `ratingCount`
- `SitterRating` 有加權計算邏輯（總體40%, 專業25%, 溝通20%, 準時15%）
- `Booking` 有狀態機制（PENDING → CONFIRMED/REJECTED → COMPLETED/CANCELLED）

### API 回應格式
所有 API 都包裝在 `ApiResponse<T>`:
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

### Android 架構
- **MVVM** 架構模式
- **Hilt** 依賴注入
- **Coroutines** 非同步處理
- **ViewBinding** 視圖綁定
- **Resource<T>** 封裝 Loading/Success/Error 狀態

## 當前問題

1. **前後端不一致**:
   - 後端 `Sitter` 有評分欄位
   - Android `Sitter` 缺少評分欄位
   - `SitterDto` 也缺少評分欄位

2. **UI 需要改進**:
   - `item_sitter.xml` 沒有顯示評分
   - 缺少星級評分視覺元素

## 開發規範

### 修改後端時
- Domain models 在 `/src/main/java/com/pet/domain/`
- DTOs 在 `/src/main/java/com/pet/dto/`
- 記得更新 Service 的 `convertToDto()` 方法

### 修改 Android 時
- 使用 `@SerializedName` 標註 JSON 欄位
- 遵循 Material Design 3 設計規範
- 使用 Kotlin data class
- 處理 nullable 欄位（評分可能為 null）

### UI 設計原則
- 星級評分使用 `RatingBar`
- 評價數顯示格式：`(123 則評價)`
- 完成訂單數顯示：`已完成 45 次服務`
- 使用 `MaterialCardView` 作為列表項目容器

## 相關文檔
- 完整架構文檔：`/Pet/.claude/PROJECT_CONTEXT.md`

---
**請在修改代碼時參考這些資訊，確保前後端一致性！**
