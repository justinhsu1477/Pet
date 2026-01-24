/**
 * Pet Care Admin - Authentication Module
 * JWT 認證模組
 */

const Auth = {
    // 記憶體中的 Access Token
    _accessToken: null,

    /**
     * 登入
     * @param {string} username - 使用者名稱
     * @param {string} password - 密碼
     * @returns {Promise<Object>} 登入結果
     */
    async login(username, password) {
        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}${CONFIG.API_ENDPOINTS.AUTH.LOGIN}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Device-Type': CONFIG.DEVICE_TYPE
                },
                body: JSON.stringify({ username, password }),
                // 允許接收 HttpOnly Cookie
                credentials: 'include'
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: '登入失敗' }));
                throw new Error(error.message || '登入失敗');
            }

            const data = await response.json();

            // 儲存 Access Token 到記憶體
            this._accessToken = data.data.accessToken;

            // 儲存用戶資訊
            this.saveUser(data.data.user);

            return data;
        } catch (error) {
            console.error('Login error:', error);
            throw error;
        }
    },

    /**
     * 登出
     */
    async logout() {
        try {
            // 呼叫後端登出 API (不需要傳送 refreshToken,後端會從 Cookie 讀取)
            await fetch(`${CONFIG.API_BASE_URL}${CONFIG.API_ENDPOINTS.AUTH.LOGOUT}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Device-Type': CONFIG.DEVICE_TYPE
                },
                credentials: 'include'
            });
        } catch (error) {
            console.error('Logout API error:', error);
        } finally {
            // 無論 API 成功與否,都清除本地資料
            this.clearAuth();
        }
    },

    /**
     * 刷新 Token
     * @returns {Promise<string>} 新的 access token
     */
    async refreshToken() {
        try {
            const response = await fetch(`${CONFIG.API_BASE_URL}${CONFIG.API_ENDPOINTS.AUTH.REFRESH}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Device-Type': CONFIG.DEVICE_TYPE
                },
                // 不需要 body,後端從 Cookie 讀取 Refresh Token
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Token refresh failed');
            }

            const data = await response.json();

            // 更新記憶體中的 Access Token
            this._accessToken = data.data.accessToken;

            return data.data.accessToken;
        } catch (error) {
            console.error('Token refresh error:', error);
            // 刷新失敗,清除認證資料
            this.clearAuth();
            throw error;
        }
    },

    /**
     * 儲存 tokens
     * @deprecated 使用記憶體和 Cookie 存儲
     */
    saveTokens(accessToken, refreshToken) {
        this._accessToken = accessToken;
    },

    /**
     * 儲存 access token
     */
    saveAccessToken(accessToken) {
        this._accessToken = accessToken;
    },

    /**
     * 儲存用戶資訊
     */
    saveUser(user) {
        localStorage.setItem(CONFIG.STORAGE_KEYS.USER, JSON.stringify(user));
    },

    /**
     * 獲取 access token
     */
    getAccessToken() {
        return this._accessToken;
    },

    /**
     * 獲取 refresh token
     * @deprecated Refresh Token 現在存放在 HttpOnly Cookie 中，JS 無法存取
     */
    getRefreshToken() {
        return null;
    },

    /**
     * 獲取用戶資訊
     */
    getUser() {
        const userStr = localStorage.getItem(CONFIG.STORAGE_KEYS.USER);
        return userStr ? JSON.parse(userStr) : null;
    },

    /**
     * 檢查是否已登入
     */
    isAuthenticated() {
        // 由於 Access Token 在刷新頁面後會消失，
        // 真實的檢查可能需要嘗試呼叫 refreshToken API 或檢查 User 資訊是否存在
        return !!this._accessToken || !!this.getUser();
    },

    /**
     * 檢查是否為管理員
     */
    isAdmin() {
        const user = this.getUser();
        return user && user.role === 'ADMIN';
    },

    /**
     * 清除認證資料
     */
    clearAuth() {
        this._accessToken = null;
        localStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
        // localStorage 中的舊 token 也一併清除(遷移用)
        localStorage.removeItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
        localStorage.removeItem(CONFIG.STORAGE_KEYS.REFRESH_TOKEN);
    },

    /**
     * 解析 JWT Token (簡單版本,不驗證簽名)
     */
    parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));

            return JSON.parse(jsonPayload);
        } catch (error) {
            console.error('JWT parse error:', error);
            return null;
        }
    },

    /**
     * 檢查 token 是否即將過期
     */
    isTokenExpiringSoon(token) {
        const payload = this.parseJwt(token);
        if (!payload || !payload.exp) {
            return true;
        }

        const expirationTime = payload.exp * 1000; // 轉換為毫秒
        const currentTime = Date.now();
        const timeUntilExpiry = expirationTime - currentTime;

        // 如果剩餘時間少於緩衝時間,視為即將過期
        return timeUntilExpiry < CONFIG.TOKEN_REFRESH_BUFFER;
    }
};
