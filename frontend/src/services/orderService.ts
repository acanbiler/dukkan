import axios from 'axios';
import { Order, OrdersPageResponse, PlaceOrderRequest } from '../types/order';
import { authService } from './authService';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const orderClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add request interceptor to include auth token and user ID
orderClient.interceptors.request.use(
  (config) => {
    const token = authService.getToken();
    const user = authService.getUser();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (user && user.userId) {
      config.headers['X-User-Id'] = user.userId;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const orderService = {
  /**
   * Place a new order
   */
  placeOrder: async (request: PlaceOrderRequest): Promise<Order> => {
    const response = await orderClient.post<Order>('/orders', request);
    return response.data;
  },

  /**
   * Get user's order history (paginated)
   */
  getMyOrders: async (page: number = 0, size: number = 10): Promise<OrdersPageResponse> => {
    const response = await orderClient.get<OrdersPageResponse>('/orders/my-orders', {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * Get order by ID
   */
  getOrderById: async (orderId: string): Promise<Order> => {
    const response = await orderClient.get<Order>(`/orders/${orderId}`);
    return response.data;
  },

  /**
   * Cancel an order
   */
  cancelOrder: async (orderId: string): Promise<Order> => {
    const response = await orderClient.post<Order>(`/orders/${orderId}/cancel`);
    return response.data;
  },
};
