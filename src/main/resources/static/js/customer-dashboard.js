// Customer Dashboard JavaScript for UniEats

let allShops = [];
let filteredShops = [];
let currentView = 'shops';

// Initialize dashboard when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    initializeDashboard();
});

// Initialize dashboard
function initializeDashboard() {
    loadShops();
    setupEventListeners();
}

// Setup event listeners
function setupEventListeners() {
    // Search input with debounce
    const searchInput = document.getElementById('shopSearch');
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }
}

// Load all shops from API
async function loadShops() {
    try {
        showLoading(true);
        
        const response = await fetch('/api/customer/shops');
        if (response.ok) {
            allShops = await response.json();
            filteredShops = [...allShops];
            
            populateCityFilter();
            displayShops();
            updateShopCount();
        } else {
            showErrorMessage('Failed to load shops. Please try again.');
        }
    } catch (error) {
        console.error('Error loading shops:', error);
        showErrorMessage('Error loading shops. Please check your connection.');
    } finally {
        showLoading(false);
    }
}

// Populate city filter dropdown
function populateCityFilter() {
    const cityFilter = document.getElementById('cityFilter');
    if (!cityFilter) return;
    
    const cities = [...new Set(allShops.map(shop => shop.city).filter(city => city))];
    cities.sort();
    
    cityFilter.innerHTML = '<option value="">All Cities</option>';
    cities.forEach(city => {
        const option = document.createElement('option');
        option.value = city;
        option.textContent = city;
        cityFilter.appendChild(option);
    });
}

// Display shops in grid
function displayShops() {
    const shopsGrid = document.getElementById('shopsGrid');
    const noShopsMessage = document.getElementById('noShopsMessage');
    
    if (!shopsGrid) return;
    
    if (filteredShops.length === 0) {
        shopsGrid.innerHTML = '';
        if (noShopsMessage) {
            noShopsMessage.style.display = 'block';
        }
        return;
    }
    
    if (noShopsMessage) {
        noShopsMessage.style.display = 'none';
    }
    
    shopsGrid.innerHTML = filteredShops.map(shop => createShopCard(shop)).join('');
}

// Create shop card HTML
function createShopCard(shop) {
    const rating = shop.rating || 0;
    const ratingStars = generateStarRating(rating);
    
    return `
        <div class="col-md-6 col-lg-4">
            <div class="card h-100 shop-card" data-shop-id="${shop.id}">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start mb-3">
                        <h5 class="card-title">${escapeHtml(shop.shopName)}</h5>
                        <span class="badge bg-success">Open</span>
                    </div>
                    
                    <div class="mb-3">
                        <div class="d-flex align-items-center mb-2">
                            <i class="fas fa-map-marker-alt text-muted me-2"></i>
                            <small class="text-muted">${escapeHtml(shop.address || '')}</small>
                        </div>
                        <div class="d-flex align-items-center mb-2">
                            <i class="fas fa-city text-muted me-2"></i>
                            <small class="text-muted">${escapeHtml(shop.city || '')}</small>
                        </div>
                        <div class="d-flex align-items-center">
                            <div class="me-2">${ratingStars}</div>
                            <small class="text-muted">(${rating.toFixed(1)})</small>
                        </div>
                    </div>
                    
                    ${shop.description ? `<p class="card-text text-muted small">${escapeHtml(shop.description.substring(0, 100))}${shop.description.length > 100 ? '...' : ''}</p>` : ''}
                    
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <small class="text-muted">
                            <i class="fas fa-shopping-bag me-1"></i>
                            ${shop.totalOrders || 0} orders
                        </small>
                        <div>
                            <button class="btn btn-outline-primary btn-sm me-2" onclick="viewShopDetails(${shop.id})">
                                <i class="fas fa-info-circle me-1"></i>Details
                            </button>
                            <button class="btn btn-primary btn-sm" onclick="viewShopMenu(${shop.id})">
                                <i class="fas fa-utensils me-1"></i>Order
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    `;
}

// Generate star rating HTML
function generateStarRating(rating) {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    
    let stars = '';
    
    // Full stars
    for (let i = 0; i < fullStars; i++) {
        stars += '<i class="fas fa-star text-warning"></i>';
    }
    
    // Half star
    if (hasHalfStar) {
        stars += '<i class="fas fa-star-half-alt text-warning"></i>';
    }
    
    // Empty stars
    for (let i = 0; i < emptyStars; i++) {
        stars += '<i class="far fa-star text-muted"></i>';
    }
    
    return stars;
}

