// ===== HOME PAGE JS =====

document.addEventListener('DOMContentLoaded', () => {
    loadHomeStats();
});

/**
 * Load live stats from backend for home page
 */
async function loadHomeStats() {
    try {
        const result = await apiCall('/admin/dashboard');
        if (result.success && result.data) {
            const data = result.data;
            const statEquipment = document.getElementById('statEquipment');
            const statUsers     = document.getElementById('statUsers');
            if (statEquipment) statEquipment.textContent = data.availableEquipment + '+';
            if (statUsers)     statUsers.textContent     = data.totalUsers + '+';
        }
    } catch (e) {
        // Silently fail — static fallback values already in HTML
    }
}
