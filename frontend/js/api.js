/**
 * Pet Care Admin - API Client
 */

// Simple refresh concurrency lock to avoid multiple parallel refresh calls
let __refreshPromise = null;

async function refreshAccessToken() {
    if (__refreshPromise) {
        return __refreshPromise;
    }
    __refreshPromise = (async () => {
        const res = await fetch(`${CONFIG.API_BASE_URL}/auth/jwt/refresh`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Device-Type': 'WEB'
            },
            credentials: 'include'
        });
        const body = await res.json().catch(() => ({}));
        if (!res.ok || !body || body.success === false || !body.data || !body.data.accessToken) {
            throw new Error((body && body.message) || `HTTP ${res.status}`);
        }
        const newAccessToken = body.data.accessToken;
        sessionStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, newAccessToken);
        return newAccessToken;
    })();
    try {
        return await __refreshPromise;
    } finally {
        __refreshPromise = null;
    }
}

const API = {
    /**
     * Make an API request
     */
    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;

        const tokenKey = CONFIG.STORAGE_KEYS.ACCESS_TOKEN;
        const getToken = () => sessionStorage.getItem(tokenKey);

        const defaultHeaders = {
            'Content-Type': 'application/json',
            'X-Device-Type': 'WEB'
        };
        const baseOptions = { credentials: 'include' };

        const buildOptionsWithToken = (token) => ({
            ...baseOptions,
            ...options,
            headers: {
                ...defaultHeaders,
                ...(options.headers || {}),
                ...(token ? { Authorization: `Bearer ${token}` } : {})
            }
        });

        // 先嘗試請求一次
        let response = await fetch(url, buildOptionsWithToken(getToken()));

        // 如果 Access Token 過期，嘗試刷新一次並重試原請求
        if (response.status === 401 && endpoint !== '/auth/jwt/refresh') {
            try {
                const newToken = await refreshAccessToken();
                response = await fetch(url, buildOptionsWithToken(newToken));
            } catch (e) {
                // 刷新失敗，視為未登入
                sessionStorage.clear();
                window.location.href = 'index.html';
                throw e;
            }
        }

        // 嘗試解析 JSON，若失敗給空物件
        const data = await response.json().catch(() => ({}));

        if (!response.ok || (data && data.success === false)) {
            throw new Error((data && data.message) || `HTTP ${response.status}`);
        }

        return data;
    },

    /**
     * Auth APIs
     */
    auth: {
        async jwtLogin(username, password) {
            return API.request('/auth/jwt/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
            });
        },

        async logout() {
            return API.request('/auth/jwt/logout', {
                method: 'POST'
            });
        }
    },

    /**
     * User/Customer APIs
     */
    users: {
        async getAll() {
            // Note: There's no direct users endpoint, we use customers
            return API.request('/customers');
        },

        async getById(id) {
            return API.request(`/customers/${id}`);
        }
    },

    /**
     * Sitter APIs
     */
    sitters: {
        async getAll() {
            return API.request('/sitters');
        },

        async getAllWithRating() {
            return API.request('/sitters/with-rating');
        },

        async getById(id) {
            return API.request(`/sitters/${id}`);
        }
    },

    /**
     * Pet APIs
     */
    pets: {
        async getAll() {
            return API.request('/pets');
        },

        async getByUser(userId) {
            return API.request(`/pets/user/${userId}`);
        },

        async getById(id) {
            return API.request(`/pets/${id}`);
        },

        async delete(id) {
            return API.request(`/pets/${id}`, {
                method: 'DELETE'
            });
        }
    },

    /**
     * Dog APIs
     */
    dogs: {
        async getById(id) {
            return API.request(`/dogs/${id}`);
        },

        async create(data, userId) {
            return API.request(`/dogs?userId=${userId}`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        async update(id, data) {
            return API.request(`/dogs/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        }
    },

    /**
     * Cat APIs
     */
    cats: {
        async getById(id) {
            return API.request(`/cats/${id}`);
        },

        async create(data, userId) {
            return API.request(`/cats?userId=${userId}`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        async update(id, data) {
            return API.request(`/cats/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        }
    },

    /**
     * Booking APIs
     */
    bookings: {
        async getAll() {
            return API.request('/bookings');
        },

        async getByUser(userId) {
            return API.request(`/bookings/user/${userId}`);
        },

        async getBySitter(sitterId) {
            return API.request(`/bookings/sitter/${sitterId}`);
        },

        async getById(id) {
            return API.request(`/bookings/${id}`);
        },

        async updateStatus(id, targetStatus, reason = null) {
            return API.request(`/bookings/${id}/status`, {
                method: 'PUT',
                body: JSON.stringify({ targetStatus, reason })
            });
        }
    },

    /**
     * Rating APIs
     */
    ratings: {
        async getBySitter(sitterId) {
            return API.request(`/ratings/sitter/${sitterId}`);
        },

        async getStatsBySitter(sitterId) {
            return API.request(`/ratings/sitter/${sitterId}/stats`);
        }
    }
};
