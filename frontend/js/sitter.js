/**
 * Pet Care - Sitter Page SPA Logic
 */

document.addEventListener('DOMContentLoaded', () => {
    const user = sessionStorage.getItem(CONFIG.STORAGE_KEYS.USER);
    const token = sessionStorage.getItem(CONFIG.STORAGE_KEYS.ACCESS_TOKEN);
    if (!user || !token) {
        window.location.href = 'index.html';
        return;
    }

    const userData = JSON.parse(user);
    if (userData.role !== 'SITTER') {
        window.location.href = 'index.html';
        return;
    }

    SitterApp.currentUser = userData;

    document.getElementById('userName').textContent = userData.roleName || userData.username;
    document.getElementById('userAvatar').textContent = (userData.roleName || userData.username).charAt(0).toUpperCase();

    SitterApp.init();
});

const SitterApp = {
    currentUser: null,
    sitterId: null,
    currentPage: 'dashboard',
    allBookings: [],
    bookingFilter: 'all',
    availabilitySlots: [],

    async init() {
        this.setupNavigation();
        this.setupLogout();
        this.setupModals();

        // Get sitter profile to obtain sitterId
        try {
            const res = await API.sitterProfile.getByUserId(this.currentUser.userId);
            this.sitterId = res.data.id || res.data.sitterId;
            // Update display name from sitter entity
            const sitterName = res.data.name;
            if (sitterName) {
                document.getElementById('userName').textContent = sitterName;
                document.getElementById('userAvatar').textContent = sitterName.charAt(0).toUpperCase();
            }
        } catch (error) {
            console.error('Failed to load sitter profile:', error);
            alert('無法載入保母資料，請重新登入');
            sessionStorage.clear();
            window.location.href = 'index.html';
            return;
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
            case 'bookings': this.loadBookings(); break;
            case 'availability': this.loadAvailability(); break;
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
            const res = await API.sitterProfile.getBookings(this.sitterId);
            const bookings = res.data || res || [];

            const now = new Date();
            const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);

            const thisMonth = bookings.filter(b => new Date(b.startTime || b.createdAt) >= monthStart);
            const pending = thisMonth.filter(b => b.status === 'PENDING').length;
            const completed = thisMonth.filter(b => b.status === 'COMPLETED').length;
            const revenue = thisMonth.filter(b => b.status === 'COMPLETED')
                .reduce((sum, b) => sum + (b.totalPrice || 0), 0);

            document.getElementById('stat-pending').textContent = pending;
            document.getElementById('stat-completed').textContent = completed;
            document.getElementById('stat-revenue').textContent = 'NT$ ' + revenue;

            // Rating from sitter profile
            try {
                const sitterRes = await API.sitterProfile.getByUserId(this.currentUser.userId);
                const sitter = sitterRes.data || sitterRes;
                document.getElementById('stat-rating').textContent = sitter.averageRating
                    ? Number(sitter.averageRating).toFixed(1) + ' / 5'
                    : '-';
            } catch (_) {
                document.getElementById('stat-rating').textContent = '-';
            }

            // Revenue trend (last 7 days)
            const trendEl = document.getElementById('revenue-trend');
            const days = [];
            for (let i = 6; i >= 0; i--) {
                const d = new Date(now);
                d.setDate(d.getDate() - i);
                const dateStr = d.toISOString().split('T')[0];
                const dayBookings = bookings.filter(b =>
                    b.status === 'COMPLETED' && (b.startTime || '').substring(0, 10) === dateStr
                );
                if (dayBookings.length) {
                    days.push({
                        date: dateStr,
                        revenue: dayBookings.reduce((s, b) => s + (b.totalPrice || 0), 0),
                        count: dayBookings.length
                    });
                }
            }
            if (days.length) {
                trendEl.innerHTML = days.map(d => `
                    <div class="trend-item">
                        <span>${d.date}</span>
                        <span style="font-weight:600;">NT$ ${d.revenue}（${d.count} 筆）</span>
                    </div>
                `).join('');
            } else {
                trendEl.innerHTML = '<p class="text-muted">近7日暫無完成訂單</p>';
            }

            // Pending bookings
            const todayEl = document.getElementById('today-pending');
            const pendingBookings = bookings.filter(b => b.status === 'PENDING');
            if (pendingBookings.length) {
                todayEl.innerHTML = pendingBookings.map(b => `
                    <div class="booking-card" style="padding:var(--space-md);">
                        <div>
                            <strong>${b.pet?.name || '寵物'}</strong>
                            <div class="text-muted" style="font-size:0.85rem;">${this.formatDateTime(b.startTime)} ~ ${this.formatDateTime(b.endTime)}</div>
                        </div>
                        <span class="badge badge-warning">待確認</span>
                    </div>
                `).join('');
            } else {
                todayEl.innerHTML = '<p class="text-muted">目前無待處理預約</p>';
            }
        } catch (error) {
            console.error('Dashboard load error:', error);
        }
    },

    // ==================== 2. Bookings ====================
    async loadBookings() {
        const container = document.getElementById('bookings-list');
        container.innerHTML = '<p class="text-muted"></p>';

        try {
            const res = await API.sitterProfile.getBookings(this.sitterId);
            this.allBookings = (res.data || []).sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
            this.bookingFilter = 'all';
            this.updateFilterButtons('all');
            this.renderBookings();
        } catch (error) {
            console.error('Load bookings error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    filterBookings(status) {
        this.bookingFilter = status;
        this.updateFilterButtons(status);
        this.renderBookings();
    },

    updateFilterButtons(active) {
        document.querySelectorAll('[id^="bf-"]').forEach(btn => {
            btn.classList.remove('btn-secondary');
            btn.classList.add('btn-ghost');
        });
        const activeBtn = document.getElementById(`bf-${active}`);
        if (activeBtn) {
            activeBtn.classList.remove('btn-ghost');
            activeBtn.classList.add('btn-secondary');
        }
    },

    renderBookings() {
        const filtered = this.bookingFilter === 'all'
            ? this.allBookings
            : this.allBookings.filter(b => b.status === this.bookingFilter);

        const container = document.getElementById('bookings-list');

        if (!filtered.length) {
            container.innerHTML = '<p class="text-muted">沒有符合條件的預約</p>';
            return;
        }

        container.innerHTML = filtered.map(b => `
            <div class="booking-card">
                <div style="flex:1;min-width:200px;">
                    <h4 style="margin:0 0 var(--space-xs) 0;font-size:1rem;">
                        ${b.petName || '寵物'} — 飼主：${b.ownerName || b.customerName || '-'}
                    </h4>
                    <p class="text-muted" style="margin:0;font-size:0.85rem;">
                        ${this.formatDateTime(b.startTime)} ~ ${this.formatDateTime(b.endTime)}
                    </p>
                    ${b.totalPrice ? `<p class="text-muted" style="margin:0;font-size:0.85rem;">費用：NT$ ${b.totalPrice}</p>` : ''}
                </div>
                <div style="display:flex;align-items:center;gap:var(--space-sm);flex-wrap:wrap;">
                    <span class="badge ${this.getStatusBadgeClass(b.status)}">${this.getStatusText(b.status)}</span>
                    <button class="btn btn-ghost" onclick="SitterApp.viewBookingDetail('${b.id}')">詳情</button>
                    ${b.status === 'PENDING' ? `
                        <button class="btn btn-primary" style="padding:var(--space-sm) var(--space-md);" onclick="SitterApp.openConfirmModal('${b.id}')">確認</button>
                        <button class="btn btn-ghost" style="color:var(--color-error);" onclick="SitterApp.openRejectModal('${b.id}')">拒絕</button>
                    ` : ''}
                    ${b.status === 'CONFIRMED' ? `
                        <button class="btn btn-primary" style="padding:var(--space-sm) var(--space-md);" onclick="SitterApp.completeBooking('${b.id}')">完成</button>
                        <button class="btn btn-ghost" style="color:var(--color-error);" onclick="SitterApp.cancelBooking('${b.id}')">取消</button>
                    ` : ''}
                </div>
            </div>
        `).join('');
    },

    async viewBookingDetail(bookingId) {
        const b = this.allBookings.find(x => x.id === bookingId);
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
                    <p><strong>飼主：</strong>${b.ownerName || b.customerName || '-'}</p>
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
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">飼主備註</h4>
                    <p style="margin:0;">${b.notes}</p>
                </div>` : ''}
            ${b.sitterResponse ? `
                <div style="padding:1rem;background:var(--color-accent);border-radius:var(--radius-md);">
                    <h4 style="margin-bottom:0.5rem;color:var(--color-primary);">您的回覆</h4>
                    <p style="margin:0;">${b.sitterResponse}</p>
                </div>` : ''}
        `;

        this.showModal('booking-detail-modal');
    },

    // Confirm booking
    openConfirmModal(bookingId) {
        document.getElementById('confirm-booking-id').value = bookingId;
        document.getElementById('confirm-message').value = '';
        this.showModal('confirm-modal');
    },

    async doConfirmBooking() {
        const bookingId = document.getElementById('confirm-booking-id').value;
        const message = document.getElementById('confirm-message').value.trim();

        try {
            await API.sitterProfile.confirmBooking(this.sitterId, bookingId, message || null);
            alert('預約已確認');
            this.hideModal('confirm-modal');
            this.loadBookings();
        } catch (error) {
            console.error('Confirm booking error:', error);
            alert('操作失敗：' + (error.message || '未知錯誤'));
        }
    },

    // Reject booking
    openRejectModal(bookingId) {
        document.getElementById('reject-booking-id').value = bookingId;
        document.getElementById('reject-reason').value = '';
        this.showModal('reject-modal');
    },

    async doRejectBooking() {
        const bookingId = document.getElementById('reject-booking-id').value;
        const reason = document.getElementById('reject-reason').value.trim();

        try {
            await API.sitterProfile.rejectBooking(this.sitterId, bookingId, reason || null);
            alert('預約已拒絕');
            this.hideModal('reject-modal');
            this.loadBookings();
        } catch (error) {
            console.error('Reject booking error:', error);
            alert('操作失敗：' + (error.message || '未知錯誤'));
        }
    },

    // Complete booking
    async completeBooking(bookingId) {
        if (!confirm('確定要將此預約標記為已完成嗎？')) return;

        try {
            await API.sitterProfile.completeBooking(this.sitterId, bookingId);
            alert('預約已標記為完成');
            this.loadBookings();
        } catch (error) {
            console.error('Complete booking error:', error);
            alert('操作失敗：' + (error.message || '未知錯誤'));
        }
    },

    // Cancel booking
    async cancelBooking(bookingId) {
        const reason = prompt('請輸入取消原因（選填）：');
        if (reason === null) return; // user clicked cancel on prompt

        try {
            await API.sitterProfile.cancelBooking(this.sitterId, bookingId, reason || null);
            alert('預約已取消');
            this.loadBookings();
        } catch (error) {
            console.error('Cancel booking error:', error);
            alert('操作失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== 3. Availability ====================
    async loadAvailability() {
        const container = document.getElementById('availability-list');
        container.innerHTML = '<p class="text-muted"></p>';

        try {
            const res = await API.sitterProfile.getAvailability(this.sitterId);
            this.availabilitySlots = res.data || [];
            this.renderAvailability();
        } catch (error) {
            console.error('Load availability error:', error);
            container.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    renderAvailability() {
        const container = document.getElementById('availability-list');
        if (!this.availabilitySlots.length) {
            container.innerHTML = '<p class="text-muted">尚未設定可服務時段，請點擊右上角新增。</p>';
            return;
        }

        const dayOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
        const sorted = [...this.availabilitySlots].sort((a, b) =>
            dayOrder.indexOf(a.dayOfWeek) - dayOrder.indexOf(b.dayOfWeek)
        );

        container.innerHTML = sorted.map(slot => `
            <div class="availability-card">
                <div style="flex:1;min-width:200px;">
                    <h4 style="margin:0 0 var(--space-xs) 0;font-size:1rem;">
                        ${this.getDayText(slot.dayOfWeek)}
                    </h4>
                    <p class="text-muted" style="margin:0;font-size:0.85rem;">
                        ${slot.startTime || '-'} ~ ${slot.endTime || '-'}
                    </p>
                    ${slot.serviceArea ? `<p class="text-muted" style="margin:0;font-size:0.85rem;">區域：${slot.serviceArea}</p>` : ''}
                </div>
                <div style="display:flex;align-items:center;gap:var(--space-sm);">
                    <span class="badge ${slot.isActive || slot.active ? 'badge-success' : 'badge-error'}">
                        ${slot.isActive || slot.active ? '啟用中' : '已停用'}
                    </span>
                    <button class="btn btn-ghost" onclick="SitterApp.editAvailability('${slot.id}')">✏️ 編輯</button>
                    <button class="btn btn-ghost" style="color:var(--color-error);" onclick="SitterApp.deleteAvailability('${slot.id}')">🗑️ 刪除</button>
                </div>
            </div>
        `).join('');
    },

    openAddAvailabilityModal() {
        document.getElementById('availability-modal-title').textContent = '新增時段';
        document.getElementById('availability-form').reset();
        document.getElementById('avail-id').value = '';
        document.getElementById('avail-active').checked = true;
        this.showModal('availability-modal');
    },

    editAvailability(slotId) {
        const slot = this.availabilitySlots.find(s => s.id === slotId);
        if (!slot) return;

        document.getElementById('availability-modal-title').textContent = '編輯時段';
        document.getElementById('avail-id').value = slotId;
        document.getElementById('avail-day').value = slot.dayOfWeek || '';
        document.getElementById('avail-start').value = slot.startTime || '';
        document.getElementById('avail-end').value = slot.endTime || '';
        document.getElementById('avail-area').value = slot.serviceArea || '';
        document.getElementById('avail-active').checked = slot.isActive !== false && slot.active !== false;
        this.showModal('availability-modal');
    },

    async saveAvailability(event) {
        event.preventDefault();

        const slotId = document.getElementById('avail-id').value;
        const isEdit = !!slotId;

        const data = {
            dayOfWeek: document.getElementById('avail-day').value,
            startTime: document.getElementById('avail-start').value,
            endTime: document.getElementById('avail-end').value,
            serviceArea: document.getElementById('avail-area').value.trim() || null,
            isActive: document.getElementById('avail-active').checked
        };

        if (data.startTime >= data.endTime) {
            alert('結束時間必須晚於開始時間');
            return;
        }

        try {
            if (isEdit) {
                await API.sitterProfile.updateAvailability(this.sitterId, slotId, data);
            } else {
                await API.sitterProfile.addAvailability(this.sitterId, data);
            }
            this.hideModal('availability-modal');
            alert(isEdit ? '時段已更新' : '時段已新增');
            this.loadAvailability();
        } catch (error) {
            console.error('Save availability error:', error);
            alert('儲存失敗：' + (error.message || '未知錯誤'));
        }
    },

    async deleteAvailability(slotId) {
        if (!confirm('確定要刪除此時段嗎？')) return;

        try {
            await API.sitterProfile.deleteAvailability(this.sitterId, slotId);
            alert('時段已刪除');
            this.loadAvailability();
        } catch (error) {
            console.error('Delete availability error:', error);
            alert('刪除失敗：' + (error.message || '未知錯誤'));
        }
    },

    // ==================== 4. Ratings ====================
    async loadRatings() {
        const statsContainer = document.getElementById('ratings-stats');
        const listContainer = document.getElementById('ratings-list');
        statsContainer.innerHTML = '<p class="text-muted"></p>';
        listContainer.innerHTML = '';

        try {
            const [statsRes, ratingsRes] = await Promise.all([
                API.ratings.getStatsBySitter(this.sitterId),
                API.ratings.getBySitter(this.sitterId)
            ]);

            const stats = statsRes.data || statsRes;
            const ratings = ratingsRes.data?.content || ratingsRes.data || [];

            // Stats overview
            const avgRating = stats.averageRating || 0;
            const totalRatings = stats.totalRatings || 0;
            const fiveStarPct = stats.fiveStarPercentage || 0;
            const distribution = stats.distribution || {};

            let statsHtml = `
                <div class="stats-grid" style="margin-bottom:var(--space-xl);">
                    <div class="stat-card">
                        <div class="stat-icon">⭐</div>
                        <div class="stat-info"><h3>平均評分</h3><div class="stat-value">${Number(avgRating).toFixed(1)}</div></div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">📝</div>
                        <div class="stat-info"><h3>評價總數</h3><div class="stat-value">${totalRatings}</div></div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">🌟</div>
                        <div class="stat-info"><h3>五星好評率</h3><div class="stat-value">${Number(fiveStarPct).toFixed(0)}%</div></div>
                    </div>
                </div>
            `;

            // Distribution chart
            if (totalRatings > 0) {
                statsHtml += '<div class="card" style="margin-bottom:var(--space-xl);"><div class="card-header"><h3 class="card-title">評分分佈</h3></div>';
                for (let i = 5; i >= 1; i--) {
                    const count = distribution[i] || 0;
                    const pct = totalRatings > 0 ? (count / totalRatings * 100) : 0;
                    statsHtml += `
                        <div class="dist-bar-container">
                            <div class="dist-bar-label">${i}★</div>
                            <div class="dist-bar-track"><div class="dist-bar-fill" style="width:${pct}%;"></div></div>
                            <div class="dist-bar-count">${count}</div>
                        </div>
                    `;
                }
                statsHtml += '</div>';
            }

            statsContainer.innerHTML = statsHtml;

            // Ratings list
            if (!ratings.length) {
                listContainer.innerHTML = '<p class="text-muted">尚無評價</p>';
                return;
            }

            listContainer.innerHTML = '<h3 style="margin-bottom:var(--space-md);color:var(--color-primary);">所有評價</h3>' +
                ratings.map(r => {
                    const score = r.score || r.overallRating || 0;
                    const hasReply = !!(r.reply || r.sitterReply);
                    return `
                        <div class="rating-card">
                            <div style="display:flex;justify-content:space-between;align-items:flex-start;">
                                <div style="flex:1;">
                                    <div style="margin-bottom:var(--space-xs);">
                                        <span class="stars-display">${this.renderStars(score)}</span>
                                        <span style="font-weight:600;margin-left:var(--space-sm);">${score}/5</span>
                                    </div>
                                    ${r.comment ? `<p style="margin:var(--space-sm) 0 0 0;">${r.comment}</p>` : ''}
                                    <p class="text-muted" style="margin:var(--space-sm) 0 0 0;font-size:0.85rem;">
                                        — ${r.customerName || r.userName || '匿名'} | ${this.formatDateTime(r.createdAt)}
                                    </p>
                                    ${hasReply ? `
                                        <div class="reply-block">
                                            <strong>您的回覆：</strong>
                                            <p style="margin:var(--space-xs) 0 0 0;">${r.reply || r.sitterReply}</p>
                                        </div>
                                    ` : ''}
                                </div>
                                <div>
                                    ${hasReply
                                        ? '<span class="badge badge-success">已回覆</span>'
                                        : `<button class="btn btn-ghost" onclick="SitterApp.openReplyModal('${r.id}')">回覆</button>`
                                    }
                                </div>
                            </div>
                        </div>
                    `;
                }).join('');
        } catch (error) {
            console.error('Load ratings error:', error);
            statsContainer.innerHTML = '<p class="text-muted">載入失敗</p>';
        }
    },

    openReplyModal(ratingId) {
        document.getElementById('reply-rating-id').value = ratingId;
        document.getElementById('reply-content').value = '';
        this.showModal('reply-modal');
    },

    async submitReply(event) {
        event.preventDefault();

        const ratingId = document.getElementById('reply-rating-id').value;
        const reply = document.getElementById('reply-content').value.trim();

        if (!reply) {
            alert('請輸入回覆內容');
            return;
        }

        try {
            await API.sitterProfile.replyToRating(ratingId, this.sitterId, reply);
            alert('回覆已送出');
            this.hideModal('reply-modal');
            this.loadRatings();
        } catch (error) {
            console.error('Submit reply error:', error);
            alert('回覆失敗：' + (error.message || '未知錯誤'));
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

    getDayText(day) {
        const map = {
            'MONDAY': '星期一',
            'TUESDAY': '星期二',
            'WEDNESDAY': '星期三',
            'THURSDAY': '星期四',
            'FRIDAY': '星期五',
            'SATURDAY': '星期六',
            'SUNDAY': '星期日'
        };
        return map[day] || day;
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
