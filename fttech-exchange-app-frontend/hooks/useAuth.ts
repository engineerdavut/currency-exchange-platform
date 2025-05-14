
import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store';
import { login, logout, register, resetError as resetAuthErrorAction } from '../store/authSlice';
import { LoginRequest, RegisterRequest } from '../types/auth';

export const useAuthActions = () => {
  const dispatch = useDispatch<AppDispatch>();

  const isLoading = useSelector((state: RootState) => state.auth.isLoading);
  const error = useSelector((state: RootState) => state.auth.error);

  const loginUser = useCallback(
    async (credentials: LoginRequest) => {
      await dispatch(login(credentials));
    },
    [dispatch]
  );

  const registerUser = useCallback(
    async (userData: RegisterRequest) => {
      const resultAction = await dispatch(register(userData));
      if (register.fulfilled.match(resultAction)) {
        return { success: true, message: resultAction.payload.message };
      } else if (register.rejected.match(resultAction)) {
        return { success: false, error: resultAction.payload };
      }
      return { success: false, error: 'An unknown registration error occurred.' };
    },
    [dispatch]
  );

  const logoutUser = useCallback(async () => {
    await dispatch(logout());
  }, [dispatch]);

  const resetError = useCallback(() => {
    dispatch(resetAuthErrorAction());
  }, [dispatch]);


  return {
    login: loginUser,
    register: registerUser,
    logout: logoutUser,
    resetAuthError: resetError,
    isLoading,
    error,
  };
};