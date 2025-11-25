const API_PREFIX = (document.querySelector('meta[name="api-prefix"]')?.content) || '/api/v1';

function getFullApi(path) {
    // path should not include the /products prefix if you pass it explicitly
    if (path.startsWith('/')) return API_PREFIX + path;
    return API_PREFIX + '/' + path;
}

function showMessage(type, text, timeout = 4000) {
    const el = document.getElementById('status') || createGlobalStatus();
    el.textContent = text;
    el.className = 'status show ' + (type === 'success' ? 'success' : 'error');
    if (timeout) {
        clearTimeout(el._t);
        el._t = setTimeout(() => { el.className = 'status'; }, timeout);
    }
}

function createGlobalStatus() {
    const el = document.createElement('div');
    el.id = 'status';
    el.className = 'status';
    document.body.appendChild(el);
    return el;
}

function getIdFromPathOrSearch() {
    // Try search param ?id=, otherwise try last path segment if numeric
    const params = new URLSearchParams(window.location.search);
    if (params.get('id')) return params.get('id');
    const parts = window.location.pathname.split('/').filter(Boolean);
    const last = parts[parts.length - 1];
    if (last && !isNaN(parseInt(last))) return last;
    return null;
}

async function fetchProduct(id) {
    const url = getFullApi(`/products/${id}`);
    const res = await fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
    if (!res.ok) {
        if (res.status === 404) return null;
        throw new Error('Fetch failed: ' + res.status);
    }
    return await res.json();
}

async function updateProduct(id, payload) {
    const url = getFullApi(`/products/update/${id}`);
    const res = await fetch(url, {
        method: 'POST', // controller uses POST for update
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (res.ok) return true;
    // try to parse body for message
    let text;
    try { const body = await res.json(); text = body?.message || JSON.stringify(body); } catch { text = await res.text(); }
    showMessage('error', 'Update failed: ' + (text || res.status));
    return false;
}

async function addProduct(payload) {
    const url = getFullApi('/products');
    // controller uses POST to /products (apiService.addProduct expects external, but controller maps add to /add -> adjust call site)
    const res = await fetch(getFullApi('/products/add'), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload)
    });
    if (!res.ok) {
        let text;
        try { const body = await res.json(); text = body?.message || JSON.stringify(body); } catch { text = await res.text(); }
        throw new Error(text || ('Status ' + res.status));
    }
    return await res.json();
}

async function deleteProduct(id) {
    const url = getFullApi(`/products/${id}`);
    // Use exchange-style endpoint that returns status in controller; controller currently calls service.deleteProductById (no return),
    // but API may return 200 if successful. We'll call DELETE and check status.
    const res = await fetch(url, { method: 'DELETE' });
    return res.ok;
}

async function fetchAllProducts(offset = 0, limit = 12) {
    const url = getFullApi(`/products/all?offset=${encodeURIComponent(offset)}&limit=${encodeURIComponent(limit)}`);
    const res = await fetch(url, { method: 'GET', headers: { 'Accept': 'application/json' } });
    if (!res.ok) throw new Error('Fetch failed: ' + res.status);
    const body = await res.json();
    // If controller wraps response in ApiResponse { message, data }, try to extract data
    return body?.data ?? body;
}

// Utility to render a product card (used in product listing pages)
function productCardHTML(product) {
    return `
    <article class="product-card" id=${product.id}>
        <img src="${product.image || ''}" alt="${escapeHtml(product.title || '')}" style="max-width: 100%; height: 80%;" />
        <div class="product-info">
            <h3>${escapeHtml(product.title || '')}</h3>
            <p class="muted">${escapeHtml(product.category || '')}</p>
            <div class="price">$${(product.price ?? '').toFixed ? product.price.toFixed(2) : product.price}</div>
            <div class="actions">
                <a class="btn" href="/products/detail/${product.id}">View</a>
                <a class="btn" href="/products/update/${product.id}">Edit</a>
                <button class="btn ghost" onclick="handleDelete(${product.id}, this)">Delete</button>
            </div>
        </div>
    </article>`;
}

function escapeHtml(str) {
    return String(str).replace(/[&<>"'`=\/]/g, function (s) {
        return ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;', '/': '&#x2F;', '`': '&#x60;', '=': '&#x3D;'
        })[s];
    });
}

async function handleDelete(id, btn) {
    if (!confirm('Delete product #' + id + '?')) return;
    btn.disabled = true;
    const ok = await deleteProduct(id);
    if (ok) {
        showMessage('success', 'Product deleted');
        // remove card if possible or refresh
        const card = document.getElementById(id);
        if (card) card.remove();
        else setTimeout(() => location.reload(), 300);
    } else {
        showMessage('error', 'Failed to delete product');
        btn.disabled = false;
    }
}

// expose some functions globally so templates can call them directly
window.getIdFromPathOrSearch = getIdFromPathOrSearch;
window.fetchProduct = fetchProduct;
window.updateProduct = updateProduct;
window.addProduct = addProduct;
window.fetchAllProducts = fetchAllProducts;
window.deleteProduct = deleteProduct;
window.showMessage = showMessage;
window.handleDelete = handleDelete;