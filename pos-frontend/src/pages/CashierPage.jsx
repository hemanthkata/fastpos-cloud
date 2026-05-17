import { useState, useEffect } from 'react';
import api from '../api/axiosClient';
import { useCart } from '../context/CartContext';
import toast from 'react-hot-toast';

export default function CashierPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [activeCategory, setActiveCategory] = useState(null);
  const [search, setSearch] = useState('');
  const [showCheckout, setShowCheckout] = useState(false);
  const { cart, addToCart, updateQuantity, removeFromCart, clearCart, fetchCart } = useCart();

  useEffect(() => {
    fetchCart();
    loadCategories();
    loadProducts();
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => loadProducts(), 300);
    return () => clearTimeout(timer);
  }, [search, activeCategory]);

  const loadCategories = async () => {
    try {
      const res = await api.get('/categories');
      const data = res.data;
      setCategories(Array.isArray(data) ? data : (data.content || []));
    } catch (err) { console.error('Failed to load categories:', err); }
  };

  const loadProducts = async () => {
    try {
      const params = { size: 50 };
      if (search) params.search = search;
      if (activeCategory) params.categoryId = activeCategory;
      const res = await api.get('/products', { params });
      const data = res.data;
      setProducts(Array.isArray(data) ? data : (data.content || []));
    } catch (err) { console.error('Failed to load products:', err); }
  };

  const handleCheckout = async (method) => {
    try {
      const res = await api.post('/orders', { notes: '' });
      const order = res.data;

      if (method === 'cash') {
        await api.post('/payments/cash', { orderId: order.id });
        toast.success(`Order ${order.orderNumber} paid! 🎉`);
      } else {
        toast.success(`Order ${order.orderNumber} created!`);
      }

      setShowCheckout(false);
      await fetchCart();
      loadProducts();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Checkout failed');
    }
  };

  return (
    <div className="cashier-page">
      {/* Product Section */}
      <div className="product-section">
        <div className="product-header">
          <h2>Products</h2>
          <div className="search-bar">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8" /><path d="m21 21-4.3-4.3" />
            </svg>
            <input id="product-search" className="input" placeholder="Search products..."
              value={search} onChange={(e) => setSearch(e.target.value)} />
          </div>
        </div>

        <div className="category-tabs">
          <button className={`category-tab ${!activeCategory ? 'active' : ''}`}
            onClick={() => setActiveCategory(null)}>All</button>
          {categories.map((cat) => (
            <button key={cat.id} className={`category-tab ${activeCategory === cat.id ? 'active' : ''}`}
              onClick={() => setActiveCategory(cat.id)}>{cat.name}</button>
          ))}
        </div>

        <div className="product-grid">
          {products.map((product) => (
            <div key={product.id} className="product-card" onClick={() => addToCart(product.id)}>
              <img className="product-card-img" src={product.imageUrl || 'https://via.placeholder.com/300x140'}
                alt={product.name} loading="lazy" />
              <div className="product-card-body">
                <h3>{product.name}</h3>
                <div className="sku">{product.sku}</div>
                <div className="product-card-footer">
                  <span className="product-price">${product.price}</span>
                  <span className="product-stock">{product.stockQuantity} left</span>
                </div>
              </div>
            </div>
          ))}
          {products.length === 0 && (
            <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: '60px', color: 'var(--text-muted)' }}>
              No products found
            </div>
          )}
        </div>
      </div>

      {/* Cart Panel */}
      <div className="cart-panel">
        <div className="cart-header">
          <h2>🛒 Cart</h2>
          {cart.totalItems > 0 && <span className="cart-badge">{cart.totalItems}</span>}
        </div>

        <div className="cart-items">
          {cart.items.length === 0 ? (
            <div className="cart-empty">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                <circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/>
                <path d="M1 1h4l2.68 13.39a2 2 0 002 1.61h9.72a2 2 0 002-1.61L23 6H6"/>
              </svg>
              <p>Your cart is empty</p>
              <p style={{ fontSize: '13px' }}>Click a product to add it</p>
            </div>
          ) : (
            cart.items.map((item) => (
              <div key={item.productId} className="cart-item">
                <img className="cart-item-img" src={item.imageUrl || 'https://via.placeholder.com/48'}
                  alt={item.productName} />
                <div className="cart-item-info">
                  <h4>{item.productName}</h4>
                  <span className="price">${item.unitPrice}</span>
                </div>
                <div className="cart-item-qty">
                  <button onClick={(e) => { e.stopPropagation(); updateQuantity(item.productId, item.quantity - 1); }}>−</button>
                  <span>{item.quantity}</span>
                  <button onClick={(e) => { e.stopPropagation(); updateQuantity(item.productId, item.quantity + 1); }}>+</button>
                </div>
                <button className="cart-item-remove" onClick={() => removeFromCart(item.productId)}>✕</button>
              </div>
            ))
          )}
        </div>

        {cart.items.length > 0 && (
          <div className="cart-footer">
            <div className="cart-totals">
              <div className="row"><span>Subtotal</span><span>${cart.subtotal?.toFixed(2)}</span></div>
              <div className="row"><span>Tax (8%)</span><span>${cart.taxAmount?.toFixed(2)}</span></div>
              <div className="row total"><span>Total</span><span className="amount">${cart.totalAmount?.toFixed(2)}</span></div>
            </div>
            <button id="checkout-btn" className="btn btn-success checkout-btn" onClick={() => setShowCheckout(true)}>
              💳 Checkout — ${cart.totalAmount?.toFixed(2)}
            </button>
          </div>
        )}
      </div>

      {/* Checkout Modal */}
      {showCheckout && (
        <div className="modal-overlay" onClick={() => setShowCheckout(false)}>
          <div className="modal glass-card" onClick={(e) => e.stopPropagation()}>
            <h2>Complete Payment</h2>
            <div className="cart-totals" style={{ marginBottom: '24px' }}>
              <div className="row"><span>Items</span><span>{cart.totalItems}</span></div>
              <div className="row"><span>Subtotal</span><span>${cart.subtotal?.toFixed(2)}</span></div>
              <div className="row"><span>Tax</span><span>${cart.taxAmount?.toFixed(2)}</span></div>
              <div className="row total"><span>Total Due</span><span className="amount">${cart.totalAmount?.toFixed(2)}</span></div>
            </div>
            <div className="modal-actions">
              <button className="btn btn-success btn-lg" onClick={() => handleCheckout('cash')}>
                💵 Cash Payment
              </button>
              <button className="btn btn-primary btn-lg" onClick={() => handleCheckout('card')}>
                💳 Card Payment
              </button>
            </div>
            <button className="btn btn-ghost" style={{ width: '100%', marginTop: '12px' }}
              onClick={() => setShowCheckout(false)}>Cancel</button>
          </div>
        </div>
      )}
    </div>
  );
}
