/**
 * Pet Care Admin - Configuration
 */

const CONFIG = {
    // API Base URL - 自動偵測環境
    // 本機開發：前端 :3000、後端 :8080（不同 port）
    // Docker/正式：前後端同域名，透過 Nginx 反向代理 /api → 後端
    API_BASE_URL: window.location.port === '3000'
        ? window.location.protocol + '//' + window.location.hostname + ':8080/api'
        : window.location.origin + '/api',

    // Storage Keys
    STORAGE_KEYS: {
        USER: 'petcare_admin_user',
        ACCESS_TOKEN: 'petcare_admin_access_token'
    },

    // Device Type
    DEVICE_TYPE: 'WEB'
};

// Freeze config to prevent modification
Object.freeze(CONFIG);
Object.freeze(CONFIG.STORAGE_KEYS);
