// ===== ADMIN DASHBOARD JS =====

document.addEventListener('DOMContentLoaded', () => {
    // Redirect if not admin
    const user = getCurrentUser();
    if (!user || user.role !== 'ADMIN') {
        window.location.href = 'login.html';
        return;
    }

    // Set admin name in sidebar
    document.getElementById('adminName').textContent = user.fullName || user.username;

    // Load default section
    showSection('overview');
    loadDashboardStats();
});

/**
 * Show a dashboard section and hide others
 */
function showSection(name) {
    document.querySelectorAll('.dashboard-section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.sidebar-nav a').forEach(a => a.classList.remove('active'));

    const section = document.getElementById('section-' + name);
    const link    = document.getElementById('nav-' + name);
    if (section) section.classList.add('active');
    if (link)    link.classList.add('active');

    // Load data for the section
    switch (name) {
        case 'overview':   loadDashboardStats(); break;
        case 'equipment':  loadEquipmentTable(); break;
        case 'bookings':   loadBookingsTable();  break;
        case 'users':      loadUsersTable();     break;
        case 'payments':   loadPaymentsTable();  break;
    }
}

// ===== OVERVIEW / STATS =====

async function loadDashboardStats() {
    try {
        const result = await apiCall('/admin/dashboard');
        if (result.success && result.data) {
            const d = result.data;
            setText('statTotalUsers',      d.totalUsers);
            setText('statTotalEquipment',  d.totalEquipment);
            setText('statAvailable',       d.availableEquipment);
            setText('statTotalBookings',   d.totalBookings);
        setText('statTotalRevenue', '₹' + Number(d.totalRevenue || 0).toLocaleString('en-IN'));
            setText('statActiveBookings',  d.activeBookings);
        }
    } catch (e) {
        console.error('Failed to load stats:', e);
    }

    // Also load recent data
    loadRecentBookings();
    loadRecentEquipment();
}

async function loadRecentBookings() {
    try {
        const result = await apiCall('/bookings');
        if (result.success) {
            const bookings = (result.data || []).slice(0, 5);
            const list = document.getElementById('recentBookingsList');
            if (!list) return;

            if (!bookings.length) {
                list.innerHTML = '<div style="padding:20px;text-align:center;color:#757575;">No bookings yet</div>';
                return;
            }

            list.innerHTML = bookings.map(b => `
                <div class="recent-item">
                    <div class="recent-item-icon">📋</div>
                    <div class="recent-item-info">
                        <strong>${b.equipment?.name || 'Unknown'}</strong>
                        <span>${b.user?.fullName || 'Unknown'} • ${b.startDate} → ${b.endDate}</span>
                    </div>
                    <span class="badge badge-${b.status?.toLowerCase()}">${b.status}</span>
                </div>`).join('');
        }
    } catch (e) { console.error(e); }
}

async function loadRecentEquipment() {
    try {
        const result = await apiCall('/equipment');
        if (result.success) {
            const items = (result.data || []).slice(0, 5);
            const list = document.getElementById('recentEquipmentList');
            if (!list) return;

            list.innerHTML = items.map(eq => `
                <div class="recent-item">
                    <div class="recent-item-icon">🚜</div>
                    <div class="recent-item-info">
                        <strong>${eq.name}</strong>
                        <span>${eq.category} • ₹${Number(eq.dailyRate).toLocaleString('en-IN')}/day</span>
                    </div>
                    <span class="badge badge-${eq.status?.toLowerCase()}">${eq.status}</span>
                </div>`).join('');
        }
    } catch (e) { console.error(e); }
}

// ===== EQUIPMENT MANAGEMENT =====

async function loadEquipmentTable() {
    try {
        const result = await apiCall('/equipment');
        if (result.success) {
            renderEquipmentTable(result.data || []);
        }
    } catch (e) {
        console.error('Failed to load equipment:', e);
    }
}

