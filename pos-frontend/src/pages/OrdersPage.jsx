import { useState, useEffect } from 'react';
import api from '../api/axiosClient';

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => { loadOrders(); }, []);

  const loadOrders = async () => {
    try {
      const res = await api.get('/orders', { params: { size: 50 } });
      setOrders(res.data.content || []);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const downloadInvoice = async (orderId, orderNumber) => {
    try {
      const res = await api.get(`/invoices/${orderId}`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `invoice-${orderNumber}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch { /* ignore */ }
  };

  if (loading) return <div className="loading-page"><div className="spinner"></div></div>;

  return (
    <div className="orders-page">
      <h1>📦 Order History</h1>

      {orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '60px', color: 'var(--text-muted)' }}>
          <p style={{ fontSize: '18px' }}>No orders yet</p>
          <p>Place your first order from the POS!</p>
        </div>
      ) : (
        <table className="orders-table">
          <thead>
            <tr>
              <th>Order #</th>
              <th>Date</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th>Payment</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => (
              <tr key={order.id}>
                <td style={{ fontWeight: 600, color: 'var(--primary-light)' }}>{order.orderNumber}</td>
                <td style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>
                  {new Date(order.createdAt).toLocaleString()}
                </td>
                <td>{order.items?.length || 0} items</td>
                <td style={{ fontWeight: 700, color: 'var(--success)' }}>${order.totalAmount?.toFixed(2)}</td>
                <td>
                  <span className={`status-badge ${order.status?.toLowerCase()}`}>
                    {order.status}
                  </span>
                </td>
                <td>
                  <span className={`status-badge ${order.paymentStatus?.toLowerCase()}`}>
                    {order.paymentStatus}
                  </span>
                </td>
                <td>
                  <button className="btn btn-ghost btn-sm"
                    onClick={() => downloadInvoice(order.id, order.orderNumber)}>
                    📄 Invoice
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
