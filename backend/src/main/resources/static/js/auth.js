// ===== AUTH UTILITIES =====

const API_BASE = '/api';

/**
 * Get current logged-in user from localStorage
 */
function getCurrentUser() {
    const data = localStorage.getItem('currentUser');
    return data ? JSON.parse(data) : null;
}

/**
 * Check if user is logged in
 */
function isLoggedIn() {
    return getCurrentUser() !== null;
}

/**
 * Check if current user is admin
 */
function isAdmin() {
    const user = getCurrentUser();
    return user && user.role === 'ADMIN';
}

/**
 * Logout user
 */
function logout() {
    localStorage.removeItem('currentUser');
    window.location.href = 'index.html';
}

/**
 * Update navbar based on login state
 */
function updateNavbar() {
    const user = getCurrentUser();

    const navLogin    = document.getElementById('navLogin');
    const navRegister = document.getElementById('navRegister');
    const navDashboard= document.getElementById('navDashboard');
    const navLogout   = document.getElementById('navLogout');
    const navMyRentals= document.getElementById('navMyRentals');
    const navUserInfo = document.getElementById('navUserInfo');

    if (user) {
        if (navLogin)     navLogin.style.display    = 'none';
        if (navRegister)  navRegister.style.display = 'none';
        if (navLogout)    navLogout.style.display   = 'list-item';

        // Show user profile in navbar
        if (navUserInfo) {
            navUserInfo.style.display = 'list-item';
            navUserInfo.innerHTML = `
                <div style="display:flex; align-items:center; gap:8px; cursor:pointer;"
                     onclick="toggleProfileDropdown()" id="profileBtn">
                    <div style="
                        width:36px; height:36px;
                        background:linear-gradient(135deg,rgba(255,255,255,0.3),rgba(255,255,255,0.1));
                        border:2px solid rgba(255,255,255,0.5);
                        border-radius:50%;
                        display:flex; align-items:center; justify-content:center;
                        font-size:1rem; font-weight:700; color:white;
                        text-transform:uppercase;">
                        ${user.fullName ? user.fullName.charAt(0) : user.username.charAt(0)}
                    </div>
                    <span style="color:rgba(255,255,255,0.9); font-size:0.88rem; font-weight:600; max-width:100px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;">
                        ${user.fullName || user.username}
                    </span>
                    <span style="color:rgba(255,255,255,0.7); font-size:0.75rem;">▼</span>
                </div>
                <div id="profileDropdown" style="
                    display:none;
                    position:absolute;
                    top:68px; right:0;
                    background:white;
                    border-radius:12px;
                    box-shadow:0 8px 30px rgba(0,0,0,0.15);
                    min-width:220px;
                    z-index:9999;
                    overflow:hidden;
                    border:1px solid #e0f2f1;">
                    <!-- Profile Header -->
                    <div style="background:linear-gradient(135deg,#1b5e20,#2e7d32); padding:16px; text-align:center;">
                        <div style="
                            width:50px; height:50px;
                            background:rgba(255,255,255,0.2);
                            border:3px solid rgba(255,255,255,0.5);
                            border-radius:50%;
                            display:flex; align-items:center; justify-content:center;
                            font-size:1.4rem; font-weight:800; color:white;
                            margin:0 auto 8px; text-transform:uppercase;">
                            ${user.fullName ? user.fullName.charAt(0) : user.username.charAt(0)}
                        </div>
                        <div style="color:white; font-weight:700; font-size:0.95rem;">${user.fullName || user.username}</div>
                        <div style="color:rgba(255,255,255,0.75); font-size:0.78rem;">@${user.username}</div>
                        <div style="margin-top:6px;">
                            <span style="background:rgba(255,255,255,0.2); color:white; padding:2px 10px; border-radius:20px; font-size:0.72rem; font-weight:600;">
                                ${user.role === 'ADMIN' ? '👑 Admin' : '👨‍🌾 Farmer'}
                            </span>
                        </div>
                    </div>
                    <!-- Menu Items -->
                    <div style="padding:8px 0;">
                        <a href="profile.html" style="display:flex; align-items:center; gap:10px; padding:11px 18px; color:#263238; font-size:0.88rem; font-weight:500; transition:all 0.2s; text-decoration:none;"
                           onmouseover="this.style.background='#e8f5e9'" onmouseout="this.style.background='transparent'">
                            <span>👤</span> My Profile
                        </a>
                        <a href="equipment.html" onclick="showMyRentals ? showMyRentals() : null" style="display:flex; align-items:center; gap:10px; padding:11px 18px; color:#263238; font-size:0.88rem; font-weight:500; transition:all 0.2s; text-decoration:none;"
                           onmouseover="this.style.background='#e8f5e9'" onmouseout="this.style.background='transparent'">
                            <span>📋</span> My Rentals
                        </a>
                        ${user.role === 'ADMIN' ? `
                        <a href="dashboard.html" style="display:flex; align-items:center; gap:10px; padding:11px 18px; color:#263238; font-size:0.88rem; font-weight:500; transition:all 0.2s; text-decoration:none;"
                           onmouseover="this.style.background='#e8f5e9'" onmouseout="this.style.background='transparent'">
                            <span>📊</span> Dashboard
                        </a>` : ''}
                        <div style="border-top:1px solid #e0f2f1; margin:4px 0;"></div>
                        <a href="#" onclick="logout()" style="display:flex; align-items:center; gap:10px; padding:11px 18px; color:#c62828; font-size:0.88rem; font-weight:500; transition:all 0.2s; text-decoration:none;"
                           onmouseover="this.style.background='#ffebee'" onmouseout="this.style.background='transparent'">
                            <span>🚪</span> Logout
                        </a>
                    </div>
                </div>`;
        }

        if (user.role === 'ADMIN') {
            if (navDashboard) navDashboard.style.display = 'list-item';
        } else {
            if (navMyRentals) navMyRentals.style.display = 'list-item';
        }
    } else {
        if (navLogin)     navLogin.style.display    = 'list-item';
        if (navRegister)  navRegister.style.display = 'list-item';
        if (navLogout)    navLogout.style.display   = 'none';
        if (navDashboard) navDashboard.style.display= 'none';
        if (navMyRentals) navMyRentals.style.display= 'none';
        if (navUserInfo)  navUserInfo.style.display = 'none';
    }
}

function toggleProfileDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown) {
        dropdown.style.display = dropdown.style.display === 'none' ? 'block' : 'none';
    }
}

// Close dropdown when clicking outside
document.addEventListener('click', function(e) {
    const btn = document.getElementById('profileBtn');
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown && btn && !btn.contains(e.target) && !dropdown.contains(e.target)) {
        dropdown.style.display = 'none';
    }
});

/**
 * Toggle mobile menu
 */
function toggleMenu() {
    const navLinks = document.getElementById('navLinks');
    if (navLinks) navLinks.classList.toggle('open');
}

/**
 * Generic API call helper
 */
async function apiCall(url, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) options.body = JSON.stringify(body);

    const response = await fetch(API_BASE + url, options);
    return response.json();
}

// Run on every page load
document.addEventListener('DOMContentLoaded', updateNavbar);
