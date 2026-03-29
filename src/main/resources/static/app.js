// Configuração da API
const API_BASE_URL = window.location.origin + '/api/products';
const MAX_RETRIES = 3;
const RETRY_DELAY = 1000;

// Estado da aplicação
let currentEditId = null;
let deleteProductId = null;

// Inicialização
document.addEventListener('DOMContentLoaded', () => {
    loadProducts();
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    document.getElementById('productForm').addEventListener('submit', handleSubmit);
    document.getElementById('cancelBtn').addEventListener('click', cancelEdit);
    document.getElementById('searchBtn').addEventListener('click', handleSearch);
    document.getElementById('lowStockBtn').addEventListener('click', loadLowStockProducts);
    document.getElementById('showAllBtn').addEventListener('click', loadProducts);
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);
    document.getElementById('cancelDeleteBtn').addEventListener('click', closeModal);
    
    // Validação em tempo real
    const inputs = document.querySelectorAll('#productForm input, #productForm textarea');
    inputs.forEach(input => {
        input.addEventListener('blur', () => validateField(input));
        input.addEventListener('input', () => clearFieldError(input));
    });
}

// Validação de Campos
function validateField(field) {
    const value = field.value.trim();
    const fieldName = field.name;
    let isValid = true;
    let errorMessage = '';

    // Limpa erro anterior
    clearFieldError(field);

    switch(fieldName) {
        case 'name':
            if (value.length < 3 || value.length > 100) {
                errorMessage = 'Nome deve ter entre 3 e 100 caracteres';
                isValid = false;
            }
            break;
        
        case 'description':
            if (value.length < 10 || value.length > 500) {
                errorMessage = 'Descrição deve ter entre 10 e 500 caracteres';
                isValid = false;
            }
            break;
        
        case 'price':
            const price = parseFloat(value);
            if (isNaN(price) || price <= 0 || price > 999999.99) {
                errorMessage = 'Preço deve ser entre 0.01 e 999999.99';
                isValid = false;
            }
            break;
        
        case 'quantity':
            const quantity = parseInt(value);
            if (isNaN(quantity) || quantity < 0 || quantity > 999999) {
                errorMessage = 'Quantidade deve ser entre 0 e 999999';
                isValid = false;
            }
            break;
        
        case 'category':
            if (value.length < 3 || value.length > 50) {
                errorMessage = 'Categoria deve ter entre 3 e 50 caracteres';
                isValid = false;
            }
            break;
    }

    if (!isValid) {
        showFieldError(field, errorMessage);
    }

    return isValid;
}

function showFieldError(field, message) {
    field.classList.add('invalid');
    const errorElement = document.getElementById(`${field.name}Error`);
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.add('show');
    }
}

function clearFieldError(field) {
    field.classList.remove('invalid');
    const errorElement = document.getElementById(`${field.name}Error`);
    if (errorElement) {
        errorElement.textContent = '';
        errorElement.classList.remove('show');
    }
}

function validateForm() {
    const inputs = document.querySelectorAll('#productForm input:not([type="hidden"]), #productForm textarea');
    let isValid = true;

    inputs.forEach(input => {
        if (!validateField(input)) {
            isValid = false;
        }
    });

    return isValid;
}

// Manipulação do Formulário
async function handleSubmit(e) {
    e.preventDefault();
    
    if (!validateForm()) {
        showAlert('Por favor, corrija os erros no formulário', 'error');
        return;
    }

    const formData = {
        name: document.getElementById('name').value.trim(),
        description: document.getElementById('description').value.trim(),
        price: parseFloat(document.getElementById('price').value),
        quantity: parseInt(document.getElementById('quantity').value),
        category: document.getElementById('category').value.trim()
    };

    const submitBtn = document.getElementById('submitBtn');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner"></span> Salvando...';

    try {
        if (currentEditId) {
            await updateProduct(currentEditId, formData);
            showAlert('Produto atualizado com sucesso!', 'success');
        } else {
            await createProduct(formData);
            showAlert('Produto criado com sucesso!', 'success');
        }
        
        resetForm();
        await loadProducts();
    } catch (error) {
        handleError(error);
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = currentEditId ? 'Atualizar produto' : 'Adicionar produto';
    }
}

