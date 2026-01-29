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
    // Allow both ADMIN and regular users (CUSTOMER)
    // if (userData.role !== 'ADMIN') {
    //     window.location.href = 'index.html';
    //     return;
    // }

    // Set user info in sidebar
    document.getElementById('userName').textContent = userData.roleName || userData.username;
    document.getElementById('userAvatar').textContent = (userData.roleName || userData.username).charAt(0).toUpperCase();

    // Store current user data in App
    App.currentUser = userData;

    // Apply role-based navigation visibility
    App.applyRoleVisibility(userData.role);

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

    /**
     * Role-based navigation visibility
     * ADMIN: all pages
     * CUSTOMER: dashboard, pets, bookings
     * SITTER: dashboard, bookings
     */
    applyRoleVisibility(role) {
        const navRules = {
            'ADMIN': ['dashboard', 'users', 'sitters', 'pets', 'bookings'],
            'CUSTOMER': ['dashboard', 'pets', 'bookings'],
            'SITTER': ['dashboard', 'bookings']
        };
        const allowedPages = navRules[role] || ['dashboard'];

        document.querySelectorAll('.nav-item[data-page]').forEach(item => {
            const page = item.dataset.page;
            item.style.display = allowedPages.includes(page) ? '' : 'none';
        });

        // Update page title based on role
        const titles = { 'ADMIN': '管理後台', 'CUSTOMER': '飼主中心', 'SITTER': '保母中心' };
        document.title = 'Pet Care - ' + (titles[role] || '系統');
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
            case 'pets':
                this.loadPets();
                break;
            case 'bookings':
                this.loadBookings();
                break;
        }
    },

    // ===== Logout =====
    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', async (e) => {
            e.preventDefault();

            try {
                // Call logout API
                await API.auth.logout();
            } catch (error) {
                console.error('Logout API error:', error);
            }

            // Clear all session data
            sessionStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
            sessionStorage.removeItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
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
            // Load stats
            const [sittersRes, petsRes, usersRes] = await Promise.all([
                API.sitters.getAllWithRating(),
                API.pets.getAll(),
                API.users.getAll()
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
        tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted"></td></tr>';

        try {
            const res = await API.users.getAll();
            const customers = res.data || [];

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
                        <button class="btn btn-ghost" onclick="App.viewUserDetail('${customer.id}', '${customer.userId}')">
                            查看詳情
                        </button>
                    </td>
                </tr>
            `).join('');

            // Also update dashboard stat
            document.getElementById('stat-users').textContent = customers.length;
        } catch (error) {
            console.error('Users load error:', error);
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted">載入失敗</td></tr>';
        }
    },

    async viewUserDetail(customerId, userId) {
        const contentEl = document.getElementById('user-detail-content');
        const nameEl = document.getElementById('user-detail-name');

        contentEl.innerHTML = '<p class="text-muted"></p>';
        this.showModal('user-detail-modal');

        try {
            const [customerRes, petsRes, bookingsRes] = await Promise.all([
                API.users.getById(customerId),
                API.pets.getByUser(userId),
                API.bookings.getByUser(userId)
            ]);

            const customer = customerRes.data;
            const pets = petsRes.data || [];
            const bookings = bookingsRes.data || [];

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
                        <span class="badge badge-info">${pet.petType === 'DOG' ? '🐕' : '🐈'} ${pet.name}</span>
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
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted"></td></tr>';

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

        contentEl.innerHTML = '<p class="text-muted"></p>';
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
    },

    // ===== Pets Management =====
    allPets: [],
    currentPetFilter: 'all',

    async loadPets() {
        const tbody = document.getElementById('pets-table');
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted"></td></tr>';

        try {
            let res;
            // If user is CUSTOMER, only load their pets
            if (this.currentUser && this.currentUser.role !== 'ADMIN') {
                res = await API.pets.getByUser(this.currentUser.userId);
            } else {
                // ADMIN can see all pets
                res = await API.pets.getAll();
            }
            this.allPets = res.data || [];

            // Update stats
            const dogs = this.allPets.filter(p => p.petType === 'DOG').length;
            const cats = this.allPets.filter(p => p.petType === 'CAT').length;
            document.getElementById('stat-dogs').textContent = dogs;
            document.getElementById('stat-cats').textContent = cats;

            this.displayPets(this.allPets);
        } catch (error) {
            console.error('Pets load error:', error);
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">載入失敗</td></tr>';
        }
    },

    displayPets(pets) {
        const tbody = document.getElementById('pets-table');

        if (!pets.length) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">暫無寵物資料</td></tr>';
            return;
        }

        tbody.innerHTML = pets.map(pet => `
            <tr>
                <td>
                    <div class="flex items-center gap-1">
                        <div class="avatar avatar-sm">${pet.petType === 'DOG' ? '🐕' : '🐈'}</div>
                        <span>${pet.name || '未知'}</span>
                    </div>
                </td>
                <td>${pet.petType === 'DOG' ? '狗狗' : '貓咪'}</td>
                <td>${pet.breed || '-'}</td>
                <td>${pet.age || '-'} 歲</td>
                <td>${pet.ownerName || '-'}</td>
                <td>
                    <button class="btn btn-ghost" onclick="App.editPet('${pet.id}')">編輯</button>
                    <button class="btn btn-ghost" style="color: var(--color-error);" onclick="App.confirmDeletePet('${pet.id}', '${pet.name}')">刪除</button>
                </td>
            </tr>
        `).join('');
    },

    filterPets(filter) {
        console.log('filterPets called with:', filter);
        console.log('All pets:', this.allPets);
        this.currentPetFilter = filter;

        // Update button states
        document.querySelectorAll('[id^="filter-"]').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-ghost');
        });

        const filterBtn = document.getElementById(`filter-${filter}`);
        console.log('Filter button found:', filterBtn);
        if (filterBtn) {
            filterBtn.classList.remove('btn-ghost');
            filterBtn.classList.add('btn-secondary');
        }

        // Filter pets
        if (filter === 'all') {
            console.log('Showing all pets');
            this.displayPets(this.allPets);
        } else {
            const filtered = this.allPets.filter(p => {
                console.log(`Checking pet ${p.name}: petType=${p.petType}, filter=${filter}, match=${p.petType === filter}`);
                return p.petType === filter;
            });
            console.log('Filtered pets:', filtered);
            this.displayPets(filtered);
        }
    },

    async openAddPetModal() {
        document.getElementById('pet-modal-title').textContent = '新增寵物';
        document.getElementById('pet-form').reset();
        document.getElementById('pet-id').value = '';
        document.getElementById('pet-user-id').value = '';

        const ownerSelect = document.getElementById('pet-owner');
        const ownerDisplay = document.getElementById('pet-owner-display');

        // If user is CUSTOMER, auto-fill with their userId and hide selection
        if (this.currentUser && this.currentUser.role !== 'ADMIN') {
            document.getElementById('pet-user-id').value = this.currentUser.userId;
            ownerSelect.style.display = 'none';
            ownerSelect.required = false;
            ownerDisplay.style.display = 'block';
            ownerDisplay.value = `${this.currentUser.roleName || this.currentUser.username} (本人)`;
        } else {
            // ADMIN can select owner
            ownerSelect.style.display = 'block';
            ownerSelect.required = true;
            ownerDisplay.style.display = 'none';
            await this.loadOwnerOptions();
        }

        this.updatePetTypeFields();
        this.showModal('pet-modal');
    },

    async loadOwnerOptions() {
        try {
            const res = await API.users.getAll();
            const customers = res.data || [];
            const selectEl = document.getElementById('pet-owner');
            selectEl.innerHTML = '<option value="">請選擇飼主</option>' +
                customers.map(c => `<option value="${c.userId}">${c.name} (@${c.username})</option>`).join('');
        } catch (error) {
            console.error('Failed to load owners:', error);
        }
    },

    async editPet(petId) {
        const pet = this.allPets.find(p => p.id === petId);
        if (!pet) return;

        document.getElementById('pet-modal-title').textContent = '編輯寵物';
        document.getElementById('pet-id').value = pet.id;
        document.getElementById('pet-user-id').value = pet.userId;

        // For editing: hide select, show readonly display with owner name
        const ownerSelect = document.getElementById('pet-owner');
        const ownerDisplay = document.getElementById('pet-owner-display');
        ownerSelect.style.display = 'none';
        ownerSelect.required = false;
        ownerDisplay.style.display = 'block';
        ownerDisplay.value = pet.ownerName || '未知飼主';

        document.getElementById('pet-type').value = pet.petType;
        document.getElementById('pet-name').value = pet.name || '';
        document.getElementById('pet-breed').value = pet.breed || '';
        document.getElementById('pet-age').value = pet.age || '';
        document.getElementById('pet-gender').value = pet.gender || '';
        document.getElementById('pet-special-needs').value = pet.specialNeeds || '';
        document.getElementById('pet-vaccine').value = pet.vaccineStatus || '';
        document.getElementById('pet-neutered').checked = pet.isNeutered || false;

        this.updatePetTypeFields();

        // Load type-specific data
        try {
            if (pet.petType === 'DOG') {
                const res = await API.dogs.getById(petId);
                const dog = res.data;
                document.getElementById('dog-size').value = dog.size || 'MEDIUM';
                document.getElementById('dog-training').value = dog.trainingLevel || 'BASIC';
                document.getElementById('dog-walk-freq').value = dog.walkFrequencyPerDay || 1;
                document.getElementById('dog-walk-required').checked = dog.isWalkRequired || false;
                document.getElementById('dog-friendly-dogs').checked = dog.isFriendlyWithDogs || false;
                document.getElementById('dog-friendly-children').checked = dog.isFriendlyWithChildren || false;
            } else if (pet.petType === 'CAT') {
                const res = await API.cats.getById(petId);
                const cat = res.data;
                document.getElementById('cat-litter-box').value = cat.litterBoxType || 'OPEN';
                document.getElementById('cat-scratching').value = cat.scratchingHabit || 'MODERATE';
                document.getElementById('cat-indoor').checked = cat.isIndoor || false;
            }
        } catch (error) {
            console.error('Failed to load pet details:', error);
        }

        this.showModal('pet-modal');
    },

    updatePetTypeFields() {
        const petType = document.getElementById('pet-type').value;
        document.getElementById('dog-fields').style.display = petType === 'DOG' ? 'block' : 'none';
        document.getElementById('cat-fields').style.display = petType === 'CAT' ? 'block' : 'none';
    },

    async savePet(event) {
        event.preventDefault();

        const petId = document.getElementById('pet-id').value;
        const petType = document.getElementById('pet-type').value;
        const isEdit = !!petId;

        // Get userId from hidden field or select depending on context
        let userId;
        if (isEdit) {
            // Editing: use hidden field
            userId = document.getElementById('pet-user-id').value;
        } else if (this.currentUser && this.currentUser.role !== 'ADMIN') {
            // New pet, CUSTOMER: use their userId from hidden field
            userId = document.getElementById('pet-user-id').value;
        } else {
            // New pet, ADMIN: use selected owner
            userId = document.getElementById('pet-owner').value;
        }

        if (!userId && !isEdit) {
            alert('請選擇飼主');
            return;
        }

        const baseData = {
            name: document.getElementById('pet-name').value,
            breed: document.getElementById('pet-breed').value,
            age: parseInt(document.getElementById('pet-age').value) || null,
            gender: document.getElementById('pet-gender').value || null,
            specialNeeds: document.getElementById('pet-special-needs').value,
            vaccineStatus: document.getElementById('pet-vaccine').value,
            isNeutered: document.getElementById('pet-neutered').checked
        };

        let petData = {};
        let apiMethod = null;

        if (petType === 'DOG') {
            petData = {
                ...baseData,
                size: document.getElementById('dog-size').value,
                trainingLevel: document.getElementById('dog-training').value,
                walkFrequencyPerDay: parseInt(document.getElementById('dog-walk-freq').value) || 0,
                isWalkRequired: document.getElementById('dog-walk-required').checked,
                isFriendlyWithDogs: document.getElementById('dog-friendly-dogs').checked,
                isFriendlyWithPeople: true,
                isFriendlyWithChildren: document.getElementById('dog-friendly-children').checked
            };
            apiMethod = isEdit
                ? (id, data) => API.dogs.update(id, data)
                : (data, uid) => API.dogs.create(data, uid);
        } else if (petType === 'CAT') {
            petData = {
                ...baseData,
                litterBoxType: document.getElementById('cat-litter-box').value,
                scratchingHabit: document.getElementById('cat-scratching').value,
                isIndoor: document.getElementById('cat-indoor').checked
            };
            apiMethod = isEdit
                ? (id, data) => API.cats.update(id, data)
                : (data, uid) => API.cats.create(data, uid);
        }

        try {
            if (isEdit) {
                await apiMethod(petId, petData);
                alert('寵物資料已更新');
            } else {
                await apiMethod(petData, userId);
                alert('寵物已新增');
            }

            this.hideModal('pet-modal');
            this.loadPets();
        } catch (error) {
            console.error('Save pet error:', error);
            alert('儲存失敗：' + (error.message || '未知錯誤'));
        }
    },

    async confirmDeletePet(petId, petName) {
        if (!confirm(`確定要刪除 ${petName} 嗎？此操作無法復原。`)) return;

        try {
            await API.pets.delete(petId);
            alert('寵物已刪除');
            this.loadPets();
        } catch (error) {
            console.error('Delete pet error:', error);
            alert('刪除失敗');
        }
    },

    // ===== Bookings Management =====
    allBookings: [],
    currentBookingFilter: 'all',

    async loadBookings() {
        const tbody = document.getElementById('bookings-table');
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted"></td></tr>';

        try {
            const res = await API.bookings.getAll();
            this.allBookings = res.data || [];

            // Update stats
            const pending = this.allBookings.filter(b => b.status === 'PENDING').length;
            const confirmed = this.allBookings.filter(b => b.status === 'CONFIRMED').length;
            const completed = this.allBookings.filter(b => b.status === 'COMPLETED').length;

            document.getElementById('stat-pending').textContent = pending;
            document.getElementById('stat-confirmed').textContent = confirmed;
            document.getElementById('stat-completed').textContent = completed;

            this.displayBookings(this.allBookings);
        } catch (error) {
            console.error('Bookings load error:', error);
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">載入失敗</td></tr>';
        }
    },

    displayBookings(bookings) {
        const tbody = document.getElementById('bookings-table');

        if (!bookings.length) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">暫無預約資料</td></tr>';
            return;
        }

        tbody.innerHTML = bookings.map(booking => `
            <tr>
                <td style="font-family: monospace; font-size: 0.85rem;">#${booking.id.substring(0, 8)}</td>
                <td>${booking.petName || '-'}</td>
                <td>${booking.username || '-'}</td>
                <td>${booking.sitterName || '-'}</td>
                <td>${this.formatDateTime(booking.startTime)}</td>
                <td>NT$ ${booking.totalPrice || 0}</td>
                <td><span class="badge ${this.getStatusBadgeClass(booking.status)}">${this.getStatusText(booking.status)}</span></td>
                <td>
                    <button class="btn btn-ghost" onclick="App.viewBookingDetail('${booking.id}')">詳情</button>
                </td>
            </tr>
        `).join('');
    },

    filterBookings(filter) {
        console.log('filterBookings called with:', filter);
        console.log('All bookings:', this.allBookings);
        this.currentBookingFilter = filter;

        // Update button states
        document.querySelectorAll('[id^="booking-filter-"]').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-ghost');
        });

        const filterBtn = document.getElementById(`booking-filter-${filter}`);
        console.log('Filter button found:', filterBtn);
        if (filterBtn) {
            filterBtn.classList.remove('btn-ghost');
            filterBtn.classList.add('btn-secondary');
        }

        // Filter bookings
        if (filter === 'all') {
            console.log('Showing all bookings');
            this.displayBookings(this.allBookings);
        } else if (filter === 'CANCELLED') {
            const filtered = this.allBookings.filter(b => b.status === 'CANCELLED' || b.status === 'REJECTED');
            console.log('Filtered cancelled/rejected bookings:', filtered);
            this.displayBookings(filtered);
        } else {
            const filtered = this.allBookings.filter(b => {
                console.log(`Checking booking ${b.id}: status=${b.status}, filter=${filter}, match=${b.status === filter}`);
                return b.status === filter;
            });
            console.log('Filtered bookings:', filtered);
            this.displayBookings(filtered);
        }
    },

    async viewBookingDetail(bookingId) {
        const booking = this.allBookings.find(b => b.id === bookingId);
        if (!booking) return;

        const contentEl = document.getElementById('booking-detail-content');

        // 根據當前狀態決定可用的操作按鈕
        let statusActions = '';
        if (booking.status === 'PENDING') {
            statusActions = `
                <div style="display: flex; gap: 0.5rem; margin-top: 1rem; flex-wrap: wrap;">
                    <button class="btn btn-primary" onclick="App.changeBookingStatus('${bookingId}', 'CONFIRMED')">✅ 確認預約</button>
                    <button class="btn btn-ghost" style="color: var(--color-error);" onclick="App.changeBookingStatus('${bookingId}', 'REJECTED')">❌ 拒絕預約</button>
                    <button class="btn btn-ghost" onclick="App.changeBookingStatus('${bookingId}', 'CANCELLED')">🚫 取消預約</button>
                </div>
            `;
        } else if (booking.status === 'CONFIRMED') {
            statusActions = `
                <div style="display: flex; gap: 0.5rem; margin-top: 1rem; flex-wrap: wrap;">
                    <button class="btn btn-primary" onclick="App.changeBookingStatus('${bookingId}', 'COMPLETED')">🎉 標記完成</button>
                    <button class="btn btn-ghost" onclick="App.changeBookingStatus('${bookingId}', 'CANCELLED')">🚫 取消預約</button>
                </div>
            `;
        } else {
            statusActions = `
                <div style="margin-top: 1rem;">
                    <p class="text-muted">此預約已結束，無法變更狀態</p>
                </div>
            `;
        }

        contentEl.innerHTML = `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1.5rem; margin-bottom: 1.5rem;">
                <div>
                    <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">訂單資訊</h4>
                    <p><strong>訂單編號：</strong>${booking.id}</p>
                    <p><strong>狀態：</strong><span class="badge ${this.getStatusBadgeClass(booking.status)}">${this.getStatusText(booking.status)}</span></p>
                    <p><strong>建立時間：</strong>${this.formatDateTime(booking.createdAt)}</p>
                    ${booking.updatedAt !== booking.createdAt ? `<p><strong>更新時間：</strong>${this.formatDateTime(booking.updatedAt)}</p>` : ''}
                </div>
                <div>
                    <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">服務資訊</h4>
                    <p><strong>寵物：</strong>${booking.petName || '-'}</p>
                    <p><strong>飼主：</strong>${booking.username || '-'}</p>
                    <p><strong>保母：</strong>${booking.sitterName || '-'}</p>
                    <p><strong>費用：</strong>NT$ ${booking.totalPrice || 0}</p>
                </div>
            </div>

            <div style="margin-bottom: 1.5rem;">
                <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">服務時間</h4>
                <p><strong>開始：</strong>${this.formatDateTime(booking.startTime)}</p>
                <p><strong>結束：</strong>${this.formatDateTime(booking.endTime)}</p>
            </div>

            ${booking.notes ? `
                <div style="padding: 1rem; background: var(--color-accent); border-radius: var(--radius-md); margin-bottom: 1rem;">
                    <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">飼主備註</h4>
                    <p>${booking.notes}</p>
                </div>
            ` : ''}

            ${booking.sitterResponse ? `
                <div style="padding: 1rem; background: var(--color-accent); border-radius: var(--radius-md); margin-bottom: 1rem;">
                    <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">保母回覆</h4>
                    <p>${booking.sitterResponse}</p>
                </div>
            ` : ''}

            <div style="border-top: 1px solid var(--color-border); padding-top: 1rem;">
                <h4 style="margin-bottom: 0.5rem; color: var(--color-primary);">狀態操作</h4>
                ${statusActions}
            </div>
        `;

        this.showModal('booking-detail-modal');
    },

    async changeBookingStatus(bookingId, targetStatus) {
        const statusNames = {
            'CONFIRMED': '確認',
            'REJECTED': '拒絕',
            'CANCELLED': '取消',
            'COMPLETED': '完成'
        };

        const statusName = statusNames[targetStatus];
        let reason = null;

        // 如果是拒絕或取消，詢問原因
        if (targetStatus === 'REJECTED' || targetStatus === 'CANCELLED') {
            reason = prompt(`請輸入${statusName}原因（選填）：`);
            if (reason === null) return; // 用戶點擊取消
        }

        if (!confirm(`確定要${statusName}此預約嗎？`)) return;

        try {
            await API.bookings.updateStatus(bookingId, targetStatus, reason);
            alert(`預約已${statusName}`);

            // 關閉 modal 並重新加載列表
            this.hideModal('booking-detail-modal');
            await this.loadBookings();
        } catch (error) {
            console.error('Change booking status error:', error);
            alert(`狀態變更失敗：${error.message || '未知錯誤'}`);
        }
    },

    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';
        try {
            const date = new Date(dateTimeStr);
            return date.toLocaleString('zh-TW', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch {
            return dateTimeStr;
        }
    }
};
