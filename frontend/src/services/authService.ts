import axios from 'axios';
import { AuthResponse, LoginRequest, RegisterRequest } from '../types/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const authClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authService = {
  /**
   * Register new user
   */
  register: async (request: RegisterRequest): Promise<AuthResponse> => {
    const response = await authClient.post<AuthResponse>('/auth/register', request);
    return response.data;
  },

  /**
   * Login user
   */
  login: async (request: LoginRequest): Promise<AuthResponse> => {
    const response = await authClient.post<AuthResponse>('/auth/login', request);
    return response.data;
  },

  /**
   * Get stored token
   */
  getToken: (): string | null => {
    return localStorage.getItem('token');
  },

  /**
   * Store token
   */
  setToken: (token: string): void => {
    localStorage.setItem('token', token);
  },

  /**
   * Remove token
   */
  removeToken: (): void => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  /**
   * Check if user is authenticated
   */
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token');
  },

  /**
   * Get stored user
   */
  getUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  /**
   * Store user
   */
  setUser: (user: any): void => {
    localStorage.setItem('user', JSON.stringify(user));
  },
};
