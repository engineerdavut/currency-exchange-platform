
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import axios, { AxiosError } from 'axios';
import { authApi } from '../lib/api';
import { AuthState, LoginRequest, RegisterRequest } from '../types/auth';
import { setStoredUsername, clearStoredUsername, getStoredUsername } from '../utils/authStorage';

const initialState: AuthState = {
  user: null,
  isAuthenticated: false,
  isLoading: true,
  error: null,
};

export const checkAuthStatus = createAsyncThunk<
  { username: string },
  void,
  { rejectValue: string }
>(
  'auth/checkAuthStatus',
  async (_, { rejectWithValue }) => {
    console.log('Checking auth status...');
    try {
      await authApi.checkAuth();
      const storedUsername = getStoredUsername();
      if (storedUsername) {
        console.log('Auth status check successful via API (cookie valid), username from storage:', storedUsername);
        return { username: storedUsername };
      } else {
        console.warn('Cookie is valid but no username in storage. Clearing.');
        clearStoredUsername();
        return rejectWithValue('Inconsistent auth state.');
      }
    } catch (error) {
      const err = error as AxiosError | Error;
      console.log('Auth status check via API failed:', err.message || err);
      clearStoredUsername();
      if (axios.isAxiosError(error)) {
        return rejectWithValue(error.response?.status === 401 ? 'User not authenticated (401)' : 'API check auth request failed');
      }
      return rejectWithValue('API check auth request failed');
    }
  }
);

export const login = createAsyncThunk<
  { username: string },
  LoginRequest,
  { rejectValue: string }
>(
  'auth/login',
  async (credentials: LoginRequest, { rejectWithValue }) => {
    try {
      const response = await authApi.login(credentials);
      const data = response.data as { username: string };
      if (data.username) {
        const username = data.username;
        setStoredUsername(username);
        console.log('Login successful, username stored:', username);
        return { username };
      } else {
        console.error('Login API call succeeded but username not found in response:', data);
        return rejectWithValue('Login failed: Username not received.');
      }
    } catch (error) {
      const err = error as AxiosError<unknown> | Error;
      console.error('Login API call failed:', err);
      let message = 'Login request failed';
      if (axios.isAxiosError(error)) {
        message = error.response?.data?.message || error.response?.data?.error || error.message;
        if (error.response?.status === 503) {
          message = 'Login service is temporarily unavailable (503). Please try again later.';
        }
      } else if (error instanceof Error) {
        message = error.message;
      }
      return rejectWithValue(message);
    }
  }
);

export const register = createAsyncThunk<
  { message: string },
  RegisterRequest,
  { rejectValue: string }
>(
  'auth/register',
  async (userData: RegisterRequest, { rejectWithValue }) => {
    console.log('Dispatching register action...');
    try {
      const response = await authApi.register(userData);
      const responseData = response.data as { message?: string };
      console.log('Register API response data:', responseData);
      if (responseData && typeof responseData === 'object' && typeof responseData.message === 'string') {
        console.log('Registration successful, message received:', responseData.message);
        return { message: responseData.message };
      } else {
        console.error('Registration response format unexpected:', responseData);
        return rejectWithValue('Registration successful but response format is unexpected.');
      }
    } catch (error) {
      const err = error as AxiosError<unknown> | Error;
      console.error('Register API call failed:', err);
      let message = 'Registration failed';
      if (axios.isAxiosError(error)) {
        message = error.response?.data?.error ||
          error.response?.data?.message ||
          (typeof error.response?.data === 'string' ? error.response.data : null) ||
          error.message;
      } else if (error instanceof Error) {
        message = error.message;
      }
      return rejectWithValue(message);
    }
  }
);

export const logout = createAsyncThunk<void, void, { rejectValue: string }>(
  'auth/logout',
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  async (_, { rejectWithValue: _rejectWithValueNotUsed }) => {
    try {
      await authApi.logout();
    } catch (error) {
      const err = error as Error;
      console.error("API logout failed:", err.message || error);
    } finally {
      clearStoredUsername();
    }
  }
);

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    resetError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(checkAuthStatus.pending, (state) => {
        state.isLoading = true;
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
        state.error = action.payload as string || null;
        console.log("Check auth rejected in reducer:", action.payload);
      })
      .addCase(login.pending, (state) => {
        state.isLoading = true;
        state.error = null;
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
        state.isLoading = true;
        state.error = null;
      })
      .addCase(register.fulfilled, (state) => {
        state.isLoading = false;
      })
      .addCase(register.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      .addCase(logout.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.isAuthenticated = false;
        state.isLoading = false;
        state.error = null;
      })
      .addCase(logout.rejected, (state, action) => {
        state.user = null;
        state.isAuthenticated = false;
        state.isLoading = false;
        state.error = action.payload as string;
      });
  },
});

export const { resetError } = authSlice.actions;
export default authSlice.reducer;