function renderEquipmentTable(list) {
    const tbody = document.getElementById('equipmentTableBody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;padding:30px;color:#757575;">No equipment found</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(eq => `
        <tr>
            <td>${eq.id}</td>
            <td><strong>${eq.name}</strong></td>
            <td>${eq.category}</td>
            <td>₹${Number(eq.dailyRate).toLocaleString('en-IN')}/day</td>
            <td>${eq.quantity}</td>
            <td><span class="badge badge-${eq.status?.toLowerCase()}">${eq.status}</span></td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-outline" onclick="editEquipment(${eq.id})">✏️ Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteEquipment(${eq.id}, '${eq.name}')">🗑️ Delete</button>
                </div>
            </td>
        </tr>`).join('');
}

function openAddEquipmentModal() {
    document.getElementById('equipmentModalTitle').textContent = '➕ Add New Equipment';
    document.getElementById('equipmentForm').reset();
    document.getElementById('equipmentId').value = '';
    document.getElementById('equipmentAlert').style.display = 'none';
    openModal('equipmentModal');
}

async function editEquipment(id) {
    try {
        const result = await apiCall('/equipment/' + id);
        if (result.success) {
            const eq = result.data;
            document.getElementById('equipmentModalTitle').textContent = '✏️ Edit Equipment';
            document.getElementById('equipmentId').value          = eq.id;
            document.getElementById('eqName').value               = eq.name;
            document.getElementById('eqCategory').value           = eq.category;
            document.getElementById('eqDailyRate').value          = eq.dailyRate;
            document.getElementById('eqQuantity').value           = eq.quantity;
            document.getElementById('eqLocation').value           = eq.location || '';
            document.getElementById('eqStatus').value             = eq.status;
            document.getElementById('eqDescription').value        = eq.description || '';
            document.getElementById('equipmentAlert').style.display = 'none';
            openModal('equipmentModal');
        }
    } catch (e) {
        alert('Failed to load equipment details');
    }
}

async function saveEquipment() {
    const id = document.getElementById('equipmentId').value;
    const payload = {
        name:        document.getElementById('eqName').value.trim(),
        category:    document.getElementById('eqCategory').value,
        dailyRate:   parseFloat(document.getElementById('eqDailyRate').value),
        quantity:    parseInt(document.getElementById('eqQuantity').value),
        location:    document.getElementById('eqLocation').value.trim(),
        status:      document.getElementById('eqStatus').value,
        description: document.getElementById('eqDescription').value.trim()
    };

    if (!payload.name || !payload.category || !payload.dailyRate) {
        showEquipmentAlert('Please fill in all required fields', 'error');
        return;
    }

    const saveBtn = document.getElementById('saveEquipmentBtn');
    saveBtn.disabled = true;
    saveBtn.textContent = '⏳ Saving...';

    try {
        const url    = id ? `/equipment/${id}` : '/equipment';
        const method = id ? 'PUT' : 'POST';
        const result = await apiCall(url, method, payload);

        if (result.success) {
            closeModal('equipmentModal');
            showSuccessToast(id ? '✅ Equipment updated!' : '✅ Equipment added!');
            loadEquipmentTable();
            loadDashboardStats();
        } else {
            showEquipmentAlert(result.message || 'Save failed', 'error');
        }
    } catch (e) {
        showEquipmentAlert('Connection error', 'error');
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = '💾 Save Equipment';
    }
}

async function deleteEquipment(id, name) {
    if (!confirm(`Are you sure you want to delete "${name}"? This cannot be undone.`)) return;

    try {
        const result = await apiCall('/equipment/' + id, 'DELETE');
        if (result.success) {
            showSuccessToast('🗑️ Equipment deleted successfully');
            loadEquipmentTable();
            loadDashboardStats();
        } else {
            alert('Delete failed: ' + result.message);
        }
    } catch (e) {
        alert('Connection error');
    }
}

function showEquipmentAlert(msg, type) {
    const el = document.getElementById('equipmentAlert');
    el.textContent = msg;
    el.className = `alert alert-${type}`;
    el.style.display = 'block';
}

// ===== BOOKINGS MANAGEMENT =====

async function loadBookingsTable() {
    try {
        const result = await apiCall('/bookings');
        if (result.success) {
            renderBookingsTable(result.data || []);
        }
    } catch (e) {
        console.error('Failed to load bookings:', e);
    }
}

function renderBookingsTable(list) {
    const tbody = document.getElementById('bookingsTableBody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center;padding:30px;color:#757575;">No bookings found</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(b => `
        <tr>
            <td>#${b.id}</td>
            <td>${b.user?.fullName || 'N/A'}</td>
            <td>${b.equipment?.name || 'N/A'}</td>
            <td>${b.startDate}</td>
            <td>${b.endDate}</td>
            <td>₹${b.totalAmount ? Number(b.totalAmount).toLocaleString('en-IN') : 'N/A'}</td>
            <td><span class="badge badge-${b.status?.toLowerCase()}">${b.status}</span></td>
            <td>
                <div class="action-btns">
                    ${b.status === 'CONFIRMED' || b.status === 'PENDING'
                        ? `<button class="btn btn-sm btn-primary" onclick="completeBooking(${b.id})">✅ Complete</button>
                           <button class="btn btn-sm btn-danger" onclick="cancelBooking(${b.id})">❌ Cancel</button>`
                        : `<span style="color:#757575;font-size:0.85rem;">${b.status}</span>`
                    }
                    <a href="/api/invoice/${b.id}" target="_blank" class="btn btn-sm" style="background:#1b5e20;color:white;border-radius:20px;">📄 Invoice</a>
                </div>
            </td>
        </tr>`).join('');
}

async function completeBooking(id) {
    if (!confirm('Mark this booking as completed?')) return;
    try {
        const result = await apiCall(`/bookings/${id}/complete`, 'PUT');
        if (result.success) {
            showSuccessToast('✅ Booking marked as completed');
            loadBookingsTable();
            loadDashboardStats();
        } else {
            alert('Failed: ' + result.message);
        }
    } catch (e) { alert('Connection error'); }
}

async function cancelBooking(id) {
    if (!confirm('Cancel this booking? The equipment will be made available again.')) return;
    try {
        const result = await apiCall(`/bookings/${id}/cancel`, 'PUT');
        if (result.success) {
            showSuccessToast('❌ Booking cancelled');
            loadBookingsTable();
            loadDashboardStats();
        } else {
            alert('Failed: ' + result.message);
        }
    } catch (e) { alert('Connection error'); }
}

// ===== USERS MANAGEMENT =====

async function loadUsersTable() {
    try {
        const result = await apiCall('/admin/users');
        if (result.success) {
            renderUsersTable(result.data || []);
        }
    } catch (e) {
        console.error('Failed to load users:', e);
    }
}

function renderUsersTable(list) {
    const tbody = document.getElementById('usersTableBody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align:center;padding:30px;color:#757575;">No users found</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(u => `
        <tr>
            <td>${u.id}</td>
            <td><strong>${u.fullName}</strong></td>
            <td>${u.username}</td>
            <td>${u.email}</td>
            <td><span class="badge ${u.role === 'ADMIN' ? 'badge-confirmed' : 'badge-available'}">${u.role}</span></td>
            <td>
                ${u.role !== 'ADMIN'
                    ? `<button class="btn btn-sm btn-danger" onclick="deleteUser(${u.id}, '${u.username}')">🗑️ Delete</button>`
                    : '<span style="color:#757575;font-size:0.85rem;">Protected</span>'
                }
            </td>
        </tr>`).join('');
}

async function deleteUser(id, username) {
    if (!confirm(`Delete user "${username}"? This cannot be undone.`)) return;
    try {
        const result = await apiCall('/admin/users/' + id, 'DELETE');
        if (result.success) {
            showSuccessToast('🗑️ User deleted');
            loadUsersTable();
            loadDashboardStats();
        } else {
            alert('Failed: ' + result.message);
        }
    } catch (e) { alert('Connection error'); }
}

// ===== PAYMENTS MANAGEMENT =====

async function loadPaymentsTable() {
    try {
        // Load payment summary stats
        const summaryResult = await apiCall('/payments/summary');
        if (summaryResult.success) {
            const s = summaryResult.data;
            setText('payStatRevenue', '₹' + Number(s.totalRevenue || 0).toLocaleString('en-IN'));
            setText('payStatPaid',    s.paidCount);
            setText('payStatPending', s.pendingCount);
        }

        // Load all payments
        const result = await apiCall('/payments');
        if (result.success) {
            renderPaymentsTable(result.data || []);
        }
    } catch (e) {
        console.error('Failed to load payments:', e);
    }
}

function renderPaymentsTable(list) {
    const tbody = document.getElementById('paymentsTableBody');
    if (!tbody) return;

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="9" style="text-align:center;padding:30px;color:#757575;">No payments found</td></tr>';
        return;
    }

    tbody.innerHTML = list.map(p => `
        <tr>
            <td>#${p.id}</td>
            <td style="font-family:monospace; font-size:0.78rem; color:#0277bd;">${p.transactionId || '—'}</td>
            <td style="font-family:monospace; font-size:0.78rem; color:#e65100; font-weight:600;">
                ${p.farmerTransactionId
                    ? `<span title="Farmer submitted this ID">${p.farmerTransactionId}</span>`
                    : '<span style="color:#bbb;">Not submitted</span>'
                }
            </td>
            <td>${p.booking?.user?.fullName || 'N/A'}</td>
            <td>${p.booking?.equipment?.name || 'N/A'}</td>
            <td style="font-weight:700; color:#2e7d32;">₹${p.amount ? Number(p.amount).toLocaleString('en-IN') : 'N/A'}</td>
            <td>${p.paymentMethod || 'N/A'}</td>
            <td><span class="badge badge-${p.paymentStatus?.toLowerCase()}">${p.paymentStatus}</span></td>
            <td style="font-size:0.82rem;">${p.paymentDate ? p.paymentDate.substring(0,10) : '—'}</td>
            <td>
                <div class="action-btns">
                    ${p.paymentStatus === 'PENDING'
                        ? `<button class="btn btn-sm btn-primary" onclick="processPayment(${p.id})" title="Verify farmer TXN ID and confirm">✅ Confirm</button>`
                        : ''
                    }
                    ${p.paymentStatus === 'PAID'
                        ? `<button class="btn btn-sm btn-danger" onclick="refundPayment(${p.id})">↩️ Refund</button>`
                        : ''
                    }
                    ${p.paymentStatus === 'REFUNDED' || p.paymentStatus === 'FAILED'
                        ? `<span style="color:#757575; font-size:0.85rem;">${p.paymentStatus}</span>`
                        : ''
                    }
                </div>
            </td>
        </tr>`).join('');
}

async function processPayment(id) {
    if (!confirm('Confirm this payment as PAID?')) return;
    try {
        const result = await apiCall(`/payments/${id}/process`, 'PUT');
        if (result.success) {
            showSuccessToast('✅ Payment confirmed! ' + result.data.transactionId);
            loadPaymentsTable();
            loadDashboardStats();
        } else {
            alert('Failed: ' + result.message);
        }
    } catch (e) { alert('Connection error'); }
}

async function refundPayment(id) {
    if (!confirm('Refund this payment? Booking will be cancelled.')) return;
    try {
        const result = await apiCall(`/payments/${id}/refund`, 'PUT');
        if (result.success) {
            showSuccessToast('↩️ Payment refunded successfully');
            loadPaymentsTable();
            loadDashboardStats();
        } else {
            alert('Failed: ' + result.message);
        }
    } catch (e) { alert('Connection error'); }
}

// ===== MODAL HELPERS =====
function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// ===== UTILITY =====
function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.textContent = value;
}

function showSuccessToast(message) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position:fixed; bottom:24px; right:24px; z-index:9999;
        background:#2e7d32; color:white; padding:16px 24px;
        border-radius:10px; box-shadow:0 4px 20px rgba(0,0,0,0.2);
        font-weight:600; font-size:0.95rem; max-width:360px;`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 3500);
}
