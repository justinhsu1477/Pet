/**
 * Pet Care Admin - API Client
 */

const API = {
    /**
     * Make an API request
     */
    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;

        // Get access token from sessionStorage
        const accessToken = sessionStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);

        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                'X-Device-Type': CONFIG.DEVICE_TYPE
            },
            credentials: 'include' // For cookies/session
        };

        // Add Authorization header if token exists
        if (accessToken) {
            defaultOptions.headers['Authorization'] = `Bearer ${accessToken}`;
        }

        const mergedOptions = {
            ...defaultOptions,
            ...options,
            headers: {
                ...defaultOptions.headers,
                ...options.headers
            }
        };

        try {
            const response = await fetch(url, mergedOptions);

            // Handle 401 Unauthorized - token expired or invalid
            if (response.status === 401) {
                console.error('Token expired or invalid, redirecting to login...');
                sessionStorage.clear();
                window.location.href = 'index.html';
                throw new Error('登入已過期，請重新登入');
            }

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || `HTTP ${response.status}`);
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
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
