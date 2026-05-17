import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login, user } = useAuth();
  const navigate = useNavigate();

  if (user) { navigate('/'); return null; }

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await login(email, password);
      toast.success('Welcome back!');
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card glass-card">
        <h1>Welcome Back</h1>
        <p>Sign in to your FastPOS account</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div className="input-group">
            <label htmlFor="login-email">Email</label>
            <input id="login-email" className="input" type="email" placeholder="admin@fastpos.com"
              value={email} onChange={(e) => setEmail(e.target.value)} required />
          </div>
          <div className="input-group">
            <label htmlFor="login-password">Password</label>
            <input id="login-password" className="input" type="password" placeholder="••••••"
              value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button id="login-submit" className="btn btn-primary btn-lg" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer">
          Don't have an account? <Link to="/register">Register</Link>
        </div>

        <div style={{ marginTop: '20px', padding: '12px', borderRadius: '8px', background: 'rgba(99,102,241,0.1)', fontSize: '12px', color: 'var(--text-muted)' }}>
          <strong>Demo Accounts:</strong><br/>
          Admin: admin@fastpos.com / admin123<br/>
          Cashier: cashier@fastpos.com / cashier123<br/>
          Customer: customer@fastpos.com / customer123
        </div>
      </div>
    </div>
  );
}
