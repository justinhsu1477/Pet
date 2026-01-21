# Pet Sitter 專案架構文檔

## 專案概述
這是一個寵物保母預約系統，包含後端 Spring Boot API 和 Android 前端應用。

## 後端架構 (Spring Boot)

### 核心領域模型 (Domain Models)

#### 1. Sitter (保母)
**檔案位置**: `/src/main/java/com/pet/domain/Sitter.java`

**欄位**:
- `id`: UUID - 主鍵
- `name`: String - 保母姓名
- `phone`: String - 電話
- `email`: String - Email
- `experience`: String - 經驗描述 (最多500字)
- `averageRating`: Double - 平均評分（反正規化欄位，用於快速查詢）
- `ratingCount`: Integer - 評價總數
- `completedBookings`: Integer - 完成訂單數

**API Endpoint**: `/api/sitters`
- GET `/api/sitters` - 獲取所有保母列表
- GET `/api/sitters/{id}` - 獲取單一保母
- POST `/api/sitters` - 創建保母
- PUT `/api/sitters/{id}` - 更新保母
- DELETE `/api/sitters/{id}` - 刪除保母

#### 2. SitterRating (保母評價)
**檔案位置**: `/src/main/java/com/pet/domain/SitterRating.java`

**欄位**:
- `id`: UUID - 主鍵
- `booking`: Booking - 關聯的預約訂單（必須是 COMPLETED 狀態）
- `sitter`: Sitter - 被評價的保母
- `user`: Users - 評價者（飼主）
- `overallRating`: Integer - 總體評分 (1-5，必填)
- `professionalismRating`: Integer - 專業度評分 (1-5)
- `communicationRating`: Integer - 溝通評分 (1-5)
- `punctualityRating`: Integer - 準時性評分 (1-5)
- `comment`: String - 評價內容 (最多1000字)
- `sitterReply`: String - 保母回覆 (最多500字)
- `isAnonymous`: Boolean - 是否匿名評價
- `createdAt`: LocalDateTime - 創建時間
- `updatedAt`: LocalDateTime - 更新時間

**業務邏輯**:
- 一個 Booking 只能評價一次（唯一索引約束）
- 只有 COMPLETED 狀態的訂單才能評價
- 加權平均計算：總體 40%, 專業 25%, 溝通 20%, 準時 15%

#### 3. Booking (預約訂單)
**檔案位置**: `/src/main/java/com/pet/domain/Booking.java`

**欄位**:
- `id`: UUID - 主鍵
- `pet`: Pet - 寵物
- `sitter`: Sitter - 保母
- `user`: Users - 飼主
- `startTime`: LocalDateTime - 開始時間
- `endTime`: LocalDateTime - 結束時間
- `status`: BookingStatus - 訂單狀態
- `version`: Long - 樂觀鎖版本號
- `notes`: String - 預約備註 (最多500字)
- `sitterResponse`: String - 保母回覆/拒絕原因 (最多500字)
- `totalPrice`: Double - 服務費用

**狀態流轉**:
```
PENDING (待確認)
  ├─> CONFIRMED (已確認) - Sitter 接受
  ├─> REJECTED (已拒絕) - Sitter 拒絕
  └─> CANCELLED (已取消) - 飼主取消

CONFIRMED (已確認)
  ├─> COMPLETED (已完成) - 服務完成，可評價
  └─> CANCELLED (已取消) - Sitter 或飼主取消
```

#### 4. Users (使用者)
**檔案位置**: `/src/main/java/com/pet/domain/Users.java`

**欄位**:
- `id`: UUID - 主鍵
- `username`: String - 使用者名稱（唯一）
- `password`: String - 密碼
- `email`: String - Email
- `phone`: String - 電話
- `role`: String - 角色

#### 5. Pet (寵物 - 抽象類)
**檔案位置**: `/src/main/java/com/pet/domain/Pet.java`

**繼承策略**: JOINED (每個子類有自己的表)