// Handle search input
function handleSearch(event) {
    const query = event.target.value.trim().toLowerCase();
    searchShops(query);
}

// Search shops by name
function searchShops(query = '') {
    if (!query) {
        filteredShops = [...allShops];
    } else {
        filteredShops = allShops.filter(shop => 
            shop.shopName.toLowerCase().includes(query) ||
            (shop.description && shop.description.toLowerCase().includes(query))
        );
    }
    
    displayShops();
    updateShopCount();
}

// Filter shops by city
function filterByCity() {
    const cityFilter = document.getElementById('cityFilter');
    const selectedCity = cityFilter ? cityFilter.value : '';
    
    if (!selectedCity) {
        filteredShops = [...allShops];
    } else {
        filteredShops = allShops.filter(shop => shop.city === selectedCity);
    }
    
    displayShops();
    updateShopCount();
}

// Sort shops
function sortShops() {
    const sortBy = document.getElementById('sortBy');
    const sortValue = sortBy ? sortBy.value : 'name';
    
    filteredShops.sort((a, b) => {
        switch (sortValue) {
            case 'rating':
                return (b.rating || 0) - (a.rating || 0);
            case 'orders':
                return (b.totalOrders || 0) - (a.totalOrders || 0);
            case 'name':
            default:
                return a.shopName.localeCompare(b.shopName);
        }
    });
    
    displayShops();
}

// Update shop count display
function updateShopCount() {
    const shopCount = document.getElementById('shopCount');
    if (shopCount) {
        shopCount.textContent = `${filteredShops.length} shop${filteredShops.length !== 1 ? 's' : ''}`;
    }
}

// Show/hide loading spinner
function showLoading(show) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const shopsGrid = document.getElementById('shopsGrid');
    
    if (loadingSpinner) {
        loadingSpinner.style.display = show ? 'block' : 'none';
    }
    
    if (shopsGrid) {
        shopsGrid.style.display = show ? 'none' : 'block';
    }
}

// View shop details
function viewShopDetails(shopId) {
    const shop = allShops.find(s => s.id === shopId);
    if (!shop) return;
    
    const modal = new bootstrap.Modal(document.getElementById('shopModal'));
    const modalTitle = document.getElementById('shopModalTitle');
    const modalBody = document.getElementById('shopModalBody');
    
    if (modalTitle) {
        modalTitle.textContent = shop.shopName;
    }
    
    if (modalBody) {
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-6">
                    <h6>Contact Information</h6>
                    <p><i class="fas fa-envelope me-2"></i>${escapeHtml(shop.email)}</p>
                    <p><i class="fas fa-phone me-2"></i>${escapeHtml(shop.phoneNumber || 'Not provided')}</p>
                    <p><i class="fas fa-map-marker-alt me-2"></i>${escapeHtml(shop.address)}</p>
                    <p><i class="fas fa-city me-2"></i>${escapeHtml(shop.city || 'Not specified')}</p>
                </div>
                <div class="col-md-6">
                    <h6>Shop Information</h6>
                    <p><strong>Rating:</strong> ${generateStarRating(shop.rating || 0)} (${(shop.rating || 0).toFixed(1)})</p>
                    <p><strong>Total Orders:</strong> ${shop.totalOrders || 0}</p>
                    <p><strong>Status:</strong> <span class="badge bg-success">Approved</span></p>
                    ${shop.description ? `<p><strong>Description:</strong><br>${escapeHtml(shop.description)}</p>` : ''}
                </div>
            </div>
        `;
    }
    
    modal.show();
}

// View shop menu (placeholder)
function viewShopMenu(shopId) {
    // This would typically redirect to a menu page or show a menu modal
    showInfoMessage('Menu viewing functionality will be implemented soon!');
    console.log('View menu for shop:', shopId);
}

// Show shops view
function showShops() {
    currentView = 'shops';
    document.getElementById('ordersSection').style.display = 'none';
    // Shops are already visible by default
}

// Show orders view
function showOrders() {
    currentView = 'orders';
    document.getElementById('ordersSection').style.display = 'block';
    showInfoMessage('Order history functionality will be implemented soon!');
}

// Utility function to escape HTML
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Debounce function
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}
