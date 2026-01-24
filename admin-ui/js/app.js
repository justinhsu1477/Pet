/**
 * Pet Care Admin - Main Application
 */

document.addEventListener('DOMContentLoaded', () => {
    // Check authentication
    const user = sessionStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    if (!user) {
        window.location.href = 'index.html';
        return;
    }

    const userData = JSON.parse(user);
    if (userData.role !== 'ADMIN') {
        window.location.href = 'index.html';
        return;
    }

    // Set user info in sidebar
    document.getElementById('userName').textContent = userData.roleName || userData.username;
    document.getElementById('userAvatar').textContent = (userData.roleName || userData.username).charAt(0).toUpperCase();

    // Initialize app
    App.init();
});

const App = {
    currentPage: 'dashboard',

    init() {
        this.setupNavigation();
        this.setupLogout();
        this.setupModals();
        this.loadDashboard();
    },

    // ===== Navigation =====
    setupNavigation() {
        const navItems = document.querySelectorAll('.nav-item[data-page]');
        navItems.forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                const page = item.dataset.page;
                this.navigateTo(page);
            });
        });
    },

    navigateTo(page) {
        // Update nav active state
        document.querySelectorAll('.nav-item[data-page]').forEach(item => {
            item.classList.toggle('active', item.dataset.page === page);
        });

        // Show/hide pages
        document.querySelectorAll('.page').forEach(p => {
            p.style.display = 'none';
        });
        document.getElementById(`page-${page}`).style.display = 'block';

        this.currentPage = page;

        // Load page data
        switch (page) {
            case 'dashboard':
                this.loadDashboard();
                break;
            case 'users':
                this.loadUsers();
                break;
            case 'sitters':
                this.loadSitters();
                break;
        }
    },

    // ===== Logout =====
    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
            window.location.href = 'index.html';
        });
    },

    // ===== Modals =====
    setupModals() {
        document.querySelectorAll('.modal-close, .modal-backdrop').forEach(el => {
            el.addEventListener('click', () => {
                document.querySelectorAll('.modal').forEach(m => m.style.display = 'none');
            });
        });
    },

    showModal(modalId) {
        document.getElementById(modalId).style.display = 'flex';
    },

    hideModal(modalId) {
        document.getElementById(modalId).style.display = 'none';
    },

    // ===== Dashboard =====
    async loadDashboard() {
        try {
            // Load stats with improved error handling for users
            const [sittersRes, petsRes, usersRes] = await Promise.all([
                API.sitters.getAllWithRating(),
                API.pets.getAll(),
                API.users.getAll().catch(err => {
                    console.error('Failed to load users count:', err);
                    return { data: [] };
                })
            ]);

            const sitters = sittersRes.data || [];
            const pets = petsRes.data || [];
            const users = usersRes.data || [];

            // Update stats
            document.getElementById('stat-sitters').textContent = sitters.length;
            document.getElementById('stat-pets').textContent = pets.length;
            document.getElementById('stat-users').textContent = users.length;

            // Update sitters table
            this.renderDashboardSitters(sitters.slice(0, 5));
        } catch (error) {
            console.error('Dashboard load error:', error);
        }
    },

    renderDashboardSitters(sitters) {
        const tbody = document.getElementById('dashboard-sitters-table');

        if (!sitters.length) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">暫無保母資料</td></tr>';
            return;
        }

        tbody.innerHTML = sitters.map(sitter => `
            <tr>
                <td>
                    <div class="flex items-center gap-1">
                        <div class="avatar avatar-sm">${(sitter.name || '?').charAt(0)}</div>
                        <span>${sitter.name || '未知'}</span>
                    </div>
                </td>
                <td>${sitter.specialties || '-'}</td>
                <td>
                    <div class="rating">
                        ⭐ <span class="rating-value">${sitter.averageRating?.toFixed(1) || '-'}</span>
                    </div>
                </td>
                <td><span class="badge badge-success">活躍</span></td>
            </tr>
        `).join('');
    },

    // ===== Users =====
    async loadUsers() {
        const tbody = document.getElementById('users-table');
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">載入中...</td></tr>';

        try {
            // 呼叫新的 Customer API
            const res = await API.users.getAll();
            const customers = res.data || [];

            console.log('API Response for Users:', customers); // 除錯用

            if (!customers.length) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">暫無使用者資料</td></tr>';
                return;
            }

            tbody.innerHTML = customers.map(customer => `
                <tr>
                    <td>
                        <div class="flex items-center gap-1">
                            <div class="avatar avatar-sm">${(customer.name || '?').charAt(0)}</div>
                            <div>
                                <div>${customer.name || '未知'}</div>
                                <div class="text-muted" style="font-size: 0.75rem;">@${customer.username || '-'}</div>
                            </div>
                        </div>
                    </td>
                    <td>${customer.email || '-'}</td>
                    <td>${customer.phone || '-'}</td>
                    <td>
                        <button class="btn btn-ghost" onclick="App.viewUserDetail('${customer.id || ''}', '${customer.userId || ''}')">
                            查看詳情
                        </button>
                    </td>
                </tr>
            `).join('');

            // Also update dashboard stat
            document.getElementById('stat-users').textContent = customers.length;
        } catch (error) {
            console.error('Users load error:', error);
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">載入失敗: ' + error.message + '</td></tr>';
        }
    },

    async viewUserDetail(customerId, userId) {
        const contentEl = document.getElementById('user-detail-content');
        const nameEl = document.getElementById('user-detail-name');

        contentEl.innerHTML = '<p class="text-muted">載入中...</p>';
        this.showModal('user-detail-modal');

        try {
            // 根據是否有 customerId 來決定怎麼查
            let customerDetailRes;
            if (customerId && customerId !== 'null') {
                customerDetailRes = await API.users.getById(customerId);
            } else if (userId && userId !== 'null') {
                // 如果沒有 customerId (應該不會發生了，因為後端有補資料)，就用 userId 查
                // 但 API.users.getById 預期是 customerId
                // 如果後端有提供 /api/customers/user/{userId}，可以用那個
                customerDetailRes = { data: { name: '未知', ... } }; // 暫時 fallback
            }

            const customer = customerDetailRes?.data;

            // 載入寵物和訂單 (使用 userId)
            let pets = [];
            let bookings = [];

            if (userId && userId !== 'null') {
                try {
                    const [petsRes, bookingsRes] = await Promise.all([
                        API.pets.getByUser(userId),
                        API.bookings.getByUser(userId)
                    ]);
                    pets = petsRes.data || [];
                    bookings = bookingsRes.data || [];
                } catch (e) {
                    console.error('Error fetching details:', e);
                }
            }

            nameEl.textContent = customer?.name || '使用者詳情';

            contentEl.innerHTML = `
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
                    <div>
                        <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">基本資訊</h4>
                        <p><strong>姓名：</strong>${customer?.name || '-'}</p>
                        <p><strong>Email：</strong>${customer?.email || '-'}</p>
                        <p><strong>電話：</strong>${customer?.phone || '-'}</p>
                        <p><strong>地址：</strong>${customer?.address || '-'}</p>
                        <p><strong>會員等級：</strong>${customer?.memberLevel || '-'}</p>
                    </div>
                    <div>
                        <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">預約統計</h4>
                        <p><strong>總預約數：</strong>${customer?.totalBookings || 0}</p>
                        <p><strong>總消費：</strong>NT$ ${customer?.totalSpent?.toFixed(0) || 0}</p>
                        <p><strong>緊急聯絡人：</strong>${customer?.emergencyContact || '-'}</p>
                        <p><strong>緊急電話：</strong>${customer?.emergencyPhone || '-'}</p>
                    </div>
                </div>
                
                <h4 style="margin: 1.5rem 0 0.5rem; color: var(--color-primary);">寵物 (${pets.length})</h4>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    ${pets.map(pet => `
                        <span class="badge badge-info">${pet.type === 'DOG' ? '🐕' : '🐈'} ${pet.name}</span>
                    `).join('') || '<span class="text-muted">無寵物</span>'}
                </div>
                
                <h4 style="margin: 1.5rem 0 0.5rem; color: var(--color-primary);">最近訂單</h4>
                <div class="table-container" style="box-shadow: none; max-height: 200px; overflow-y: auto;">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>寵物</th>
                                <th>保母</th>
                                <th>時間</th>
                                <th>狀態</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${bookings.slice(0, 5).map(b => `
                                <tr>
                                    <td>${b.petName || '-'}</td>
                                    <td>${b.sitterName || '-'}</td>
                                    <td>${this.formatDate(b.startTime)}</td>
                                    <td><span class="badge ${this.getStatusBadgeClass(b.status)}">${this.getStatusText(b.status)}</span></td>
                                </tr>
                            `).join('') || '<tr><td colspan="4" class="text-center text-muted">暫無訂單</td></tr>'}
                        </tbody>
                    </table>
                </div>
            `;
        } catch (error) {
            console.error('User detail error:', error);
            contentEl.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    // ===== Sitters =====
    async loadSitters() {
        const tbody = document.getElementById('sitters-table');
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">載入中...</td></tr>';

        try {
            const res = await API.sitters.getAllWithRating();
            const sitters = res.data || [];

            if (!sitters.length) {
                tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">暫無保母資料</td></tr>';
                return;
            }

            tbody.innerHTML = sitters.map(sitter => `
                <tr>
                    <td>
                        <div class="flex items-center gap-1">
                            <div class="avatar avatar-sm">${(sitter.name || '?').charAt(0)}</div>
                            <span>${sitter.name || '未知'}</span>
                        </div>
                    </td>
                    <td>${sitter.specialties || '-'}</td>
                    <td>
                        <div class="rating">
                            ⭐ <span class="rating-value">${sitter.averageRating?.toFixed(1) || '-'}</span>
                            <span class="text-muted">(${sitter.ratingCount || 0})</span>
                        </div>
                    </td>
                    <td>${sitter.completedBookings || 0}</td>
                    <td>
                        <button class="btn btn-ghost" onclick="App.viewSitterDetail('${sitter.id}')">
                            查看詳情
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            console.error('Sitters load error:', error);
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted">載入失敗</td></tr>';
        }
    },

    async viewSitterDetail(sitterId) {
        const contentEl = document.getElementById('sitter-detail-content');
        const nameEl = document.getElementById('sitter-detail-name');

        contentEl.innerHTML = '<p class="text-muted">載入中...</p>';
        this.showModal('sitter-detail-modal');

        try {
            const [sitterRes, bookingsRes, ratingsRes] = await Promise.all([
                API.sitters.getById(sitterId),
                API.bookings.getBySitter(sitterId),
                API.ratings.getStatsBySitter(sitterId)
            ]);

            const sitter = sitterRes.data;
            const bookings = bookingsRes.data || [];
            const ratingStats = ratingsRes.data;

            nameEl.textContent = sitter?.name || '保母詳情';

            contentEl.innerHTML = `
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem;">
                    <div>
                        <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">基本資訊</h4>
                        <p><strong>姓名：</strong>${sitter?.name || '-'}</p>
                        <p><strong>專長：</strong>${sitter?.specialties || '-'}</p>
                        <p><strong>經驗：</strong>${sitter?.experienceYears || 0} 年</p>
                        <p><strong>簡介：</strong>${sitter?.bio || '-'}</p>
                    </div>
                    <div>
                        <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">評分統計</h4>
                        <p><strong>平均評分：</strong>⭐ ${ratingStats?.averageRating?.toFixed(1) || '-'}</p>
                        <p><strong>評價數量：</strong>${ratingStats?.totalRatings || 0}</p>
                        <p><strong>完成訂單：</strong>${ratingStats?.completedBookings || 0}</p>
                    </div>
                </div>
                
                <h4 style="margin: 1.5rem 0 0.5rem; color: var(--color-primary);">最近訂單</h4>
                <div class="table-container" style="box-shadow: none; max-height: 200px; overflow-y: auto;">
                    <table class="table">
                        <thead>
                            <tr>
                                <th>寵物</th>
                                <th>時間</th>
                                <th>狀態</th>
                            </tr>
                        </thead>
                        <tbody>
                            ${bookings.slice(0, 5).map(b => `
                                <tr>
                                    <td>${b.petName || '-'}</td>
                                    <td>${this.formatDate(b.startTime)}</td>
                                    <td><span class="badge ${this.getStatusBadgeClass(b.status)}">${this.getStatusText(b.status)}</span></td>
                                </tr>
                            `).join('') || '<tr><td colspan="3" class="text-center text-muted">暫無訂單</td></tr>'}
                        </tbody>
                    </table>
                </div>
            `;
        } catch (error) {
            console.error('Sitter detail error:', error);
            contentEl.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    // ===== Helpers =====
    formatDate(dateStr) {
        if (!dateStr) return '-';
        try {
            const date = new Date(dateStr);
            return date.toLocaleDateString('zh-TW', { year: 'numeric', month: '2-digit', day: '2-digit' });
        } catch {
            return dateStr.substring(0, 10);
        }
    },

    getStatusText(status) {
        const map = {
            'PENDING': '待確認',
            'CONFIRMED': '已確認',
            'COMPLETED': '已完成',
            'CANCELLED': '已取消',
            'REJECTED': '已拒絕'
        };
        return map[status] || status;
    },

    getStatusBadgeClass(status) {
        const map = {
            'PENDING': 'badge-warning',
            'CONFIRMED': 'badge-info',
            'COMPLETED': 'badge-success',
            'CANCELLED': 'badge-error',
            'REJECTED': 'badge-error'
        };
        return map[status] || '';
    }
};