**欄位**:
- `id`: UUID - 主鍵
- `name`: String - 寵物名稱
- `age`: Integer - 年齡
- `breed`: String - 品種
- `gender`: Gender - 性別 (MALE/FEMALE)
- `ownerName`: String - 飼主姓名
- `ownerPhone`: String - 飼主電話
- `specialNeeds`: String - 特殊需求 (最多500字)
- `isNeutered`: Boolean - 是否已絕育
- `vaccineStatus`: String - 疫苗狀態

**子類**:
- `Dog.java` - 狗
- `Cat.java` - 貓

#### 6. SitterRecord (保母照護記錄)
**檔案位置**: `/src/main/java/com/pet/domain/SitterRecord.java`

**欄位**:
- `id`: UUID - 主鍵
- `pet`: Pet - 寵物
- `sitter`: Sitter - 保母
- `recordTime`: LocalDateTime - 記錄時間
- `activity`: String - 活動
- `fed`: Boolean - 是否餵食
- `walked`: Boolean - 是否遛狗
- `moodStatus`: String - 心情狀態
- `notes`: String - 備註 (最多1000字)
- `photos`: String - 照片 (最多500字)

### DTO (數據傳輸物件)

#### SitterDto
**檔案位置**: `/src/main/java/com/pet/dto/SitterDto.java`

```java
public record SitterDto(
    UUID id,
    String name,      // @NotBlank, @Size(max=100)
    String phone,     // @NotBlank, @Pattern, @Size(max=20)
    String email,     // @Email, @Size(max=100)
    String experience // @Size(max=500)
)
```

**注意**: 目前 DTO 不包含評分資料，需要擴充！

#### SitterRatingStatsDto
**檔案位置**: `/src/main/java/com/pet/dto/SitterRatingStatsDto.java`

```java
public record SitterRatingStatsDto(
    UUID sitterId,
    String sitterName,
    Double averageRating,
    Double averageProfessionalism,
    Double averageCommunication,
    Double averagePunctuality,
    Integer totalRatings,
    Integer completedBookings,
    // 評分分佈
    Integer fiveStarCount,
    Integer fourStarCount,
    Integer threeStarCount,
    Integer twoStarCount,
    Integer oneStarCount
)
```

### API Response 結構
**檔案位置**: `/src/main/java/com/pet/dto/response/ApiResponse.java`

所有 API 回應都包裝在 `ApiResponse<T>` 中：
```json
{
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

## Android 前端架構

### 當前模型 (需要更新)
**檔案位置**: `/android-app/app/src/main/java/com/pet/android/data/model/Sitter.kt`

```kotlin
data class Sitter(
    val id: String?,
    val name: String,
    val phone: String,
    val email: String?,
    val experience: String?
)
```

**問題**: 缺少評分相關欄位！

### 建議的新模型結構

```kotlin
data class Sitter(
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("experience")
    val experience: String?,

    // 新增評分欄位
    @SerializedName("averageRating")
    val averageRating: Double? = null,

    @SerializedName("ratingCount")
    val ratingCount: Int? = null,

    @SerializedName("completedBookings")
    val completedBookings: Int? = null
)
```

### UI 元件
**檔案位置**:
- Activity: `/android-app/app/src/main/java/com/pet/android/ui/sitter/SitterListActivity.kt`
- Adapter: `/android-app/app/src/main/java/com/pet/android/ui/sitter/SitterAdapter.kt`
- Layout: `/android-app/app/src/main/res/layout/item_sitter.xml`

## 前後端整合重點

### 1. Sitter 列表 API 需要擴充
**當前問題**: `SitterDto` 不包含評分資訊

**解決方案選項**:
1. 修改 `SitterDto` 加入評分欄位
2. 創建新的 `SitterListItemDto` 專門用於列表顯示
3. 使用 `SitterRatingStatsDto` 作為列表回應

### 2. Android UI 設計建議
使用 Material Design 3 組件展示：
- 保母名稱、聯絡資訊
- 星級評分 (RatingBar)
- 評價數量
- 完成訂單數
- 經驗描述
- 認證徽章（如適用）

### 3. API 調用流程
```
Android App -> GET /api/sitters
            <- ApiResponse<List<SitterDto>> (需包含評分)
            -> 顯示在 RecyclerView
            -> 點擊項目 -> SitterRatingActivity (已存在)
