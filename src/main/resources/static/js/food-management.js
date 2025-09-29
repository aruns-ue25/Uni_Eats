// Food Management JavaScript
let foods = [];
let editingFoodId = null;

// Load foods when page loads
document.addEventListener('DOMContentLoaded', function() {
    loadFoods();
});

// Load foods from API
async function loadFoods() {
    try {
        const response = await fetch('/api/shop/foods');
        if (response.ok) {
            foods = await response.json();
            renderFoodTable();
        } else {
            showAlert('Error loading foods', 'danger');
        }
    } catch (error) {
        console.error('Error loading foods:', error);
        showAlert('Error loading foods', 'danger');
    }
}

// Render food table
function renderFoodTable() {
    const tbody = document.getElementById('foodTableBody');
    tbody.innerHTML = '';
    
    foods.forEach(food => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>
                <img src="${food.imageUrl || '/images/placeholder-food.jpg'}" 
                     alt="${food.name}" 
                     class="img-thumbnail" 
                     style="width: 50px; height: 50px; object-fit: cover;">
            </td>
            <td>${food.name}</td>
            <td><span class="badge bg-secondary">${food.category || 'N/A'}</span></td>
            <td>
                ${food.hasDiscount ? 
                    `<span class="text-decoration-line-through">$${food.originalPrice}</span><br>
                     <span class="text-success fw-bold">$${food.discountedPrice}</span>
                     <span class="badge bg-danger ms-1">-${food.discountPercentage}%</span>` :
                    `$${food.price}`
                }
            </td>
            <td>
                <span class="badge ${food.isAvailable ? 'bg-success' : 'bg-danger'}">
                    ${food.isAvailable ? 'Available' : 'Unavailable'}
                </span>
            </td>
            <td>
                <span class="badge ${food.isFeatured ? 'bg-warning' : 'bg-secondary'}">
                    ${food.isFeatured ? 'Featured' : 'Regular'}
                </span>
            </td>
            <td>
                ${food.hasDiscount ? 
                    `<span class="text-success">${food.discountPercentage}% off</span>` :
                    '<span class="text-muted">No discount</span>'
                }
            </td>
            <td>
                <div class="btn-group" role="group">
                    <button class="btn btn-sm btn-outline-primary" onclick="editFood(${food.id})" title="Edit">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-${food.isAvailable ? 'warning' : 'success'}" 
                            onclick="toggleAvailability(${food.id})" 
                            title="${food.isAvailable ? 'Make Unavailable' : 'Make Available'}">
                        <i class="fas fa-${food.isAvailable ? 'eye-slash' : 'eye'}"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-${food.isFeatured ? 'secondary' : 'warning'}" 
                            onclick="toggleFeatured(${food.id})" 
                            title="${food.isFeatured ? 'Remove from Featured' : 'Add to Featured'}">
                        <i class="fas fa-star"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="deleteFood(${food.id})" title="Delete">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(row);
    });
}

// Save food (create or update)
async function saveFood() {
    const form = document.getElementById('foodForm');
    const formData = new FormData(form);
    
    const foodData = {
        name: document.getElementById('foodName').value,
        description: document.getElementById('foodDescription').value,
        price: parseFloat(document.getElementById('foodPrice').value),
        category: document.getElementById('foodCategory').value,
        imageUrl: document.getElementById('foodImageUrl').value,
        isAvailable: document.getElementById('foodAvailable').checked,
        isFeatured: document.getElementById('foodFeatured').checked,
        calories: parseInt(document.getElementById('foodCalories').value) || null,
        preparationTime: parseInt(document.getElementById('foodPrepTime').value) || null,
        ingredients: document.getElementById('foodIngredients').value,
        allergens: document.getElementById('foodAllergens').value
    };
    
    try {
        let response;
        if (editingFoodId) {
            // Update existing food
            foodData.id = editingFoodId;
            response = await fetch(`/api/shop/foods/${editingFoodId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(foodData)
            });
        } else {
            // Create new food
            response = await fetch('/api/shop/foods', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(foodData)
            });
        }
        
        if (response.ok) {
            showAlert(editingFoodId ? 'Food updated successfully!' : 'Food created successfully!', 'success');
            closeModal();
            loadFoods();
        } else {
            showAlert('Error saving food', 'danger');
        }
    } catch (error) {
        console.error('Error saving food:', error);
        showAlert('Error saving food', 'danger');
    }
}

// Edit food
function editFood(foodId) {
    const food = foods.find(f => f.id === foodId);
    if (!food) return;
    
    editingFoodId = foodId;
    
    // Populate form
    document.getElementById('foodName').value = food.name;
    document.getElementById('foodDescription').value = food.description || '';
    document.getElementById('foodPrice').value = food.price;
    document.getElementById('foodCategory').value = food.category || '';
    document.getElementById('foodImageUrl').value = food.imageUrl || '';
    document.getElementById('foodAvailable').checked = food.isAvailable;
    document.getElementById('foodFeatured').checked = food.isFeatured;
    document.getElementById('foodCalories').value = food.calories || '';
    document.getElementById('foodPrepTime').value = food.preparationTime || '';
    document.getElementById('foodIngredients').value = food.ingredients || '';
    document.getElementById('foodAllergens').value = food.allergens || '';
    
    // Update modal title
    document.querySelector('#addFoodModal .modal-title').textContent = 'Edit Food';
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('addFoodModal'));
    modal.show();
}

// Toggle food availability
async function toggleAvailability(foodId) {
    try {
        const response = await fetch(`/api/shop/foods/${foodId}/toggle-availability`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showAlert('Food availability updated!', 'success');
            loadFoods();
        } else {
            showAlert('Error updating availability', 'danger');
        }
    } catch (error) {
        console.error('Error toggling availability:', error);
        showAlert('Error updating availability', 'danger');
    }
}

// Toggle food featured status
async function toggleFeatured(foodId) {
    try {
        const response = await fetch(`/api/shop/foods/${foodId}/toggle-featured`, {
            method: 'POST'
        });
        
        if (response.ok) {
            showAlert('Food featured status updated!', 'success');
            loadFoods();
        } else {
            showAlert('Error updating featured status', 'danger');
        }
    } catch (error) {
        console.error('Error toggling featured:', error);
        showAlert('Error updating featured status', 'danger');
    }
}

// Delete food
async function deleteFood(foodId) {
    if (!confirm('Are you sure you want to delete this food item?')) {
        return;
    }
    
    try {
        const response = await fetch(`/api/shop/foods/${foodId}`, {
            method: 'DELETE'
        });
        
        if (response.ok) {
            showAlert('Food deleted successfully!', 'success');
            loadFoods();
        } else {
            showAlert('Error deleting food', 'danger');
        }
    } catch (error) {
        console.error('Error deleting food:', error);
        showAlert('Error deleting food', 'danger');
    }
}

// Close modal and reset form
function closeModal() {
    editingFoodId = null;
    document.getElementById('foodForm').reset();
    document.querySelector('#addFoodModal .modal-title').textContent = 'Add New Food';
    const modal = bootstrap.Modal.getInstance(document.getElementById('addFoodModal'));
    modal.hide();
}

// Show alert
function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    alertDiv.style.top = '20px';
    alertDiv.style.right = '20px';
    alertDiv.style.zIndex = '9999';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(alertDiv);
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.parentNode.removeChild(alertDiv);
        }
    }, 5000);
}





