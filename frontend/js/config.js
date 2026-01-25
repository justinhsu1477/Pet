/**
 * Pet Care Admin - Configuration
 */

const CONFIG = {
    // API Base URL - 請根據您的後端位址修改
    API_BASE_URL: 'http://localhost:8080/api',

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
