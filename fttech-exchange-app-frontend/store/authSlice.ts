// src/store/authSlice.ts (Güncellenmiş ve Geliştirilmiş Tam Hali)
import { createSlice, createAsyncThunk, PayloadAction, ActionReducerMapBuilder } from '@reduxjs/toolkit';
import { authApi } from '../lib/api'; // Doğru import
import { AuthState, LoginRequest, RegisterRequest, LoginResponse, User } from '../types/auth'; // Doğru import
import { setStoredUsername, clearStoredUsername, getStoredUsername } from '../utils/authStorage'; // Yeni storage util import
import  jwtDecode  from 'jwt-decode'; // JWT decode etmek için

// Başlangıç state'ini tanımla
const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: true, // Başlangıçta auth durumu kontrol ediliyor
  error: null,
};

// Async Thunks (API çağrıları ve yan etkiler)

// Check Auth Status Thunk (Yeni)
export const checkAuthStatus = createAsyncThunk<
  { username: string }, // Başarılı durumda username döndürelim (Storage'dan veya JWT'den)
  void,
  { rejectValue: string }
 >(
  'auth/checkAuthStatus',
  async (_, { rejectWithValue }) => {
    console.log('Checking auth status...');
    try {
      // Önce API'ye soralım, cookie geçerli mi?
      await authApi.checkAuth(); // Bu sadece 200 OK veya 401 dönecek

      // Eğer API 200 OK döndüyse (yani hata fırlatmadıysa),
      // cookie geçerli demektir. Kullanıcı adını storage'dan almayı deneyelim.
      // VEYA daha iyisi, API isteği sırasında set edilen cookie'den decode edelim?
      // Ama HttpOnly olduğu için JS'den okuyamayız.
      // Bu durumda, state'i korumak için yine sessionStorage'a güvenmemiz gerekecek.
      const storedUsername = getStoredUsername(); // Storage'dan username al
      if (storedUsername) {
         console.log('Auth status check successful via API (cookie valid), username from storage:', storedUsername);
         // Kullanıcı objesini basitçe oluşturalım
         return { username: storedUsername };
      } else {
         // Cookie geçerli ama storage'da username yok? Bu garip bir durum.
         // Belki logout sonrası storage temizlenmemiş? Güvenlik için reject edelim.
         console.warn('Cookie is valid but no username in storage. Clearing.');
         clearStoredUsername();
         return rejectWithValue('Inconsistent auth state.');
      }

    } catch (error: any) {
      // API isteği hata fırlattı (muhtemelen 401 veya network hatası)
       console.log('Auth status check via API failed:', error.message || error);
       clearStoredUsername(); // Storage'ı temizle
      return rejectWithValue(error.response?.status === 401 ? 'User not authenticated (401)' : 'API check auth request failed');
    }
  }
);


// Login Thunk
export const login = createAsyncThunk<
  { username: string }, // Başarı durumunda sadece username döndürelim (JWT'den decode edip)
  LoginRequest,
  { rejectValue: string }
>(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const response = await authApi.login(credentials);
      // Backend sadece token dönüyor: { token: "..." }
      const data = response.data as { username: string }; // Tipi daraltalım

      if (data.username) {
        const username = data.username; // Doğrudan username'i al
        setStoredUsername(username);
        console.log('Login successful, username stored:', username);
        return { username };
     } else {
       console.error('Login API call succeeded but username not found in response:', data);
       return rejectWithValue('Login failed: Username not received.');
     }
    } catch (error: any) {
      console.error('Login API call failed:', error);
      const message = error.response?.data?.message || error.message || 'Login request failed';
       // 503 hatası için özel kontrol
       if (error.response?.status === 503) {
          return rejectWithValue('Login service is temporarily unavailable (503). Please try again later.');
       }
      return rejectWithValue(message);
    }
  }
);

// Register Thunk
export const register = createAsyncThunk<
  { message: string }, // Başarı payload tipi
  RegisterRequest,
  { rejectValue: string }