function resetForm() {
    document.getElementById('productForm').reset();
    document.getElementById('productId').value = '';
    document.getElementById('formTitle').textContent = 'Novo produto';
    document.getElementById('submitBtn').textContent = 'Adicionar produto';
    document.getElementById('cancelBtn').style.display = 'none';
    currentEditId = null;
    
    // Limpa todos os erros
    const inputs = document.querySelectorAll('#productForm input, #productForm textarea');
    inputs.forEach(input => clearFieldError(input));
}

function cancelEdit() {
    resetForm();
}

// Operações CRUD com Retry Logic
async function fetchWithRetry(url, options = {}, retries = MAX_RETRIES) {
    for (let i = 0; i < retries; i++) {
        try {
            const response = await fetch(url, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                timeout: 10000
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => null);
                const error = new Error(errorData?.message || 'Erro na requisição');
                error.status = response.status;
                error.data = errorData;
                throw error;
            }

            // Se não há conteúdo (204), retorna null
            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            // Se é o último retry ou erro não é de rede, lança erro
            if (i === retries - 1 || error.status < 500) {
                throw error;
            }
            
            // Aguarda antes de tentar novamente
            await new Promise(resolve => setTimeout(resolve, RETRY_DELAY * (i + 1)));
        }
    }
}

async function createProduct(product) {
    return await fetchWithRetry(API_BASE_URL, {
        method: 'POST',
        body: JSON.stringify(product)
    });
}

async function loadProducts() {
    try {
        const products = await fetchWithRetry(API_BASE_URL);
        displayProducts(products);
    } catch (error) {
        handleError(error);
        displayProducts([]);
    }
}

async function updateProduct(id, product) {
    return await fetchWithRetry(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        body: JSON.stringify(product)
    });
}

async function deleteProduct(id) {
    return await fetchWithRetry(`${API_BASE_URL}/${id}`, {
        method: 'DELETE'
    });
}

async function handleSearch() {
    const searchTerm = document.getElementById('searchInput').value.trim();
    
    if (!searchTerm) {
        showAlert('Digite um termo para buscar', 'warning');
        return;
    }

    try {
        const products = await fetchWithRetry(`${API_BASE_URL}/search?name=${encodeURIComponent(searchTerm)}`);
        displayProducts(products);
        
        if (products.length === 0) {
            showAlert('Nenhum produto encontrado', 'info');
        }
    } catch (error) {
        handleError(error);
    }
}

async function loadLowStockProducts() {
    try {
        const products = await fetchWithRetry(`${API_BASE_URL}/low-stock`);
        displayProducts(products);
        
        if (products.length === 0) {
            showAlert('Não há produtos com estoque baixo', 'success');
        } else {
            showAlert(`${products.length} produto(s) com estoque baixo`, 'warning');
        }
    } catch (error) {
        handleError(error);
    }
}

// Exibição de Produtos
function displayProducts(products) {
    const tbody = document.getElementById('productsTableBody');
    const emptyState = document.getElementById('emptyState');
    const table = document.getElementById('productsTable');
    const productCount = document.getElementById('productCount');

    tbody.innerHTML = '';

    // Update stats cards if present
    const statTotal = document.getElementById('statTotal');
    const statLowStock = document.getElementById('statLowStock');
    const statCategories = document.getElementById('statCategories');
    if (products && statTotal) {
        statTotal.textContent = products.length;
        const lowCount = products.filter(p => p.quantity < 10).length;
        statLowStock.textContent = lowCount;
        const cats = new Set(products.map(p => p.category));
        statCategories.textContent = cats.size;
    } else if (statTotal) {
        statTotal.textContent = '0';
        statLowStock.textContent = '0';
        statCategories.textContent = '0';
    }

    if (!products || products.length === 0) {
        table.style.display = 'none';
        emptyState.style.display = 'block';
        productCount.textContent = '0 produtos';
        return;
    }

    table.style.display = 'table';
    emptyState.style.display = 'none';
    productCount.textContent = `${products.length} produto${products.length > 1 ? 's' : ''}`;

    products.forEach(product => {
        const row = tbody.insertRow();
        const isLowStock = product.quantity < 10;

        row.innerHTML = `
            <td>${product.id}</td>
            <td><strong>${escapeHtml(product.name)}</strong></td>
            <td>${escapeHtml(product.category)}</td>
            <td>${escapeHtml(truncateText(product.description, 50))}</td>
            <td>R$ ${formatPrice(product.price)}</td>
            <td class="${isLowStock ? 'low-stock' : ''}">
                ${product.quantity} ${isLowStock ? '⚠️' : ''}
            </td>
            <td>
                <div class="action-buttons">
                    <button class="btn btn-sm btn-warning" onclick="editProduct(${product.id})">
                        ✏️ Editar
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="showDeleteConfirmation(${product.id}, '${escapeHtml(product.name)}')">
                        🗑️ Excluir
                    </button>
                </div>
            </td>
        `;
    });
}

