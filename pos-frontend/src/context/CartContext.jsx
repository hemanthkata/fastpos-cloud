import { createContext, useContext, useState, useCallback } from 'react';
import api from '../api/axiosClient';
import toast from 'react-hot-toast';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const [cart, setCart] = useState({ items: [], subtotal: 0, taxAmount: 0, totalAmount: 0, totalItems: 0 });

  const fetchCart = useCallback(async () => {
    try {
      const res = await api.get('/cart');
      setCart(res.data);
    } catch { /* ignore if not logged in */ }
  }, []);

  const addToCart = async (productId, quantity = 1) => {
    try {
      const res = await api.post('/cart/add', { productId, quantity });
      setCart(res.data);
      toast.success('Added to cart!');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add');
    }
  };

  const updateQuantity = async (productId, quantity) => {
    try {
      const res = await api.put('/cart/update', { productId, quantity });
      setCart(res.data);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to update');
    }
  };

  const removeFromCart = async (productId) => {
    try {
      const res = await api.delete(`/cart/remove/${productId}`);
      setCart(res.data);
      toast.success('Removed from cart');
    } catch (err) {
      toast.error('Failed to remove');
    }
  };

  const clearCart = async () => {
    try {
      const res = await api.delete('/cart/clear');
      setCart(res.data);
    } catch { /* ignore */ }
  };

  return (
    <CartContext.Provider value={{ cart, fetchCart, addToCart, updateQuantity, removeFromCart, clearCart }}>
      {children}
    </CartContext.Provider>
  );
}

export const useCart = () => useContext(CartContext);
