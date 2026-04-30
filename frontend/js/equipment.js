// ===== EQUIPMENT PAGE JS =====

let allEquipment = [];
let selectedEquipment = null;

const CATEGORY_ICONS = {
    'Tractor':   '🚜',
    'Harvester': '🌾',
    'Tiller':    '⚙️',
    'Irrigation':'💧',
    'Sprayer':   '🌿',
    'Planting':  '🌱',
    'Excavator': '🏗️',
    'Default':   '🔧'
};

document.addEventListener('DOMContentLoaded', () => {
    loadEquipment();
    checkUrlParams();

    // Search input live filter
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', filterEquipment);
    }

    // Category filter
    const categoryFilter = document.getElementById('categoryFilter');
    if (categoryFilter) {
        categoryFilter.addEventListener('change', filterEquipment);
    }

    // Status filter
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', filterEquipment);
    }
});

/**
 * Check URL params for pre-selected category
 */
function checkUrlParams() {
    const params = new URLSearchParams(window.location.search);
    const category = params.get('category');
    if (category) {
        const categoryFilter = document.getElementById('categoryFilter');
        if (categoryFilter) {
            categoryFilter.value = category;
        }
    }
}

/**
 * Load all equipment from API
 */
async function loadEquipment() {
    showLoading(true);
    try {
        const result = await apiCall('/equipment');
        if (result.success) {
            allEquipment = result.data || [];
            filterEquipment();
        } else {
            showError('Failed to load equipment: ' + result.message);
        }
    } catch (e) {
        showError('Cannot connect to server. Make sure the backend is running on port 8080.');
    } finally {
        showLoading(false);
    }
}

/**
 * Filter equipment based on search/category/status
 */
function filterEquipment() {
    const search   = (document.getElementById('searchInput')?.value || '').toLowerCase();
    const category = document.getElementById('categoryFilter')?.value || '';
    const status   = document.getElementById('statusFilter')?.value || '';

    let filtered = allEquipment.filter(eq => {
        const matchSearch   = !search   || eq.name.toLowerCase().includes(search) || eq.description?.toLowerCase().includes(search);
        const matchCategory = !category || eq.category === category;
        const matchStatus   = !status   || eq.status === status;
        return matchSearch && matchCategory && matchStatus;
    });

    renderEquipment(filtered);
}

/**
 * Render equipment cards
 */
