import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import toast from 'react-hot-toast';

export default function RegisterPage() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', phone: '' });
  const [loading, setLoading] = useState(false);
  const { register, user } = useAuth();
  const navigate = useNavigate();

  if (user) { navigate('/'); return null; }

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await register({ ...form, role: 'CUSTOMER' });
      toast.success('Account created!');
      navigate('/');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card glass-card">
        <h1>Create Account</h1>
        <p>Join FastPOS to get started</p>

        <form className="auth-form" onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="input-group">
              <label htmlFor="reg-first">First Name</label>
              <input id="reg-first" className="input" name="firstName" placeholder="John"
                value={form.firstName} onChange={handleChange} required />
            </div>
            <div className="input-group">
              <label htmlFor="reg-last">Last Name</label>
              <input id="reg-last" className="input" name="lastName" placeholder="Doe"
                value={form.lastName} onChange={handleChange} required />
            </div>
          </div>
          <div className="input-group">
            <label htmlFor="reg-email">Email</label>
            <input id="reg-email" className="input" type="email" name="email" placeholder="john@example.com"
              value={form.email} onChange={handleChange} required />
          </div>
          <div className="input-group">
            <label htmlFor="reg-phone">Phone</label>
            <input id="reg-phone" className="input" name="phone" placeholder="555-0100"
              value={form.phone} onChange={handleChange} />
          </div>
          <div className="input-group">
            <label htmlFor="reg-password">Password</label>
            <input id="reg-password" className="input" type="password" name="password" placeholder="Min 6 characters"
              value={form.password} onChange={handleChange} required minLength={6} />
          </div>
          <button id="register-submit" className="btn btn-primary btn-lg" type="submit" disabled={loading}>
            {loading ? 'Creating...' : 'Create Account'}
          </button>
        </form>

        <div className="auth-footer">
          Already have an account? <Link to="/login">Sign In</Link>
        </div>
      </div>
    </div>
  );
}
