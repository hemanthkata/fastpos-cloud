import { useState, useEffect } from 'react';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import api from '../api/axiosClient';

export default function AnalyticsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [preset, setPreset] = useState('30d');
  const [dateRange, setDateRange] = useState({ from: '', to: '' });

  useEffect(() => { applyPreset('30d'); }, []);

  const applyPreset = (p) => {
    setPreset(p);
    const to = new Date();
    const from = new Date();
    if (p === '7d') from.setDate(to.getDate() - 7);
    else if (p === '30d') from.setDate(to.getDate() - 30);
    else if (p === '90d') from.setDate(to.getDate() - 90);
    else if (p === 'today') { /* same day */ }

    const fromStr = from.toISOString().split('T')[0];
    const toStr = to.toISOString().split('T')[0];
    setDateRange({ from: fromStr, to: toStr });
    loadAnalytics(fromStr, toStr);
  };

  const loadAnalytics = async (from, to) => {
    setLoading(true);
    try {
      const res = await api.get('/analytics/sales', { params: { from, to } });
      setData(res.data);
    } catch { setData(null); }
    setLoading(false);
  };

  const exportCSV = async () => {
    try {
      const res = await api.get('/analytics/export/csv', {
        params: { from: dateRange.from, to: dateRange.to },
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `sales-${dateRange.from}-to-${dateRange.to}.csv`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch { /* ignore */ }
  };

  const exportJSON = async () => {
    try {
      const res = await api.get('/analytics/export/json', {
        params: { from: dateRange.from, to: dateRange.to },
      });
      const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/json' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `sales-${dateRange.from}-to-${dateRange.to}.json`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch { /* ignore */ }
  };

  if (loading) return <div className="loading-page"><div className="spinner"></div></div>;

  return (
    <div className="analytics-page">
      <div className="analytics-header">
        <h1>📊 Sales Analytics</h1>
        <div className="date-filters">
          {['today', '7d', '30d', '90d'].map((p) => (
            <button key={p} className={`date-preset ${preset === p ? 'active' : ''}`}
              onClick={() => applyPreset(p)}>{p === 'today' ? 'Today' : p}</button>
          ))}
          <div className="export-buttons">
            <button className="btn btn-ghost btn-sm" onClick={exportCSV}>📥 CSV</button>
            <button className="btn btn-ghost btn-sm" onClick={exportJSON}>📥 JSON</button>
          </div>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="stats-grid">
        <div className="glass-card stat-card stat-revenue">
          <div className="stat-icon">💰</div>
          <div className="stat-value">${data?.totalRevenue?.toFixed(2) || '0.00'}</div>
          <div className="stat-label">Total Revenue</div>
        </div>
        <div className="glass-card stat-card stat-orders">
          <div className="stat-icon">📦</div>
          <div className="stat-value">{data?.totalOrders || 0}</div>
          <div className="stat-label">Total Orders</div>
        </div>
        <div className="glass-card stat-card stat-avg">
          <div className="stat-icon">📈</div>
          <div className="stat-value">${data?.averageOrderValue?.toFixed(2) || '0.00'}</div>
          <div className="stat-label">Avg Order Value</div>
        </div>
        <div className="glass-card stat-card stat-products">
          <div className="stat-icon">🛍️</div>
          <div className="stat-value">{data?.totalProductsSold || 0}</div>
          <div className="stat-label">Products Sold</div>
        </div>
      </div>

      {/* Charts */}
      <div className="charts-grid">
        <div className="glass-card chart-card">
          <h3>Revenue Trend</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={data?.dailySales || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
              <XAxis dataKey="date" tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(99,102,241,0.3)', borderRadius: '8px', color: '#f1f5f9' }} />
              <Line type="monotone" dataKey="revenue" stroke="#6366f1" strokeWidth={2} dot={{ fill: '#6366f1', r: 4 }} activeDot={{ r: 6 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="glass-card chart-card">
          <h3>Top Products</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={data?.topProducts?.slice(0, 5) || []} layout="vertical">
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(148,163,184,0.1)" />
              <XAxis type="number" tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <YAxis dataKey="productName" type="category" tick={{ fill: '#94a3b8', fontSize: 11 }} width={100} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid rgba(99,102,241,0.3)', borderRadius: '8px', color: '#f1f5f9' }} />
              <Bar dataKey="revenue" fill="#06b6d4" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>
    </div>
  );
}