function renderEquipment(list) {
    const grid = document.getElementById('equipmentGrid');
    const countEl = document.getElementById('equipmentCount');

    if (countEl) countEl.textContent = `${list.length} item${list.length !== 1 ? 's' : ''} found`;

    if (!list.length) {
        grid.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                <div class="empty-icon">🔍</div>
                <h3>No equipment found</h3>
                <p>Try adjusting your search or filters</p>
            </div>`;
        return;
    }

    grid.innerHTML = list.map(eq => createEquipmentCard(eq)).join('');
}

/**
 * Create HTML for one equipment card
 */
function createEquipmentCard(eq) {
    const icon   = CATEGORY_ICONS[eq.category] || CATEGORY_ICONS['Default'];
    const status = eq.status || 'UNAVAILABLE';
    const badgeClass = `badge-${status.toLowerCase()}`;
    const isAvailable = status === 'AVAILABLE';

    return `
    <div class="equipment-card">
        <div class="card-image">
            <span>${icon}</span>
            <div class="card-status">
                <span class="badge ${badgeClass}">${status}</span>
            </div>
        </div>
        <div class="card-body">
            <div class="card-category">${eq.category}</div>
            <div class="card-title">${eq.name}</div>
            <div class="card-description">${eq.description || 'No description available.'}</div>
            <div class="card-meta">
                <div class="card-price">
                    ₹${Number(eq.dailyRate).toLocaleString('en-IN')}
                    <span>/ day</span>
                </div>
                <div class="card-location">📍 ${eq.location || 'N/A'}</div>
            </div>
            <div class="card-actions">
                <button class="btn btn-outline btn-sm" onclick="viewDetails(${eq.id})">Details</button>
                ${isAvailable
                    ? `<button class="btn btn-primary btn-sm" onclick="openRentModal(${eq.id})">🚜 Rent Now</button>`
                    : `<button class="btn btn-sm" style="background:#eee;color:#999;cursor:not-allowed;" disabled>Not Available</button>`
                }
            </div>
        </div>
    </div>`;
}

/**
 * View equipment details in modal
 */
function viewDetails(id) {
    const eq = allEquipment.find(e => e.id === id);
    if (!eq) return;

    const icon = CATEGORY_ICONS[eq.category] || CATEGORY_ICONS['Default'];
    document.getElementById('detailsContent').innerHTML = `
        <div style="text-align:center; font-size:5rem; margin-bottom:16px;">${icon}</div>
        <table style="width:100%; border-collapse:collapse; font-size:0.92rem;">
            <tr><td style="padding:8px; color:#757575; width:40%;">Name</td><td style="padding:8px; font-weight:600;">${eq.name}</td></tr>
            <tr style="background:#f5f5f5"><td style="padding:8px; color:#757575;">Category</td><td style="padding:8px;">${eq.category}</td></tr>
            <tr><td style="padding:8px; color:#757575;">Daily Rate</td><td style="padding:8px; font-weight:700; color:#2e7d32; font-size:1.1rem;">₹${Number(eq.dailyRate).toLocaleString('en-IN')}/day</td></tr>
            <tr style="background:#f5f5f5"><td style="padding:8px; color:#757575;">Status</td><td style="padding:8px;"><span class="badge badge-${eq.status?.toLowerCase()}">${eq.status}</span></td></tr>
            <tr><td style="padding:8px; color:#757575;">Quantity</td><td style="padding:8px;">${eq.quantity} unit(s)</td></tr>
            <tr style="background:#f5f5f5"><td style="padding:8px; color:#757575;">Location</td><td style="padding:8px;">📍 ${eq.location || 'N/A'}</td></tr>
            <tr><td style="padding:8px; color:#757575; vertical-align:top;">Description</td><td style="padding:8px;">${eq.description || 'N/A'}</td></tr>
        </table>
        ${eq.status === 'AVAILABLE'
            ? `<button class="btn btn-primary btn-full" style="margin-top:20px;" onclick="closeModal('detailsModal'); openRentModal(${eq.id})">🚜 Rent This Equipment</button>`
            : ''
        }`;

    openModal('detailsModal');
}

/**
 * Open rent modal for a specific equipment
 */
function openRentModal(id) {
    const user = getCurrentUser();
    if (!user) {
        if (confirm('You need to login to rent equipment. Go to login page?')) {
            window.location.href = 'login.html';
        }
        return;
    }

    selectedEquipment = allEquipment.find(e => e.id === id);
    if (!selectedEquipment) return;

    // Populate rent modal
    document.getElementById('rentEquipmentName').textContent = selectedEquipment.name;
    document.getElementById('rentDailyRate').textContent = `₹${Number(selectedEquipment.dailyRate).toLocaleString('en-IN')}/day`;

    // Set min date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('rentStartDate').min = today;
    document.getElementById('rentEndDate').min   = today;
    document.getElementById('rentStartDate').value = '';
    document.getElementById('rentEndDate').value   = '';
    document.getElementById('rentTotalAmount').textContent = '₹0';
    document.getElementById('rentAlert').style.display = 'none';

    openModal('rentModal');
}

/**
 * Calculate total rental cost when dates change
 */
function calculateRentTotal() {
    const start = document.getElementById('rentStartDate').value;
    const end   = document.getElementById('rentEndDate').value;

    if (start && end && selectedEquipment) {
        const startDate = new Date(start);
        const endDate   = new Date(end);

        if (endDate < startDate) {
            document.getElementById('rentTotalAmount').textContent = 'Invalid dates';
            return;
        }

        const days = Math.floor((endDate - startDate) / (1000 * 60 * 60 * 24)) + 1;
        const total = days * Number(selectedEquipment.dailyRate);
        document.getElementById('rentTotalAmount').textContent =
            `₹${total.toLocaleString('en-IN')} (${days} day${days > 1 ? 's' : ''})`;
    }
}

/**
 * Submit rent form
 */
async function submitRent() {
    const user = getCurrentUser();
    if (!user || !selectedEquipment) return;

    const startDate = document.getElementById('rentStartDate').value;
    const endDate   = document.getElementById('rentEndDate').value;
    const notes     = document.getElementById('rentNotes').value;

    if (!startDate || !endDate) {
        showRentAlert('Please select both start and end dates', 'error');
        return;
    }

    if (new Date(endDate) < new Date(startDate)) {
        showRentAlert('End date must be after start date', 'error');
        return;
    }

    const rentBtn = document.getElementById('rentSubmitBtn');
    rentBtn.disabled = true;
    rentBtn.textContent = '⏳ Processing...';

    try {
        const result = await apiCall('/rent', 'POST', {
            userId:      user.userId,
            equipmentId: selectedEquipment.id,
            startDate,
            endDate,
            notes
        });

        if (result.success) {
            // Auto-create payment for the booking
            let paymentId = null;
            try {
                const payResult = await apiCall('/payments', 'POST', {
                    bookingId: result.data.id,
                    paymentMethod: 'UPI',
                    notes: 'Awaiting UPI payment'
                });
                if (payResult.success) paymentId = payResult.data.id;
            } catch (e) { /* continue */ }

            closeModal('rentModal');

            // Redirect to payment page
            const bookingId = result.data.id;
            const payUrl = `payment.html?bookingId=${bookingId}${paymentId ? '&paymentId=' + paymentId : ''}`;

            showSuccessToast('🎉 Booking created! Redirecting to payment...');
            setTimeout(() => {
                window.location.href = payUrl;
            }, 1500);
        } else {
            showRentAlert(result.message || 'Rental failed. Please try again.', 'error');
        }
    } catch (e) {
        showRentAlert('Connection error. Please try again.', 'error');
    } finally {
        rentBtn.disabled = false;
        rentBtn.textContent = '✅ Confirm Rental';
    }
}

function showRentAlert(msg, type) {
    const el = document.getElementById('rentAlert');
    el.textContent = msg;
    el.className = `alert alert-${type}`;
    el.style.display = 'block';
}

// ===== MODAL HELPERS =====
function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// ===== LOADING / ERROR =====
function showLoading(show) {
    const loader = document.getElementById('loadingState');
    const grid   = document.getElementById('equipmentGrid');
    if (loader) loader.style.display = show ? 'block' : 'none';
    if (grid)   grid.style.display   = show ? 'none'  : 'grid';
}

function showError(msg) {
    const grid = document.getElementById('equipmentGrid');
    if (grid) {
        grid.style.display = 'grid';
        grid.innerHTML = `
            <div class="empty-state" style="grid-column:1/-1">
                <div class="empty-icon">⚠️</div>
                <h3>Error Loading Equipment</h3>
                <p>${msg}</p>
                <button class="btn btn-primary" style="margin-top:16px" onclick="loadEquipment()">Retry</button>
            </div>`;
    }
}

// ===== TOAST NOTIFICATION =====
function showSuccessToast(message) {
    const toast = document.createElement('div');
    toast.style.cssText = `
        position:fixed; bottom:24px; right:24px; z-index:9999;
        background:#2e7d32; color:white; padding:16px 24px;
        border-radius:10px; box-shadow:0 4px 20px rgba(0,0,0,0.2);
        font-weight:600; font-size:0.95rem; max-width:360px;
        animation: slideUp 0.3s ease;`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

// ===== MY RENTALS with Invoice Download =====
async function showMyRentals() {
    const user = getCurrentUser();
    if (!user) return;

    openModal('myRentalsModal');
    const list = document.getElementById('myRentalsList');
    list.innerHTML = '<div style="text-align:center;padding:30px;color:#757575;">⏳ Loading...</div>';

    try {
        const result = await apiCall('/bookings?userId=' + user.userId);
        if (result.success && result.data.length > 0) {
            list.innerHTML = result.data.map(b => `
                <div style="border:2px solid #e8f5e9; border-radius:10px; padding:18px; margin-bottom:14px; background:#fafffe;">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                        <strong style="font-size:1rem; color:#1b5e20;">${b.equipment?.name || 'N/A'}</strong>
                        <span class="badge badge-${b.status?.toLowerCase()}">${b.status}</span>
                    </div>
                    <p style="font-size:0.88rem; color:#757575; margin-bottom:4px;">
                        📅 ${b.startDate} → ${b.endDate}
                        <span style="background:#e8f5e9; color:#2e7d32; padding:2px 8px; border-radius:10px; margin-left:6px; font-size:0.8rem;">
                            ${b.totalDays} day${b.totalDays > 1 ? 's' : ''}
                        </span>
                    </p>
                    <p style="font-size:1rem; color:#2e7d32; font-weight:800; margin-bottom:10px;">
                        💰 Total: ₹${b.totalAmount ? Number(b.totalAmount).toLocaleString('en-IN') : 'N/A'}
                    </p>
                    ${b.notes ? `<p style="font-size:0.85rem; color:#757575; margin-bottom:10px;">📝 ${b.notes}</p>` : ''}
                    <div style="display:flex; gap:8px; flex-wrap:wrap;">
                        <a href="/api/invoice/${b.id}"
                           target="_blank"
                           style="display:inline-flex; align-items:center; gap:6px; background:linear-gradient(135deg,#1b5e20,#2e7d32); color:white; padding:8px 16px; border-radius:20px; font-size:0.85rem; font-weight:600; text-decoration:none; transition:all 0.3s;">
                            📄 Download Invoice
                        </a>
                        ${b.status === 'CONFIRMED' || b.status === 'PENDING'
                            ? `<a href="payment.html?bookingId=${b.id}"
                                  style="display:inline-flex; align-items:center; gap:6px; background:linear-gradient(135deg,#e65100,#ff6f00); color:white; padding:8px 16px; border-radius:20px; font-size:0.85rem; font-weight:600; text-decoration:none;">
                                   💳 Pay Now
                               </a>`
                            : ''
                        }
                    </div>
                </div>`).join('');
        } else {
            list.innerHTML = `
                <div style="text-align:center; padding:40px; color:#757575;">
                    <div style="font-size:3rem; margin-bottom:12px;">📋</div>
                    <p>No rentals yet.</p>
                    <a href="equipment.html" style="color:#2e7d32; font-weight:600;">Browse equipment</a> to get started!
                </div>`;
        }
    } catch (e) {
        list.innerHTML = '<div style="text-align:center;padding:30px;color:#c62828;">Failed to load rentals</div>';
    }
}
