/**
 * Pet Care Admin - Login Page Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check if already logged in
    const user = sessionStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    if (user) {
        window.location.href = 'dashboard.html';
        return;
    }

    // Get form elements
    const loginForm = document.getElementById('loginForm');
    const loginError = document.getElementById('loginError');
    const loginBtn = document.getElementById('loginBtn');
    const loginBtnText = document.getElementById('loginBtnText');
    const loginSpinner = document.getElementById('loginSpinner');

    // Handle form submission
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Hide previous error
        loginError.classList.remove('show');

        // Get form values
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;

        if (!username || !password) {
            showError('請輸入帳號和密碼');
            return;
        }

        // Show loading state
        setLoading(true);

        try {
            const response = await API.auth.jwtLogin(username, password);

            if (response.success && response.data) {
                const jwtData = response.data;

                // Store access token
                sessionStorage.setItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN, jwtData.accessToken);

                // Store user data
                const userData = {
                    userId: jwtData.userId,
                    username: jwtData.username,
                    role: jwtData.role,
                    roleId: jwtData.roleId,
                    roleName: jwtData.roleName,
                    email: jwtData.email,
                    phone: jwtData.phone
                };
                sessionStorage.setItem(CONFIG.STORAGE_KEYS.USER, JSON.stringify(userData));

                console.log('登入成功:', userData);

                // Redirect to dashboard
                window.location.href = 'dashboard.html';
            } else {
                showError(response.message || '登入失敗');
            }
        } catch (error) {
            console.error('Login error:', error);
            showError('帳號或密碼錯誤，請重試');
        } finally {
            setLoading(false);
        }
    });

    function showError(message) {
        loginError.textContent = message;
        loginError.classList.add('show');
    }

    function setLoading(loading) {
        loginBtn.disabled = loading;
        loginBtnText.style.display = loading ? 'none' : 'inline';
        loginSpinner.style.display = loading ? 'block' : 'none';
    }
});
