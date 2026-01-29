/**
 * Pet Care Admin - Configuration
 */

const CONFIG = {
    // API Base URL - Nginx 反向代理 /api → 後端
    API_BASE_URL: window.location.origin + '/api',

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
