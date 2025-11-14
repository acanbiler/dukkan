import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { User, LoginRequest, RegisterRequest } from '../types/auth';
import { authService } from '../services/authService';
import { notifications } from '@mantine/notifications';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (request: LoginRequest) => Promise<void>;
  register: (request: RegisterRequest) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
  isAdmin: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const { t } = useTranslation();
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Load user from localStorage on mount
    const storedUser = authService.getUser();
    if (storedUser) {
      setUser(storedUser);
    }
    setLoading(false);
  }, []);

  const login = async (request: LoginRequest) => {
    try {
      const response = await authService.login(request);

      const userData: User = {
        userId: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role,
      };

      authService.setToken(response.token);
      authService.setUser(userData);
      setUser(userData);

      notifications.show({
        title: t('common.success'),
        message: t('auth.login.success', { name: response.firstName }),
        color: 'green',
      });
    } catch (error: any) {
      notifications.show({
        title: t('common.error'),
        message: error.response?.data?.message || t('auth.login.error'),
        color: 'red',
      });
      throw error;
    }
  };

  const register = async (request: RegisterRequest) => {
    try {
      const response = await authService.register(request);

      const userData: User = {
        userId: response.userId,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role,
      };

      authService.setToken(response.token);
      authService.setUser(userData);
      setUser(userData);

      notifications.show({
        title: t('common.success'),
        message: t('auth.register.success'),
        color: 'green',
      });
    } catch (error: any) {
      notifications.show({
        title: t('common.error'),
        message: error.response?.data?.message || t('auth.register.error'),
        color: 'red',
      });
      throw error;
    }
  };

  const logout = () => {
    authService.removeToken();
    setUser(null);
    notifications.show({
      title: t('common.success'),
      message: t('auth.logout.success'),
      color: 'blue',
    });
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isAdmin: user?.role === 'ADMIN',
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
