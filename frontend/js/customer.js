/**
 * Pet Care - Customer Page SPA Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    const user = sessionStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    const token = sessionStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
    if (!user || !token) {
        window.location.href = 'index.html';
        return;
    }

    const userData = JSON.parse(user);
    CustomerApp.currentUser = userData;

    document.getElementById('userName').textContent = userData.roleName || userData.username;
    document.getElementById('userAvatar').textContent = (userData.roleName || userData.username).charAt(0).toUpperCase();

    CustomerApp.init();
});

const CustomerApp = {
    currentUser: null,
    currentPage: 'dashboard',
    myPets: [],
    myBookings: [],
    myRatings: [],
    allSitters: [],

    // Booking flow state
    bookingStep: 1,
    selectedSitterId: null,
    selectedSitter: null,
    selectedPetId: null,
    bookingFilter: 'all',

    async init() {
        this.setupNavigation();
        this.setupLogout();
        this.setupModals();
        this.setupRatingStars();

        // Fetch customer profile to get proper display name
        try {
            const res = await API.customers.getByUserId(this.currentUser.userId);
            const customerName = res.data && res.data.name;
            if (customerName) {
                document.getElementById('userName').textContent = customerName;
                document.getElementById('userAvatar').textContent = customerName.charAt(0).toUpperCase();
            }
        } catch (error) {
            console.error('Failed to load customer profile for display name:', error);
        }

        // 連接 WebSocket 接收即時通知
        API.ws.connect((notification) => {
            this.showToast(notification.title, notification.message, notification.type);
            // 收到通知時刷新當前頁面資料
            if (this.currentPage === 'dashboard') this.loadDashboard();
            else if (this.currentPage === 'bookings') this.loadBookings();
        });

        this.loadDashboard();
    },

    showToast(title, message, type) {
        const container = document.getElementById('toast-container');
        if (!container) return;
        const colors = {
            'BOOKING_CONFIRMED': '#22c55e',
            'BOOKING_CANCELLED': '#ef4444',
            'BOOKING_REJECTED': '#f59e0b',
            'BOOKING_COMPLETED': '#3b82f6',
            'BOOKING_EXPIRED': '#6b7280'
        };
        const color = colors[type] || '#3b82f6';
        const toast = document.createElement('div');
        toast.style.cssText = `background:${color};color:#fff;padding:14px 20px;border-radius:10px;box-shadow:0 4px 12px rgba(0,0,0,0.15);min-width:280px;max-width:380px;animation:slideIn 0.3s ease;`;
        toast.innerHTML = `<div style="font-weight:600;margin-bottom:4px;">${title}</div><div style="font-size:0.9em;opacity:0.95;">${message}</div>`;
        container.appendChild(toast);
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(100%)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 5000);
    },

    // ==================== Navigation ====================
    setupNavigation() {
        document.querySelectorAll('.nav-item[data-page]').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateTo(item.dataset.page);
            });
        });
    },

    navigateTo(page) {
        document.querySelectorAll('.page').forEach(p => p.style.display = 'none');
        const pageEl = document.getElementById(`page-${page}`);
        if (pageEl) pageEl.style.display = 'block';

        document.querySelectorAll('.nav-item[data-page]').forEach(n => {
            n.classList.toggle('active', n.dataset.page === page);
        });

        this.currentPage = page;

        switch (page) {
            case 'dashboard': this.loadDashboard(); break;
            case 'my-pets': this.loadMyPets(); break;
            case 'new-booking': this.loadNewBooking(); break;
            case 'my-bookings': this.loadMyBookings(); break;
            case 'ratings': this.loadRatings(); break;
        }
    },

    // ==================== Logout ====================
    setupLogout() {
        document.getElementById('logoutBtn').addEventListener('click', async (e) => {
            e.preventDefault();
            API.ws.disconnect();
            try { await API.auth.logout(); } catch (_) {}
            sessionStorage.removeItem(CONFIG.STORAGE_KEYS.USER);
            sessionStorage.removeItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
            window.location.href = 'index.html';
        });
    },

    // ==================== Modals ====================
    setupModals() {
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal-backdrop') || e.target.classList.contains('modal-close')) {
                const modal = e.target.closest('.modal');
                if (modal) modal.style.display = 'none';
            }
        });
    },

    showModal(id) {
        document.getElementById(id).style.display = 'flex';
    },

    hideModal(id) {
        document.getElementById(id).style.display = 'none';
    },

    // ==================== 1. Dashboard ====================
    async loadDashboard() {
        try {
            const userId = this.currentUser.userId;
            const [petsRes, bookingsRes] = await Promise.all([
                API.pets.getByUser(userId),
                API.bookings.getByUser(userId)
            ]);

            const pets = petsRes.data || [];
            const bookings = bookingsRes.data || [];
            this.myPets = pets;
            this.myBookings = bookings;

            // Stats
            document.getElementById('stat-my-pets').textContent = pets.length;

            const active = bookings.filter(b => b.status === 'PENDING' || b.status === 'CONFIRMED');
            document.getElementById('stat-active-bookings').textContent = active.length;

            const completed = bookings.filter(b => b.status === 'COMPLETED');
            document.getElementById('stat-completed-bookings').textContent = completed.length;

            // Latest booking status
            const sorted = [...bookings].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            const latest = sorted[0];
            const latestEl = document.getElementById('stat-latest-booking');
            if (latest) {
                latestEl.innerHTML = `<span class="badge ${this.getStatusBadgeClass(latest.status)}">${this.getStatusText(latest.status)}</span>`;
            } else {
                latestEl.textContent = '尚無預約';
            }
        } catch (error) {
            console.error('Dashboard load error:', error);
        }
    },

    // ==================== 2. My Pets ====================
    async loadMyPets() {
        const container = document.getElementById('pet-cards-container');
        container.innerHTML = '';

        try {
            const res = await API.pets.getByUser(this.currentUser.userId);
            this.myPets = res.data || [];
            this.renderPetCards();
        } catch (error) {
            console.error('Load pets error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    renderPetCards() {
        const container = document.getElementById('pet-cards-container');
        if (!this.myPets.length) {
            container.innerHTML = '<p class="text-muted">尚未新增寵物，點擊右上角按鈕新增吧！</p>';
            return;
        }

        container.innerHTML = this.myPets.map(pet => `
            <div class="pet-card">
                <div class="pet-card-header">
                    <div class="pet-card-name">
                        <span style="font-size:1.5rem;">${pet.petType === 'DOG' ? '🐕' : '🐈'}</span>
                        <h3>${pet.name || '未命名'}</h3>
                    </div>
                    <span class="badge badge-info">${pet.petType === 'DOG' ? '狗狗' : '貓咪'}</span>
                </div>
                <div class="pet-card-details">
                    <p>品種：${pet.breed || '-'}</p>
                    <p>年齡：${pet.age != null ? pet.age + ' 歲' : '-'}</p>
                    <p>性別：${this.getGenderText(pet.gender)}</p>
                </div>
                <div class="pet-card-actions">
                    <button class="btn btn-ghost" onclick="CustomerApp.editPet('${pet.id}')">✏️ 編輯</button>
                    <button class="btn btn-ghost" style="color:var(--color-error);" onclick="CustomerApp.deletePet('${pet.id}', '${pet.name}')">🗑️ 刪除</button>
                </div>
            </div>
        `).join('');
    },

    openAddPetModal() {
        document.getElementById('pet-modal-title').textContent = '新增寵物';
        document.getElementById('pet-form').reset();
        document.getElementById('pet-id').value = '';
        document.getElementById('pet-type').disabled = false;
        this.updatePetTypeFields();
        this.showModal('pet-modal');
    },

    async editPet(petId) {
        const pet = this.myPets.find(p => p.id === petId);
        if (!pet) return;

        document.getElementById('pet-modal-title').textContent = '編輯寵物';
        document.getElementById('pet-id').value = petId;
        document.getElementById('pet-type').value = pet.petType;
        document.getElementById('pet-type').disabled = true;
        document.getElementById('pet-name').value = pet.name || '';
        document.getElementById('pet-breed').value = pet.breed || '';
        document.getElementById('pet-age').value = pet.age ?? '';
        document.getElementById('pet-gender').value = pet.gender || '';
        document.getElementById('pet-special-needs').value = pet.specialNeeds || '';
        document.getElementById('pet-vaccine').value = pet.vaccineStatus || pet.vaccinationStatus || '';
        document.getElementById('pet-neutered').checked = pet.isNeutered || pet.neutered || false;

        this.updatePetTypeFields();

        try {
            if (pet.petType === 'DOG') {
                const res = await API.dogs.getById(petId);
                const dog = res.data;
                if (dog) {
                    document.getElementById('dog-size').value = dog.size || 'MEDIUM';
                    document.getElementById('dog-training').value = dog.trainingLevel || 'NONE';
                    document.getElementById('dog-walk-freq').value = dog.walkFrequencyPerDay ?? dog.walkFrequency ?? 1;
                    document.getElementById('dog-walk-required').checked = dog.isWalkRequired || dog.walkRequired || false;
                    document.getElementById('dog-friendly-dogs').checked = dog.isFriendlyWithDogs || dog.friendlyWithDogs || false;
                    document.getElementById('dog-friendly-children').checked = dog.isFriendlyWithChildren || dog.friendlyWithChildren || false;
                }
            } else if (pet.petType === 'CAT') {
                const res = await API.cats.getById(petId);
                const cat = res.data;
                if (cat) {
                    document.getElementById('cat-litter-box').value = cat.litterBoxType || 'OPEN';
                    document.getElementById('cat-scratching').value = cat.scratchingHabit || 'MODERATE';
                    document.getElementById('cat-indoor').checked = cat.isIndoor || cat.indoorOnly || false;
                }
            }
        } catch (error) {
            console.error('Load pet detail error:', error);
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

        const baseData = {
            name: document.getElementById('pet-name').value.trim(),
            breed: document.getElementById('pet-breed').value.trim() || null,
            age: document.getElementById('pet-age').value ? parseInt(document.getElementById('pet-age').value) : null,
            gender: document.getElementById('pet-gender').value || null,
            specialNeeds: document.getElementById('pet-special-needs').value.trim() || null,
            vaccineStatus: document.getElementById('pet-vaccine').value.trim() || null,
            isNeutered: document.getElementById('pet-neutered').checked
        };

        try {
            if (petType === 'DOG') {
                const dogData = {
                    ...baseData,
                    size: document.getElementById('dog-size').value,
                    trainingLevel: document.getElementById('dog-training').value,
                    walkFrequencyPerDay: parseInt(document.getElementById('dog-walk-freq').value) || 0,
                    isWalkRequired: document.getElementById('dog-walk-required').checked,
                    isFriendlyWithDogs: document.getElementById('dog-friendly-dogs').checked,
                    isFriendlyWithPeople: true,
                    isFriendlyWithChildren: document.getElementById('dog-friendly-children').checked
                };
                if (isEdit) {
                    await API.dogs.update(petId, dogData);
                } else {
                    await API.dogs.create(dogData, this.currentUser.userId);
                }
            } else if (petType === 'CAT') {
                const catData = {
                    ...baseData,
                    litterBoxType: document.getElementById('cat-litter-box').value,
                    scratchingHabit: document.getElementById('cat-scratching').value,
                    isIndoor: document.getElementById('cat-indoor').checked
                };
                if (isEdit) {
                    await API.cats.update(petId, catData);
                } else {
                    await API.cats.create(catData, this.currentUser.userId);
                }
            }

            this.hideModal('pet-modal');
            alert(isEdit ? '寵物資料已更新' : '寵物已新增');
            this.loadMyPets();
        } catch (error) {
            console.error('Save pet error:', error);
            alert('儲存失敗：' + (error.message || '未知錯誤'));
        }
    },

    async deletePet(petId, petName) {
        if (!confirm(`確定要刪除「${petName}」嗎？此操作無法復原。`)) return;

        try {
            await API.pets.delete(petId);
            alert('寵物已刪除');
            this.loadMyPets();
        } catch (error) {
            console.error('Delete pet error:', error);
            alert('刪除失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== 3. New Booking ====================
    async loadNewBooking() {
        this.bookingStep = 1;
        this.selectedSitterId = null;
        this.selectedSitter = null;
        this.selectedPetId = null;
        this._bookingIdempotencyKey = null; // 新流程重置冪等性 key
        this.updateBookingStepUI();
        await this.loadSitters();
    },

    async loadSitters() {
        const container = document.getElementById('sitter-cards-container');
        container.innerHTML = '';

        try {
            const res = await API.sitters.getAllWithRating();
            this.allSitters = res.data || [];

            if (!this.allSitters.length) {
                container.innerHTML = '<p class="text-muted">目前沒有可用的保母</p>';
                return;
            }

            container.innerHTML = this.allSitters.map(sitter => `
                <div class="sitter-card ${this.selectedSitterId === sitter.id ? 'selected' : ''}"
                     onclick="CustomerApp.selectSitter('${sitter.id}')">
                    <div style="display:flex;align-items:center;gap:var(--space-md);margin-bottom:var(--space-md);">
                        <div class="avatar">${(sitter.name || '?').charAt(0)}</div>
                        <div>
                            <h4 style="margin:0;">${sitter.name || '未知'}</h4>
                            <span class="text-muted" style="font-size:0.85rem;">${sitter.experienceLevel || sitter.experience || '-'}</span>
                        </div>
                    </div>
                    <div style="margin-bottom:var(--space-sm);">
                        <span class="stars-display">${this.renderStars(sitter.averageRating)}</span>
                        <span class="text-muted" style="font-size:0.85rem;">(${sitter.ratingCount || 0} 則評價)</span>
                    </div>
                    <div style="display:flex;justify-content:space-between;font-size:0.9rem;">
                        <span class="text-muted">完成訂單：${sitter.completedBookings || 0}</span>
                        <span style="font-weight:600;color:var(--color-primary);">$${sitter.hourlyRate || '-'}/時</span>
                    </div>
                </div>
            `).join('');
        } catch (error) {
            console.error('Load sitters error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    selectSitter(sitterId) {
        this.selectedSitterId = sitterId;
        this.selectedSitter = this.allSitters.find(s => s.id === sitterId);

        // Update card selection visual
        document.querySelectorAll('.sitter-card').forEach(card => {
            card.classList.remove('selected');
        });
        event.currentTarget.closest('.sitter-card').classList.add('selected');
    },

    updateBookingStepUI() {
        const step = this.bookingStep;

        // Update step indicators
        document.querySelectorAll('#booking-steps-bar .step-indicator').forEach(el => {
            const s = parseInt(el.dataset.step);
            el.classList.remove('active', 'completed');
            if (s === step) el.classList.add('active');
            else if (s < step) el.classList.add('completed');
        });

        // Show/hide step content
        for (let i = 1; i <= 4; i++) {
            const el = document.getElementById(`booking-step-${i}`);
            el.classList.toggle('active', i === step);
        }
    },

    bookingNextStep(fromStep) {
        if (fromStep === 1) {
            if (!this.selectedSitterId) {
                alert('請先選擇一位保母');
                return;
            }
        } else if (fromStep === 2) {
            const date = document.getElementById('booking-date').value;
            const start = document.getElementById('booking-start-time').value;
            const end = document.getElementById('booking-end-time').value;
            if (!date || !start || !end) {
                alert('請填寫完整的日期和時間');
                return;
            }
            if (start >= end) {
                alert('結束時間必須晚於開始時間');
                return;
            }
            // Load pets for step 3
            this.loadBookingPets();
        } else if (fromStep === 3) {
            if (!this.selectedPetId) {
                alert('請選擇一隻寵物');
                return;
            }
            this.renderBookingSummary();
        }

        this.bookingStep = fromStep + 1;
        this.updateBookingStepUI();
    },

    bookingPrevStep(fromStep) {
        this.bookingStep = fromStep - 1;
        this.updateBookingStepUI();
    },

    async loadBookingPets() {
        const container = document.getElementById('booking-pet-select');

        if (!this.myPets.length) {
            try {
                const res = await API.pets.getByUser(this.currentUser.userId);
                this.myPets = res.data || [];
            } catch (error) {
                console.error('Load pets error:', error);
            }
        }

        if (!this.myPets.length) {
            container.innerHTML = '<p class="text-muted">您尚未新增寵物，請先到「我的寵物」頁面新增。</p>';
            return;
        }

        container.innerHTML = this.myPets.map(pet => `
            <div class="pet-select-card ${this.selectedPetId === pet.id ? 'selected' : ''}"
                 onclick="CustomerApp.selectBookingPet('${pet.id}', this)">
                <span style="font-size:1.25rem;">${pet.petType === 'DOG' ? '🐕' : '🐈'}</span>
                <div>
                    <strong>${pet.name}</strong>
                    <div class="text-muted" style="font-size:0.8rem;">${pet.breed || '未知品種'} ${pet.age != null ? '/ ' + pet.age + '歲' : ''}</div>
                </div>
            </div>
        `).join('');
    },

    selectBookingPet(petId, el) {
        this.selectedPetId = petId;
        document.querySelectorAll('.pet-select-card').forEach(c => c.classList.remove('selected'));
        el.classList.add('selected');
    },

    renderBookingSummary() {
        const sitter = this.selectedSitter;
        const pet = this.myPets.find(p => p.id === this.selectedPetId);
        const date = document.getElementById('booking-date').value;
        const start = document.getElementById('booking-start-time').value;
        const end = document.getElementById('booking-end-time').value;

        document.getElementById('booking-summary').innerHTML = `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;">
                <p><strong>保母：</strong>${sitter ? sitter.name : '-'}</p>
                <p><strong>時薪：</strong>${sitter && sitter.hourlyRate ? '$' + sitter.hourlyRate : '-'}</p>
                <p><strong>寵物：</strong>${pet ? (pet.petType === 'DOG' ? '🐕 ' : '🐈 ') + pet.name : '-'}</p>
                <p><strong>品種：</strong>${pet ? (pet.breed || '-') : '-'}</p>
                <p><strong>日期：</strong>${date}</p>
                <p><strong>時間：</strong>${start} ~ ${end}</p>
            </div>
        `;
    },

    async submitBooking() {
        const date = document.getElementById('booking-date').value;
        const start = document.getElementById('booking-start-time').value;
        const end = document.getElementById('booking-end-time').value;
        const notes = document.getElementById('booking-notes').value.trim();

        if (!this.selectedSitterId || !this.selectedPetId || !date || !start || !end) {
            alert('請完成所有必填欄位');
            return;
        }

        const data = {
            sitterId: this.selectedSitterId,
            petId: this.selectedPetId,
            serviceDate: date,
            startTime: `${date}T${start}:00`,
            endTime: `${date}T${end}:00`,
            notes: notes || null
        };

        // 產生冪等性 key，防止重複點擊建立多筆預約
        if (!this._bookingIdempotencyKey) {
            this._bookingIdempotencyKey = crypto.randomUUID();
        }

        try {
            await API.bookings.create(data, this.currentUser.userId, this._bookingIdempotencyKey);
            this._bookingIdempotencyKey = null; // 成功後清除，下次預約產生新 key
            alert('預約已送出！');
            this.navigateTo('my-bookings');
        } catch (error) {
            console.error('Submit booking error:', error);
            alert('預約失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== 4. My Bookings ====================
    async loadMyBookings() {
        const container = document.getElementById('my-bookings-list');
        container.innerHTML = '';

        try {
            const res = await API.bookings.getByUser(this.currentUser.userId);
            this.myBookings = (res.data || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            this.bookingFilter = 'all';
            this.updateFilterButtons('all');
            this.renderMyBookings();
        } catch (error) {
            console.error('Load bookings error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    filterBookings(status) {
        this.bookingFilter = status;
        this.updateFilterButtons(status);
        this.renderMyBookings();
    },

    updateFilterButtons(active) {
        document.querySelectorAll('[id^="mbf-"]').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-ghost');
        });
        const activeBtn = document.getElementById(`mbf-${active}`);
        if (activeBtn) {
            activeBtn.classList.remove('btn-ghost');
            activeBtn.classList.add('btn-secondary');
        }
    },

    renderMyBookings() {
        const filtered = this.bookingFilter === 'all'
            ? this.myBookings
            : this.myBookings.filter(b => b.status === this.bookingFilter);

        const container = document.getElementById('my-bookings-list');

        if (!filtered.length) {
            container.innerHTML = '<p class="text-muted">沒有符合條件的預約</p>';
            return;
        }

        container.innerHTML = filtered.map(b => `
            <div class="booking-card">
                <div style="flex:1;min-width:200px;">
                    <h4 style="margin:0 0 var(--space-xs) 0;font-size:1rem;">
                        ${b.petName || '寵物'} — 保母：${b.sitterName || '-'}
                    </h4>
                    <p class="text-muted" style="margin:0;font-size:0.85rem;">
                        ${this.formatDateTime(b.startTime)} ~ ${this.formatDateTime(b.endTime)}
                    </p>
                    ${b.totalPrice ? `<p class="text-muted" style="margin:0;font-size:0.85rem;">費用：NT$ ${b.totalPrice}</p>` : ''}
                </div>
                <div style="display:flex;align-items:center;gap:var(--space-sm);">
                    <span class="badge ${this.getStatusBadgeClass(b.status)}">${this.getStatusText(b.status)}</span>
                    <button class="btn btn-ghost" onclick="CustomerApp.viewBookingDetail('${b.id}')">詳情</button>
                    ${(b.status === 'PENDING' || b.status === 'CONFIRMED')
                        ? `<button class="btn btn-ghost" style="color:var(--color-error);" onclick="CustomerApp.cancelBooking('${b.id}')">取消</button>`
                        : ''}
                </div>
            </div>
        `).join('');
    },

    async viewBookingDetail(bookingId) {
        const b = this.myBookings.find(x => x.id === bookingId);
        if (!b) return;

        const contentEl = document.getElementById('booking-detail-content');
        contentEl.innerHTML = `
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:1rem;margin-bottom:1.5rem;">
                <div>
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">訂單資訊</h4>
                    <p><strong>訂單編號：</strong>${b.id}</p>
                    <p><strong>狀態：</strong><span class="badge ${this.getStatusBadgeClass(b.status)}">${this.getStatusText(b.status)}</span></p>
                    <p><strong>建立時間：</strong>${this.formatDateTime(b.createdAt)}</p>
                </div>
                <div>
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">服務資訊</h4>
                    <p><strong>寵物：</strong>${b.petName || '-'}</p>
                    <p><strong>保母：</strong>${b.sitterName || '-'}</p>
                    <p><strong>費用：</strong>${b.totalPrice ? 'NT$ ' + b.totalPrice : '-'}</p>
                </div>
            </div>
            <div style="margin-bottom:1rem;">
                <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">服務時間</h4>
                <p><strong>開始：</strong>${this.formatDateTime(b.startTime)}</p>
                <p><strong>結束：</strong>${this.formatDateTime(b.endTime)}</p>
            </div>
            ${b.notes ? `
                <div style="padding:1rem;background:var(--color-accent);border-radius:var(--radius-md);margin-bottom:1rem;">
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">備註</h4>
                    <p style="margin:0;">${b.notes}</p>
                </div>` : ''}
            ${b.sitterResponse ? `
                <div style="padding:1rem;background:var(--color-accent);border-radius:var(--radius-md);">
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">保母回覆</h4>
                    <p style="margin:0;">${b.sitterResponse}</p>
                </div>` : ''}
        `;

        this.showModal('booking-detail-modal');
    },

    async cancelBooking(bookingId) {
        if (!confirm('確定要取消此預約嗎？')) return;

        try {
            await API.bookings.cancel(bookingId);
            alert('預約已取消');
            this.loadMyBookings();
        } catch (error) {
            console.error('Cancel booking error:', error);
            alert('取消失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== 5. Ratings ====================
    async loadRatings() {
        const container = document.getElementById('ratings-content');
        container.innerHTML = '';

        try {
            const userId = this.currentUser.userId;
            const [bookingsRes, ratingsRes] = await Promise.all([
                API.bookings.getByUser(userId),
                API.ratings.getByUser(userId)
            ]);

            const bookings = bookingsRes.data || [];
            const ratings = ratingsRes.data || [];
            this.myRatings = ratings;

            const completedBookings = bookings.filter(b => b.status === 'COMPLETED');

            if (!completedBookings.length) {
                container.innerHTML = '<p class="text-muted">目前沒有已完成的預約可以評價</p>';
                return;
            }

            // Build a map of bookingId -> rating
            const ratingMap = {};
            ratings.forEach(r => {
                ratingMap[r.bookingId] = r;
            });

            // Separate into unrated and rated
            const unrated = completedBookings.filter(b => !ratingMap[b.id]);
            const rated = completedBookings.filter(b => ratingMap[b.id]);

            let html = '';

            if (unrated.length) {
                html += '<h3 style="margin-bottom:var(--space-md);color:var(--color-primary);">待評價</h3>';
                html += unrated.map(b => `
                    <div class="rating-card">
                        <div style="display:flex;justify-content:space-between;align-items:center;">
                            <div>
                                <h4 style="margin:0 0 var(--space-xs) 0;">${b.petName || '寵物'} — 保母：${b.sitterName || '-'}</h4>
                                <p class="text-muted" style="margin:0;font-size:0.85rem;">${this.formatDateTime(b.startTime)}</p>
                            </div>
                            <button class="btn btn-primary" onclick="CustomerApp.openRatingModal('${b.id}', '${b.sitterId || ''}', '${b.sitterName || ''}')">撰寫評價</button>
                        </div>
                    </div>
                `).join('');
            }

            if (rated.length) {
                html += '<h3 style="margin:var(--space-xl) 0 var(--space-md);color:var(--color-primary);">已評價</h3>';
                html += rated.map(b => {
                    const r = ratingMap[b.id];
                    const score = r.score || r.overallRating || 0;
                    return `
                        <div class="rating-card">
                            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                                <div>
                                    <h4 style="margin:0 0 var(--space-xs) 0;">${b.petName || '寵物'} — 保母：${b.sitterName || '-'}</h4>
                                    <p class="text-muted" style="margin:0 0 var(--space-sm) 0;font-size:0.85rem;">${this.formatDateTime(b.startTime)}</p>
                                    <div style="margin-bottom:var(--space-xs);">
                                        <span class="stars-display">${this.renderStars(score)}</span>
                                        <span style="font-weight:600;margin-left:var(--space-sm);">${score}/5</span>
                                    </div>
                                    ${r.comment ? `<p style="margin:var(--space-sm) 0 0 0;color:var(--color-text);">${r.comment}</p>` : ''}
                                </div>
                                <span class="badge badge-success">已評價</span>
                            </div>
                        </div>
                    `;
                }).join('');
            }

            container.innerHTML = html || '<p class="text-muted">目前沒有已完成的預約可以評價</p>';
        } catch (error) {
            console.error('Load ratings error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    setupRatingStars() {
        document.querySelectorAll('#rating-stars .star').forEach(star => {
            star.addEventListener('click', () => {
                const val = parseInt(star.dataset.value);
                document.getElementById('rating-score').value = val;
                document.querySelectorAll('#rating-stars .star').forEach(s => {
                    s.classList.toggle('filled', parseInt(s.dataset.value) <= val);
                });
            });
            star.addEventListener('mouseenter', () => {
                const val = parseInt(star.dataset.value);
                document.querySelectorAll('#rating-stars .star').forEach(s => {
                    s.classList.toggle('filled', parseInt(s.dataset.value) <= val);
                });
            });
        });

        document.getElementById('rating-stars').addEventListener('mouseleave', () => {
            const currentVal = parseInt(document.getElementById('rating-score').value) || 0;
            document.querySelectorAll('#rating-stars .star').forEach(s => {
                s.classList.toggle('filled', parseInt(s.dataset.value) <= currentVal);
            });
        });
    },

    openRatingModal(bookingId, sitterId, sitterName) {
        document.getElementById('rating-booking-id').value = bookingId;
        document.getElementById('rating-sitter-id').value = sitterId;
        document.getElementById('rating-sitter-name').textContent = sitterName || '-';
        document.getElementById('rating-score').value = '0';
        document.getElementById('rating-comment').value = '';
        document.querySelectorAll('#rating-stars .star').forEach(s => s.classList.remove('filled'));
        this.showModal('rating-modal');
    },

    async submitRating(event) {
        event.preventDefault();

        const score = parseInt(document.getElementById('rating-score').value);
        if (!score || score < 1) {
            alert('請選擇評分');
            return;
        }

        const data = {
            bookingId: document.getElementById('rating-booking-id').value,
            sitterId: document.getElementById('rating-sitter-id').value,
            score: score,
            comment: document.getElementById('rating-comment').value.trim() || null
        };

        try {
            await API.ratings.create(data, this.currentUser.userId);
            alert('評價已送出！');
            this.hideModal('rating-modal');
            this.loadRatings();
        } catch (error) {
            console.error('Submit rating error:', error);
            alert('評價失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== Helpers ====================
    formatDateTime(dateTimeStr) {
        if (!dateTimeStr) return '-';
        try {
            const date = new Date(dateTimeStr);
            return date.toLocaleString('zh-TW', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit'
            });
        } catch {
            return dateTimeStr;
        }
    },

    getStatusText(status) {
        const map = {
            'PENDING': '待確認',
            'CONFIRMED': '已確認',
            'COMPLETED': '已完成',
            'CANCELLED': '已取消'
        };
        return map[status] || status;
    },

    getStatusBadgeClass(status) {
        const map = {
            'PENDING': 'badge-warning',
            'CONFIRMED': 'badge-info',
            'COMPLETED': 'badge-success',
            'CANCELLED': 'badge-error'
        };
        return map[status] || '';
    },

    getGenderText(gender) {
        if (gender === 'MALE') return '公';
        if (gender === 'FEMALE') return '母';
        return '-';
    },

    renderStars(rating) {
        const r = Math.round(rating || 0);
        let stars = '';
        for (let i = 1; i <= 5; i++) {
            stars += i <= r ? '★' : '☆';
        }
        return stars;
    }
};
