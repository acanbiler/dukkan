import { describe, it, expect, vi, beforeEach } from 'vitest';
import type { LoginRequest, RegisterRequest, AuthResponse } from '../types/auth';

// Create mocks using vi.hoisted to ensure they're available during module initialization
const { mockPost } = vi.hoisted(() => ({
  mockPost: vi.fn(),
}));

// Mock axios before imports
vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      post: mockPost,
    })),
  },
}));

// Now import after mocks
import { authService } from './authService';

const mockAuthResponse: AuthResponse = {
  userId: '123',
  email: 'test@example.com',
  firstName: 'John',
  lastName: 'Doe',
  role: 'CUSTOMER',
  token: 'mock-jwt-token',
};

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

describe('authService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  describe('register', () => {
    it('should register user successfully', async () => {
      const registerRequest: RegisterRequest = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      };

      mockPost.mockResolvedValue({ data: mockAuthResponse });

      const result = await authService.register(registerRequest);

      expect(result).toEqual(mockAuthResponse);
      expect(mockPost).toHaveBeenCalledWith('/auth/register', registerRequest);
    });

    it('should handle registration error', async () => {
      const registerRequest: RegisterRequest = {
        email: 'test@example.com',
        password: 'password123',
        firstName: 'John',
        lastName: 'Doe',
      };

      const error = new Error('Email already exists');
      mockPost.mockRejectedValue(error);

      await expect(authService.register(registerRequest)).rejects.toThrow('Email already exists');
    });
  });

  describe('login', () => {
    it('should login user successfully', async () => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'password123',
      };

      mockPost.mockResolvedValue({ data: mockAuthResponse });

      const result = await authService.login(loginRequest);

      expect(result).toEqual(mockAuthResponse);
      expect(mockPost).toHaveBeenCalledWith('/auth/login', loginRequest);
    });

    it('should handle login error', async () => {
      const loginRequest: LoginRequest = {
        email: 'test@example.com',
        password: 'wrong-password',
      };

      const error = new Error('Invalid credentials');
      mockPost.mockRejectedValue(error);

      await expect(authService.login(loginRequest)).rejects.toThrow('Invalid credentials');
    });
  });

  describe('getToken', () => {
    it('should get token from localStorage', () => {
      localStorage.setItem('token', 'mock-token');

      const token = authService.getToken();

      expect(token).toBe('mock-token');
    });

    it('should return null if token not found', () => {
      const token = authService.getToken();

      expect(token).toBeNull();
    });
  });

  describe('setToken', () => {
    it('should store token in localStorage', () => {
      authService.setToken('new-token');

      expect(localStorage.getItem('token')).toBe('new-token');
    });
  });

  describe('removeToken', () => {
    it('should remove token from localStorage', () => {
      localStorage.setItem('token', 'mock-token');
      localStorage.setItem('user', '{"userId":"123"}');

      authService.removeToken();

      expect(localStorage.getItem('token')).toBeNull();
      expect(localStorage.getItem('user')).toBeNull();
    });
  });

  describe('isAuthenticated', () => {
    it('should return true if token exists', () => {
      localStorage.setItem('token', 'mock-token');

      const isAuth = authService.isAuthenticated();

      expect(isAuth).toBe(true);
    });

    it('should return false if token does not exist', () => {
      const isAuth = authService.isAuthenticated();

      expect(isAuth).toBe(false);
    });
  });

  describe('getUser', () => {
    it('should get user from localStorage', () => {
      const user = {
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      };
      localStorage.setItem('user', JSON.stringify(user));

      const result = authService.getUser();

      expect(result).toEqual(user);
    });

    it('should return null if user not found', () => {
      const result = authService.getUser();

      expect(result).toBeNull();
    });

    it('should handle corrupted user data', () => {
      localStorage.setItem('user', 'invalid-json');

      expect(() => authService.getUser()).toThrow();
    });
  });

  describe('setUser', () => {
    it('should store user in localStorage', () => {
      const user = {
        userId: '123',
        email: 'test@example.com',
        firstName: 'John',
        lastName: 'Doe',
        role: 'CUSTOMER',
      };

      authService.setUser(user);

      const stored = localStorage.getItem('user');
      expect(stored).toBeTruthy();
      expect(JSON.parse(stored!)).toEqual(user);
    });

    it('should overwrite existing user', () => {
      const user1 = {
        userId: '123',
        email: 'user1@example.com',
        firstName: 'User',
        lastName: 'One',
        role: 'CUSTOMER',
      };

      const user2 = {
        userId: '456',
        email: 'user2@example.com',
        firstName: 'User',
        lastName: 'Two',
        role: 'ADMIN',
      };

      authService.setUser(user1);
      authService.setUser(user2);

      const stored = localStorage.getItem('user');
      expect(JSON.parse(stored!)).toEqual(user2);
    });
  });
});