```

## 開發規範

### 後端
- 使用 Spring Boot 3.x
- JPA/Hibernate 作為 ORM
- UUID 作為主鍵
- Record 作為 DTO (Java 17+)
- 樂觀鎖處理併發

### 前端
- Kotlin
- MVVM 架構
- Hilt 依賴注入
- Coroutines 處理非同步
- ViewBinding
- Material Design 3

## 待辦事項

### 後端改進
1. 修改 `SitterService.convertToDto()` 包含評分資料
2. 考慮新增 `GET /api/sitters/{id}/rating-stats` 端點

### Android 改進
1. 更新 `Sitter.kt` 模型加入評分欄位
2. 重新設計 `item_sitter.xml` 展示評分
3. 更新 `SitterAdapter` 綁定評分 UI

## 相關檔案清單

### 後端核心檔案
```
/src/main/java/com/pet/
├── domain/
│   ├── Sitter.java
│   ├── SitterRating.java
│   ├── Booking.java
│   ├── Users.java
│   ├── Pet.java
│   ├── Dog.java
│   ├── Cat.java
│   ├── SitterRecord.java
│   └── SitterAvailability.java
├── dto/
│   ├── SitterDto.java
│   ├── SitterRatingDto.java
│   ├── SitterRatingStatsDto.java
│   └── response/ApiResponse.java
├── service/
│   ├── SitterService.java
│   ├── SitterRatingService.java
│   └── SitterRecordService.java
├── web/
│   └── SitterController.java
└── repository/
    ├── SitterRepository.java
    └── SitterRatingRepository.java
```

### Android 核心檔案
```
/android-app/app/src/main/java/com/pet/android/
├── data/model/
│   └── Sitter.kt
├── ui/sitter/
│   ├── SitterListActivity.kt
│   ├── SitterAdapter.kt
│   ├── SitterViewModel.kt
│   └── rating/
│       └── SitterRatingActivity.kt
└── util/
    └── Resource.kt
```

### Layout 檔案
```
/android-app/app/src/main/res/layout/
├── activity_sitter_list.xml
└── item_sitter.xml
```

## 資料庫關係圖

```
Users (飼主)
  |
  ├─> Pet (寵物) ─┐
  |               |
  └─> Booking <───┘
        |
        ├─> Sitter (保母)
        |     |
        |     ├─> SitterRating (評價)
        |     ├─> SitterRecord (照護記錄)
        |     └─> SitterAvailability (可用時段)
        |
        └─> SitterRating (一對一)
```

## 注意事項

1. **UUID vs String**: 後端使用 UUID，Android 使用 String，需要序列化處理
2. **評分計算**: 後端已實作加權平均，前端只需顯示
3. **權限控制**: 只有管理員可以進入 SitterRatingActivity (在 SitterListActivity:41-46)
4. **狀態管理**: 使用 `Resource<T>` 封裝 Loading/Success/Error 狀態
5. **反正規化欄位**: `Sitter.averageRating` 和 `ratingCount` 應在每次評價後更新

## 測試重點

### 後端
- 評價只能針對 COMPLETED 訂單
- 一個訂單只能評價一次
- 平均分計算正確性
- 併發更新處理（樂觀鎖）

### Android
- 評分顯示正確（處理 null 情況）
- 列表性能（大量資料）
- 網路錯誤處理
- 權限檢查

---
**文檔版本**: 1.0
**最後更新**: 2026-01-21
**維護者**: Development Team
