import axios, { AxiosInstance, AxiosError } from 'axios';
import { ApiResponse, ErrorResponse } from '@/types/api';

/**
 * Create axios instance with base configuration
 */
const createApiClient = (): AxiosInstance => {
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

  const client = axios.create({
    baseURL,
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: 10000,
  });

  // Request interceptor
  client.interceptors.request.use(
    (config) => {
      // Add any auth tokens here in the future
      // const token = localStorage.getItem('authToken');
      // if (token) {
      //   config.headers.Authorization = `Bearer ${token}`;
      // }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );

  // Response interceptor
  client.interceptors.response.use(
    (response) => {
      return response;
    },
    (error: AxiosError<ErrorResponse>) => {
      // Handle errors globally
      if (error.response) {
        const errorResponse = error.response.data;
        console.error('API Error:', errorResponse);
      } else if (error.request) {
        console.error('Network Error:', error.message);
      } else {
        console.error('Error:', error.message);
      }
      return Promise.reject(error);
    }
  );

  return client;
};

export const apiClient = createApiClient();

/**
 * Helper function to extract data from API response
 */
export const extractData = <T>(response: ApiResponse<T>): T => {
  return response.data;
};
