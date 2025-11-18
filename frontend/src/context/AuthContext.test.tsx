import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import { AuthProvider, useAuth } from './AuthContext';
import { authService } from '../services/authService';
import { notifications } from '@mantine/notifications';
import type { LoginRequest, RegisterRequest, User } from '../types/auth';

// Mock Mantine notifications
vi.mock('@mantine/notifications', () => ({
  notifications: {
    show: vi.fn(),
  },
}));

// Mock authService
vi.mock('../services/authService', () => ({
  authService: {
    login: vi.fn(),
    register: vi.fn(),
    getUser: vi.fn(),
    setToken: vi.fn(),
    setUser: vi.fn(),
    removeToken: vi.fn(),
  },
}));

// Mock react-i18next
vi.mock('react-i18next', () => ({
  useTranslation: () => ({
    t: (key: string, params?: any) => {
      const translations: Record<string, string> = {
        'common.success': 'Success',
        'common.error': 'Error',
        'auth.login.success': `Welcome ${params?.name || 'User'}`,
        'auth.login.error': 'Login failed',
        'auth.register.success': 'Registration successful',
        'auth.register.error': 'Registration failed',
        'auth.logout.success': 'Logged out successfully',
      };
      return translations[key] || key;
    },
  }),
}));

const mockUser: User = {
  userId: '123',
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  role: 'CUSTOMER',
};

const mockAdminUser: User = {
  ...mockUser,
  role: 'ADMIN',
};

const mockLoginResponse = {
  userId: '123',
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  role: 'CUSTOMER',
  token: 'mock-jwt-token',
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(authService.getUser).mockReturnValue(null);
  });

  describe('Provider initialization', () => {
    it('should initialize with no user and loading true', async () => {
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      // Initially loading should be true
      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAdmin).toBe(false);
    });

    it('should load user from authService on mount', async () => {
      vi.mocked(authService.getUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toEqual(mockUser);
      expect(result.current.isAuthenticated).toBe(true);
      expect(result.current.isAdmin).toBe(false);
      expect(authService.getUser).toHaveBeenCalled();
    });

    it('should identify admin user correctly', async () => {
      vi.mocked(authService.getUser).mockReturnValue(mockAdminUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.isAdmin).toBe(true);
    });
  });

  describe('login', () => {
    it('should login successfully and update user state', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123',
      };

      await act(async () => {
        await result.current.login(loginRequest);
      });

      expect(authService.login).toHaveBeenCalledWith(loginRequest);
      expect(authService.setToken).toHaveBeenCalledWith('mock-jwt-token');
      expect(authService.setUser).toHaveBeenCalledWith({
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      });
      expect(result.current.user).toEqual({
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      });
      expect(result.current.isAuthenticated).toBe(true);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Success',
        message: 'Welcome John',
        color: 'green',
      });
    });

    it('should handle login error and show notification', async () => {
      const errorResponse = {
        response: {
          data: {
            message: 'Invalid credentials',
          },
        },
      };
      vi.mocked(authService.login).mockRejectedValue(errorResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'wrong-password',
      };

      await expect(
        act(async () => {
          await result.current.login(loginRequest);
        })
      ).rejects.toEqual(errorResponse);

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Error',
        message: 'Invalid credentials',
        color: 'red',
      });
    });

    it('should handle login error without message', async () => {
      const errorResponse = new Error('Network error');
      vi.mocked(authService.login).mockRejectedValue(errorResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123',
      };

      await expect(
        act(async () => {
          await result.current.login(loginRequest);
        })
      ).rejects.toEqual(errorResponse);

      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Error',
        message: 'Login failed',
        color: 'red',
      });
    });
  });

  describe('register', () => {
    it('should register successfully and update user state', async () => {
      vi.mocked(authService.register).mockResolvedValue(mockLoginResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const registerRequest: RegisterRequest = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      };

      await act(async () => {
        await result.current.register(registerRequest);
      });

      expect(authService.register).toHaveBeenCalledWith(registerRequest);
      expect(authService.setToken).toHaveBeenCalledWith('mock-jwt-token');
      expect(authService.setUser).toHaveBeenCalledWith({
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      });
      expect(result.current.user).toEqual({
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      });
      expect(result.current.isAuthenticated).toBe(true);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Success',
        message: 'Registration successful',
        color: 'green',
      });
    });

    it('should handle registration error and show notification', async () => {
      const errorResponse = {
        response: {
          data: {
            message: 'Email already exists',
          },
        },
      };
      vi.mocked(authService.register).mockRejectedValue(errorResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const registerRequest: RegisterRequest = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      };

      await expect(
        act(async () => {
          await result.current.register(registerRequest);
        })
      ).rejects.toEqual(errorResponse);

      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Error',
        message: 'Email already exists',
        color: 'red',
      });
    });

    it('should handle registration error without message', async () => {
      const errorResponse = new Error('Network error');
      vi.mocked(authService.register).mockRejectedValue(errorResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      const registerRequest: RegisterRequest = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      };

      await expect(
        act(async () => {
          await result.current.register(registerRequest);
        })
      ).rejects.toEqual(errorResponse);

      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Error',
        message: 'Registration failed',
        color: 'red',
      });
    });
  });

  describe('logout', () => {
    it('should logout and clear user state', async () => {
      vi.mocked(authService.getUser).mockReturnValue(mockUser);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.user).toEqual(mockUser);

      act(() => {
        result.current.logout();
      });

      expect(authService.removeToken).toHaveBeenCalled();
      expect(result.current.user).toBeNull();
      expect(result.current.isAuthenticated).toBe(false);
      expect(result.current.isAdmin).toBe(false);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Success',
        message: 'Logged out successfully',
        color: 'blue',
      });
    });

    it('should logout when no user is logged in', async () => {
      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      act(() => {
        result.current.logout();
      });

      expect(authService.removeToken).toHaveBeenCalled();
      expect(result.current.user).toBeNull();
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Success',
        message: 'Logged out successfully',
        color: 'blue',
      });
    });
  });

  describe('useAuth hook', () => {
    it('should throw error when used outside provider', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => {
        renderHook(() => useAuth());
      }).toThrow('useAuth must be used within AuthProvider');

      consoleSpy.mockRestore();
    });
  });

  describe('authentication state', () => {
    it('should correctly update isAuthenticated when user logs in', async () => {
      vi.mocked(authService.login).mockResolvedValue(mockLoginResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.isAuthenticated).toBe(false);

      await act(async () => {
        await result.current.login({
          email: 'test@example.com',
          password: 'password123',
        });
      });

      expect(result.current.isAuthenticated).toBe(true);
    });

    it('should correctly update isAdmin for admin user', async () => {
      const adminResponse = { ...mockLoginResponse, role: 'ADMIN' };
      vi.mocked(authService.login).mockResolvedValue(adminResponse);

      const { result } = renderHook(() => useAuth(), {
        wrapper: AuthProvider,
      });

      await waitFor(() => {
        expect(result.current.loading).toBe(false);
      });

      expect(result.current.isAdmin).toBe(false);

      await act(async () => {
        await result.current.login({
          email: 'admin@example.com',
          password: 'password123',
        });
      });

      expect(result.current.isAdmin).toBe(true);
      expect(result.current.user?.role).toBe('ADMIN');
    });
  });
});
