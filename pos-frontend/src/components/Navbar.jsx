import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const links = [
    { path: '/', label: 'POS', icon: '🛒' },
    { path: '/orders', label: 'Orders', icon: '📦' },
  ];

  if (user?.role === 'ADMIN' || user?.role === 'CASHIER') {
    links.push({ path: '/analytics', label: 'Analytics', icon: '📊' });
  }

  return (
    <nav className="navbar" id="main-navbar">
      <div className="navbar-brand">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <path d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
        FastPOS
      </div>

      <div className="navbar-links">
        {links.map((link) => (
          <button
            key={link.path}
            className={`nav-link ${location.pathname === link.path ? 'active' : ''}`}
            onClick={() => navigate(link.path)}
          >
            {link.icon} {link.label}
          </button>
        ))}
      </div>

      <div className="navbar-user">
        {cart.totalItems > 0 && (
          <span className="cart-badge">{cart.totalItems}</span>
        )}
        <div className="user-avatar">
          {user?.firstName?.[0]}{user?.lastName?.[0]}
        </div>
        <span style={{ fontSize: '14px', color: 'var(--text-secondary)' }}>
          {user?.firstName}
        </span>
        <button className="btn btn-ghost btn-sm" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}
