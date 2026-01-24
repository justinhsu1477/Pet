/**
 * Pet Care Admin - API Configuration
 * API 配置檔案
 */

const CONFIG = {
    // API Base URL
    API_BASE_URL: 'http://localhost:8080',

    // API 端點
    API_ENDPOINTS: {
        // 認證相關
        AUTH: {
            LOGIN: '/api/auth/jwt/login',
            REFRESH: '/api/auth/jwt/refresh',
            LOGOUT: '/api/auth/jwt/logout'
        },

        // 用戶相關
        CUSTOMERS: '/api/customers',

        // 保母相關
        SITTERS: '/api/sitters',
        SITTER_RATINGS: '/api/sitter-ratings',

        // 寵物相關
        PETS: '/api/pets',

        // 訂單相關
        BOOKINGS: '/api/bookings'
    },

    // 儲存鍵值
    STORAGE_KEYS: {
        ACCESS_TOKEN: 'pet_admin_access_token',
        REFRESH_TOKEN: 'pet_admin_refresh_token',
        USER: 'pet_admin_user'
    },

    // Token 刷新設定
    TOKEN_REFRESH_BUFFER: 60 * 1000, // 60秒緩衝時間

    // 請求超時設定
    REQUEST_TIMEOUT: 30000, // 30秒

    // 設備類型
    DEVICE_TYPE: 'WEB'
};
