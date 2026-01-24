/**
 * Pet Care Admin - API Client
 */

const API = {
    /**
     * Make an API request
     */
    async request(endpoint, options = {}) {
        const url = `${CONFIG.API_BASE_URL}${endpoint}`;

        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include' // For cookies/session
        };

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
        async login(username, password) {
            return API.request('/auth/login', {
                method: 'POST',
                body: JSON.stringify({ username, password })
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
        }
    },

    /**
     * Booking APIs
     */
    bookings: {
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
