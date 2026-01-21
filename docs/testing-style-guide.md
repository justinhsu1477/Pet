# Pet 專案測試風格指南

此文件記錄專案的測試撰寫規範，作為之後開發的參考。

## 測試框架

- **JUnit 5** - 測試框架
- **Mockito** - Mock 框架
- **AssertJ** - 斷言庫

## 測試結構

### 1. 類別命名
```
{被測試類別名稱}Test.java
```
範例：`CatServiceTest.java`, `DogServiceTest.java`

### 2. 使用 @Nested 分組
按 CRUD 操作分組，每個操作一個內部類別：

```java
@Nested
@DisplayName("Create 操作")
class CreateTests { ... }

@Nested
@DisplayName("Read 操作")
class ReadTests { ... }

@Nested
@DisplayName("Update 操作")
class UpdateTests { ... }

@Nested
@DisplayName("Delete 操作")
class DeleteTests { ... }
```

### 3. 測試方法命名
使用 `should` 開頭的描述性命名：
- `shouldCreateCat()` - 正向測試
- `shouldThrowExceptionWhenCatNotFound()` - 異常測試

### 4. @DisplayName 使用中文
讓測試報告更易讀：
```java
@DisplayName("應該成功建立貓咪")
@DisplayName("當貓咪不存在時應該拋出例外")
```

## 測試模式

### Given-When-Then 結構
每個測試方法使用註解分隔三個區塊：

```java
@Test
void shouldGetCatById() {
    // given - 準備測試資料和 Mock 行為
    given(catRepository.findById(testId)).willReturn(Optional.of(testCat));

    // when - 執行被測試的方法
    CatDto result = catService.getById(testId);

    // then - 驗證結果
    assertThat(result.id()).isEqualTo(testId);
}
```

### Mock 設定
使用 BDDMockito 的 `given()` 方法：
```java
given(repository.findById(id)).willReturn(Optional.of(entity));
given(repository.existsById(id)).willReturn(true);
given(repository.save(any(Entity.class))).willReturn(entity);
```

### 斷言使用 AssertJ
```java
assertThat(result).hasSize(1);
assertThat(result.name()).isEqualTo("小花");
assertThatThrownBy(() -> service.getById(unknownId))
    .isInstanceOf(ResourceNotFoundException.class);
```

## 測試類別模板

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("{Service名稱} 測試")
class XxxServiceTest {

    @Mock
    private XxxRepository repository;

    @InjectMocks
    private XxxService service;

    private Xxx testEntity;
    private XxxDto testDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        // 初始化測試資料
    }

    @Nested
    @DisplayName("Create 操作")
    class CreateTests {
        @Test
        @DisplayName("應該成功建立...")
        void shouldCreate() { ... }
    }

    // ... 其他 CRUD 測試
}
```

## CRUD 測試清單

每個 Service 至少需要以下測試：

| 操作 | 正向測試 | 異常測試 |
|------|----------|----------|
| Create | 成功建立 | - |
| Read (All) | 取得所有 | - |
| Read (ById) | 根據 ID 取得 | ID 不存在 |
| Update | 成功更新 | ID 不存在 |
| Delete | 成功刪除 | ID 不存在 |

## 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類別
mvn test -Dtest=CatServiceTest

# 執行特定測試方法
mvn test -Dtest=CatServiceTest#shouldCreateCat
```
