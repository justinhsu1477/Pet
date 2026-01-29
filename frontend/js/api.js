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
     * Customer APIs
     */
    customers: {
        async getByUserId(userId) {
            return API.request(`/customers/user/${userId}`);
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
        },

        async getAvailable(date, startTime, endTime) {
            return API.request(`/sitters/available?date=${date}&startTime=${startTime}&endTime=${endTime}`);
        }
    },

    /**
     * Sitter Profile APIs (for logged-in sitters)
     */
    sitterProfile: {
        async getByUserId(userId) {
            return API.request(`/sitters/user/${userId}`);
        },

        async getBookings(sitterId) {
            return API.request(`/sitter/${sitterId}/bookings`);
        },

        async getPendingBookings(sitterId) {
            return API.request(`/sitter/${sitterId}/bookings/pending`);
        },

        async confirmBooking(sitterId, bookingId, response) {
            return API.request(`/sitter/${sitterId}/bookings/${bookingId}/confirm`, {
                method: 'POST',
                body: JSON.stringify({ response })
            });
        },

        async rejectBooking(sitterId, bookingId, reason) {
            return API.request(`/sitter/${sitterId}/bookings/${bookingId}/reject`, {
                method: 'POST',
                body: JSON.stringify({ reason })
            });
        },

        async completeBooking(sitterId, bookingId) {
            return API.request(`/sitter/${sitterId}/bookings/${bookingId}/complete`, {
                method: 'POST'
            });
        },

        async cancelBooking(sitterId, bookingId, reason) {
            return API.request(`/sitter/${sitterId}/bookings/${bookingId}/cancel`, {
                method: 'POST',
                body: JSON.stringify(reason)
            });
        },

        async getStatistics(sitterId) {
            return API.request(`/sitter/${sitterId}/statistics`);
        },

        async getAvailability(sitterId) {
            return API.request(`/sitter/${sitterId}/availability`);
        },

        async addAvailability(sitterId, data) {
            return API.request(`/sitter/${sitterId}/availability`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        async updateAvailability(sitterId, id, data) {
            return API.request(`/sitter/${sitterId}/availability/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },

        async deleteAvailability(sitterId, id) {
            return API.request(`/sitter/${sitterId}/availability/${id}`, {
                method: 'DELETE'
            });
        },

        async replyToRating(ratingId, sitterId, reply) {
            return API.request(`/ratings/${ratingId}/reply?sitterId=${sitterId}`, {
                method: 'POST',
                body: JSON.stringify(reply)
            });
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

        async create(data, userId, idempotencyKey) {
            const opts = {
                method: 'POST',
                body: JSON.stringify(data)
            };
            if (idempotencyKey) {
                opts.headers = { 'Idempotency-Key': idempotencyKey };
            }
            return API.request(`/bookings?userId=${userId}`, opts);
        },

        async cancel(id) {
            return API.request(`/bookings/${id}/cancel`, {
                method: 'POST'
            });
        },

        async updateStatus(id, targetStatus, reason = null) {
            return API.request(`/bookings/${id}/status`, {
                method: 'PUT',
                body: JSON.stringify({ targetStatus, reason })
            });
        }
    },

    /**
     * Sitter Profile APIs (sitter-facing)
     */
    sitterProfile: {
        async getByUserId(userId) {
            return API.request(`/sitters/user/${userId}`);
        },

        async getStatistics(sitterId) {
            return API.request(`/sitters/${sitterId}/statistics`);
        },

        async getBookings(sitterId) {
            return API.request(`/bookings/sitter/${sitterId}`);
        },

        async getPendingBookings(sitterId) {
            return API.request(`/bookings/sitter/${sitterId}/pending`);
        },

        async confirmBooking(sitterId, bookingId, response) {
            return API.request(`/bookings/${bookingId}/confirm`, {
                method: 'POST',
                body: JSON.stringify({ sitterId, response })
            });
        },

        async rejectBooking(sitterId, bookingId, reason) {
            return API.request(`/bookings/${bookingId}/reject`, {
                method: 'POST',
                body: JSON.stringify({ sitterId, reason })
            });
        },

        async completeBooking(sitterId, bookingId) {
            return API.request(`/bookings/${bookingId}/complete`, {
                method: 'POST',
                body: JSON.stringify({ sitterId })
            });
        },

        async cancelBooking(sitterId, bookingId, reason) {
            return API.request(`/bookings/${bookingId}/cancel`, {
                method: 'POST',
                body: JSON.stringify({ sitterId, reason })
            });
        },

        async getAvailability(sitterId) {
            return API.request(`/sitter/${sitterId}/availability`);
        },

        async addAvailability(sitterId, data) {
            return API.request(`/sitter/${sitterId}/availability`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        },

        async updateAvailability(sitterId, id, data) {
            return API.request(`/sitter/${sitterId}/availability/${id}`, {
                method: 'PUT',
                body: JSON.stringify(data)
            });
        },

        async deleteAvailability(sitterId, id) {
            return API.request(`/sitter/${sitterId}/availability/${id}`, {
                method: 'DELETE'
            });
        },

        async replyToRating(ratingId, sitterId, reply) {
            return API.request(`/ratings/${ratingId}/reply`, {
                method: 'POST',
                body: JSON.stringify({ sitterId, reply })
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
        },

        async getByBooking(bookingId) {
            return API.request(`/ratings/booking/${bookingId}`);
        },

        async getByUser(userId) {
            return API.request(`/ratings/user/${userId}`);
        },

        async create(data, userId) {
            return API.request(`/ratings?userId=${userId}`, {
                method: 'POST',
                body: JSON.stringify(data)
            });
        }
    }
};