>(
  'auth/register',
  async (userData: RegisterRequest, { rejectWithValue }) => {
    console.log('Dispatching register action...');
    try {
      const response = await authApi.register(userData);
      // Backend'den gelen data ZATEN bir obje: { message: "..." }
      const responseData = response.data as { message?: string }; // Type assertion
      console.log('Register API response data:', responseData);

      // Gelen verinin bir obje olduğunu ve message alanı içerdiğini kontrol et
      if (responseData && typeof responseData === 'object' && typeof responseData.message === 'string') {
         console.log('Registration successful, message received:', responseData.message);
         // Doğrudan gelen objeyi (veya sadece mesajı içeren yeni bir obje) döndür
         return { message: responseData.message }; // Thunk'ın beklediği format bu
      } else {
         // Backend 200 OK döndü ama data formatı beklenmedik
         console.error('Registration response format unexpected:', responseData);
         return rejectWithValue('Registration successful but response format is unexpected.');
      }

    } catch (error: any) {
      console.error('Register API call failed:', error);
      const message = error.response?.data?.error ||
                      error.response?.data?.message ||
                      (typeof error.response?.data === 'string' ? error.response.data : null) ||
                      error.message ||
                      'Registration failed';
      return rejectWithValue(message);
    }
  }
);


// Logout Thunk
export const logout = createAsyncThunk<void, void, { rejectValue: string }>(
  'auth/logout',
  async (_, { rejectWithValue }) => {
    try {
      await authApi.logout();
    } catch (error: any) {
      console.error("API logout failed:", error);
    } finally {
       clearStoredUsername(); // Storage'ı temizle
       // clearStoredUser() da vardı, sadece username'e geçince bu yeterli.
    }
  }
);

// Auth Slice Tanımı
const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // ===> HATAYI GİDEREN TANIMLAMA <===
    resetError: (state) => {
      state.error = null; // Hata state'ini temizle
    }
    // Buraya başka senkron reducer'lar ekleyebilirsin (örn: setLoadingManuel)
  },
  extraReducers: (builder) => {
    // extraReducers kısmı aynı kalabilir
    builder
      .addCase(checkAuthStatus.pending, (state) => {
        state.isLoading = true;
        // state.error = null; // Opsiyonel: Check başlarken hatayı temizle
      })
      .addCase(checkAuthStatus.fulfilled, (state, action: PayloadAction<{ username: string }>) => {
        state.isLoading = false;
        state.user = { username: action.payload.username };
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(checkAuthStatus.rejected, (state, action) => {
        state.isLoading = false;
        state.user = null;
        state.isAuthenticated = false;
        state.error = null;
        console.log("Check auth rejected in reducer:", action.payload);
      })
      .addCase(login.pending, (state) => {
        state.isLoading = true;
        state.error = null; // Login başlarken hatayı temizle
      })
      .addCase(login.fulfilled, (state, action: PayloadAction<{ username: string }>) => {
        state.isLoading = false;
        state.user = { username: action.payload.username };
        state.isAuthenticated = true;
        state.error = null;
      })
      .addCase(login.rejected, (state, action) => {
        state.isLoading = false;
        state.isAuthenticated = false;
        state.user = null;
        state.error = action.payload as string;
      })
      .addCase(register.pending, (state) => {
         state.isLoading = true; // veya registerLoading
         state.error = null; // Register başlarken hatayı temizle
      })
      .addCase(register.fulfilled, (state) => {
         state.isLoading = false;
      })
      .addCase(register.rejected, (state, action) => {
         state.isLoading = false;
         state.error = action.payload as string;
      })
      .addCase(logout.pending, (state) => { /* ... */ })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
        state.isLoading = false;
        state.error = null;
      })
      .addCase(logout.rejected, (state, action) => { /* ... */ });
  },
});

// ===> ARTIK HATA VERMEMELİ <===
// resetError action creator'ı artık authSlice.actions içinde mevcut.
export const { resetError } = authSlice.actions;

export default authSlice.reducer;