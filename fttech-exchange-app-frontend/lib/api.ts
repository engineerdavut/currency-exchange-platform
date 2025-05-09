import axios from 'axios';
import { LoginRequest, RegisterRequest } from '../types/auth';
import { DepositWithdrawRequest } from '../types/account';
import { ExchangeRequest } from '../types/exchange';
import { clearStoredUsername } from '../utils/authStorage';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8090';


const api = axios.create({
  baseURL: API_URL, 
  withCredentials: true,
  headers: {
    'X-Requested-With': 'XMLHttpRequest'
  }
});


api.interceptors.response.use(
  response => response,
  error => {
    // Sadece client tarafında çalışıyorsa ve response varsa bu kontrolü yap
    if (typeof window !== 'undefined' && error.response) {
        const { status } = error.response;
        const currentPath = window.location.pathname; // Mevcut sayfa yolu

        // Eğer 401 hatasıysa VE kullanıcı zaten login veya register sayfasında DEĞİLSE yönlendir.
        if (status === 401 && currentPath !== '/login' && currentPath !== '/register'&& currentPath !== '/') {
            console.warn(`Unauthorized (401) on path ${currentPath}, redirecting to /login.`);
            // Yönlendirme yapmadan önce belki storage'ı temizlemek iyi olabilir (opsiyonel)
             clearStoredUsername(); // authStorage'dan import etmen gerekebilir
             window.location.href = '/login';
             // Yönlendirme sonrası hatayı tekrar fırlatmamak önemli olabilir,
             // çünkü sayfa zaten yenilenecek. Ancak diğer 401 durumları için fırlatmak gerekebilir.
             // Bu kısmı projenin akışına göre ayarlamak lazım. Şimdilik sadece yönlendirip bırakalım.
             // return; // Promise reject etmeden çıkabiliriz, sayfa zaten yenilenecek.
        } else if (status === 401) {
            console.log(`Intercepted 401 on public path (${currentPath}), letting the calling code handle it.`);
            // Login/Register sayfasındayken 401 gelmesi normal, yönlendirme yapma, hatayı fırlat.
        }
    }
    // Diğer tüm hataları veya server-side hatalarını normal şekilde reject et
    return Promise.reject(error);
  }
);

api.interceptors.request.use(config => {
  config.withCredentials = true; 
  console.log('Sending cookies:', document.cookie);
  return config;
});


export const authApi = {
  login: (credentials: LoginRequest) => api.post('/api/auth/login', credentials),
  register: (userData: RegisterRequest) => api.post('/api/auth/register', userData),
  logout: () => api.post('/api/auth/logout'),
  checkAuth: () => api.get('/api/auth/check')
};

export const accountApi = {
  getWallet: () => api.get('/api/account/wallet'),
  deposit: (data: DepositWithdrawRequest) => api.post('/api/account/deposit', data),
  withdraw: (data: DepositWithdrawRequest) => api.post('/api/account/withdraw', data),
};

export const transactionApi = {
  getTransactions: (currencyType?: string) => {
    const params = currencyType && currencyType !== 'ALL' 
      ? `?currencyType=${currencyType}` 
      : '';
    return api.get(`/api/account/transactions${params}`);
  },
};

export const exchangeApi = {
  processExchange: (data: Omit<ExchangeRequest, 'username'>) => api.post('/api/exchange/process', data),
};

export default api;
