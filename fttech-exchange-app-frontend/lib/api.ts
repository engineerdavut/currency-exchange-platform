import axios from 'axios';
import { LoginRequest, RegisterRequest } from '../types/auth';
import { DepositWithdrawRequest } from '../types/account';
import { ExchangeRequest } from '../types/exchange';
import { clearStoredUsername } from '../utils/authStorage';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

if (!API_BASE_URL) {
  console.error("FATAL ERROR: NEXT_PUBLIC_API_URL is not defined! Falling back to localhost, but this will likely fail in production.");

}

const api = axios.create({
  baseURL: API_BASE_URL,// || 'http://localhost:8090/api',
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest'
  }
});

api.interceptors.response.use(
  response => response,
  error => {
    if (typeof window !== 'undefined' && error.response) {
      const { status } = error.response;
      const currentPath = window.location.pathname;

      if (status === 401 && currentPath !== '/login' && currentPath !== '/register' && currentPath !== '/') {
        console.warn(`Unauthorized (401) on path ${currentPath}, redirecting to /login.`);
        clearStoredUsername();
        window.location.href = '/login';
      } else if (status === 401) {
        console.log(`Intercepted 401 on public path (${currentPath}), letting the calling code handle it.`);
      }
    }
    return Promise.reject(error);
  }
);

api.interceptors.request.use(config => {
  config.withCredentials = true;
  console.log('Sending cookies:', document.cookie);
  return config;
});



export const authApi = {
  login: (credentials: LoginRequest) => api.post('/auth/login', credentials),
  register: (userData: RegisterRequest) => api.post('/auth/register', userData),
  logout: () => api.post('/auth/logout'),
  checkAuth: () => api.get('/auth/check')
};

export const accountApi = {
  getWallet: () => api.get('/account/wallet'),
  deposit: (data: DepositWithdrawRequest) => api.post('/account/deposit', data),
  withdraw: (data: DepositWithdrawRequest) => api.post('/account/withdraw', data),
};

export const transactionApi = {
  getTransactions: (currencyType?: string) => {
    const params = currencyType && currencyType !== 'ALL'
      ? `?currencyType=${currencyType}`
      : '';
    return api.get(`/account/transactions${params}`);
  },
};

export const exchangeApi = {
  processExchange: (data: Omit<ExchangeRequest, 'username'>) => api.post('/exchange/process', data),
};

export default api;
