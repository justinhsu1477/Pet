/**
 * Pet Care Admin - API Request Wrapper
 * API 請求包裝器 - 自動處理 JWT Token 和錯誤
 */

const API = {
    /**
     * 發送 API 請求
     * @param {string} endpoint - API 端點
     * @param {Object} options - fetch 選項
     * @param {boolean} retry - 是否為重試請求
     */
    async request(endpoint, options = {}, retry = false) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;
        const accessToken = Auth.getAccessToken();

        // 預設 headers
        const headers = {
            'Content-Type': 'application/json',
            'X-Device-Type': CONFIG.DEVICE_TYPE,
            ...options.headers
        };

        // 添加 Authorization header
        if (accessToken) {
            headers['Authorization'] = `Bearer ${accessToken}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers,
                timeout: CONFIG.REQUEST_TIMEOUT,
                // 允許發送 HttpOnly Cookie
                credentials: 'include'
            });

            // 處理 401 未授權錯誤
            if (response.status === 401 && !retry) {
                console.log('Token expired, attempting to refresh...');

                try {
                    // 嘗試刷新 token
                    await Auth.refreshToken();

                    // 重試原始請求
                    return await this.request(endpoint, options, true);
                } catch (refreshError) {
                    console.error('Token refresh failed, redirecting to login...');

                    // 刷新失敗,跳轉到登入頁
                    Auth.clearAuth();
                    window.location.href = 'index.html';
                    throw new Error('Session expired, please login again');
                }
            }

            // 處理其他 HTTP 錯誤
            if (!response.ok) {
                const error = await response.json().catch(() => ({
                    message: `HTTP error ${response.status}`
                }));
                throw new Error(error.message || `Request failed with status ${response.status}`);
            }

            // 返回成功響應
            return await response.json();
        } catch (error) {
            console.error('API request error:', error);
            throw error;
        }
    },

    /**
     * GET 請求
     */
    async get(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'GET' });
    },

    /**
     * POST 請求
     */
    async post(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'POST',
            body: JSON.stringify(data)
        });
    },

    /**
     * PUT 請求
     */
    async put(endpoint, data, options = {}) {
        return this.request(endpoint, {
            ...options,
            method: 'PUT',
            body: JSON.stringify(data)
        });
    },

    /**
     * DELETE 請求
     */
    async delete(endpoint, options = {}) {
        return this.request(endpoint, { ...options, method: 'DELETE' });
    },

    // ===== 用戶 API =====
    users: {
        getAll() {
            return API.get(`${CONFIG.API_ENDPOINTS.CUSTOMERS}?role=CUSTOMER`);
        },

        getById(id) {
            return API.get(`${CONFIG.API_ENDPOINTS.CUSTOMERS}/${id}`);
        },

        getByUser(userId) {
            return API.get(`${CONFIG.API_ENDPOINTS.CUSTOMERS}/user/${userId}`);
        }
    },

    // ===== 保母 API =====
    sitters: {
        getAll() {
            return API.get(CONFIG.API_ENDPOINTS.SITTERS);
        },

        getAllWithRating() {
            return API.get(`${CONFIG.API_ENDPOINTS.SITTERS}/with-rating`);
        },

        getById(id) {
            return API.get(`${CONFIG.API_ENDPOINTS.SITTERS}/${id}`);
        }
    },

    // ===== 寵物 API =====
    pets: {
        getAll() {
            return API.get(CONFIG.API_ENDPOINTS.PETS);
        },

        getById(id) {
            return API.get(`${CONFIG.API_ENDPOINTS.PETS}/${id}`);
        },

        getByUser(userId) {
            return API.get(`${CONFIG.API_ENDPOINTS.PETS}/user/${userId}`);
        }
    },

    // ===== 訂單 API =====
    bookings: {
        getAll() {
            return API.get(CONFIG.API_ENDPOINTS.BOOKINGS);
        },

        getById(id) {
            return API.get(`${CONFIG.API_ENDPOINTS.BOOKINGS}/${id}`);
        },

        getByUser(userId) {
            return API.get(`${CONFIG.API_ENDPOINTS.BOOKINGS}/user/${userId}`);
        },

        getBySitter(sitterId) {
            return API.get(`${CONFIG.API_ENDPOINTS.BOOKINGS}/sitter/${sitterId}`);
        }
    },

    // ===== 評分 API =====
    ratings: {
        getBySitter(sitterId) {
            return API.get(`${CONFIG.API_ENDPOINTS.SITTER_RATINGS}/sitter/${sitterId}`);
        },

        getStatsBySitter(sitterId) {
            return API.get(`${CONFIG.API_ENDPOINTS.SITTER_RATINGS}/sitter/${sitterId}/stats`);
        }
    }
};