// Edição e Exclusão
async function editProduct(id) {
    try {
        const product = await fetchWithRetry(`${API_BASE_URL}/${id}`);
        
        document.getElementById('productId').value = product.id;
        document.getElementById('name').value = product.name;
        document.getElementById('description').value = product.description;
        document.getElementById('price').value = product.price;
        document.getElementById('quantity').value = product.quantity;
        document.getElementById('category').value = product.category;
        
        document.getElementById('formTitle').textContent = 'Editar produto';
        document.getElementById('submitBtn').textContent = 'Atualizar produto';
        document.getElementById('cancelBtn').style.display = 'inline-flex';
        
        currentEditId = id;
        
        // Scroll para o formulário
        document.querySelector('.form-section').scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
        handleError(error);
    }
}

function showDeleteConfirmation(id, name) {
    deleteProductId = id;
    document.getElementById('confirmMessage').textContent = 
        `Tem certeza que deseja excluir o produto "${name}"?`;
    const modal = document.getElementById('confirmModal');
    modal.classList.add('show');
}

function closeModal() {
    document.getElementById('confirmModal').classList.remove('show');
    deleteProductId = null;
}

async function confirmDelete() {
    if (!deleteProductId) return;

    const confirmBtn = document.getElementById('confirmDeleteBtn');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner"></span> Excluindo...';

    try {
        await deleteProduct(deleteProductId);
        showAlert('Produto excluído com sucesso!', 'success');
        await loadProducts();
        closeModal();
    } catch (error) {
        handleError(error);
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = 'Sim, Excluir';
    }
}

// Alertas
function showAlert(message, type = 'info') {
    const container = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    
    const icon = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ'
    }[type] || 'ℹ';
    
    alert.innerHTML = `<strong>${icon}</strong><span>${escapeHtml(message)}</span>`;
    
    container.appendChild(alert);
    
    // Remove após 5 segundos
    setTimeout(() => {
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 300);
    }, 5000);
}

// Tratamento de Erros
function handleError(error) {
    console.error('Erro:', error);
    
    let message = 'Ocorreu um erro inesperado. Tente novamente.';
    
    if (error.message === 'Failed to fetch' || error.name === 'TypeError') {
        message = 'Erro de conexão. Verifique se o servidor está rodando.';
    } else if (error.status === 404) {
        message = 'Recurso não encontrado.';
    } else if (error.status === 422 && error.data) {
        message = error.data.message || 'Erro de validação.';
    } else if (error.status === 400 && error.data) {
        if (error.data.details && error.data.details.length > 0) {
            message = error.data.details.join('; ');
        } else {
            message = error.data.message || 'Dados inválidos.';
        }
    } else if (error.data && error.data.message) {
        message = error.data.message;
    }
    
    showAlert(message, 'error');
}

// Utilitários
function formatPrice(price) {
    return parseFloat(price).toFixed(2).replace('.', ',');
}

function truncateText(text, maxLength) {
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Tratamento de erros globais
window.addEventListener('unhandledrejection', event => {
    console.error('Erro não tratado:', event.reason);
    showAlert('Erro inesperado na aplicação. Por favor, recarregue a página.', 'error');
});

// Detecção de conexão offline
window.addEventListener('offline', () => {
    showAlert('Você está offline. Algumas funcionalidades podem não funcionar.', 'warning');
});

window.addEventListener('online', () => {
    showAlert('Conexão restaurada!', 'success');
    loadProducts();
});
