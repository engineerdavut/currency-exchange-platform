// src/hooks/useAuth.ts (Redux için Refaktör Edilmiş Hali)
import { useCallback } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { AppDispatch, RootState } from '../store/store'; // Store tiplerini import et
import { login, logout, register, resetError as resetAuthErrorAction } from '../store/authSlice'; // Thunk'ları import et
import { LoginRequest, RegisterRequest } from '../types/auth'; // Tipleri import et

export const useAuthActions = () => {
  const dispatch = useDispatch<AppDispatch>();

  // Thunk'ların loading/error durumunu takip etmek için selector'lar
  // Not: Redux Toolkit otomatik olarak thunk durumunu slice'a eklemez.
  // İsterseniz slice'a manuel olarak 'loginLoading', 'registerLoading' gibi
  // state'ler ekleyip extraReducer'larda güncelleyebilirsiniz.
  // Şimdilik genel auth.isLoading ve auth.error'ı kullanalım.
  const isLoading = useSelector((state: RootState) => state.auth.isLoading);
  const error = useSelector((state: RootState) => state.auth.error);

  const loginUser = useCallback(
    async (credentials: LoginRequest) => {
      // login thunk'ı zaten promise döndürür (unwrap().then() veya try/catch ile handle edilebilir)
      // Component tarafında sonucu handle etmek daha esnek olabilir.
      // return dispatch(login(credentials)).unwrap(); // Bu, component'ta try/catch gerektirir
      await dispatch(login(credentials)); // Sadece dispatch et, sonucu component'ta selector ile izle
    },
    [dispatch]
  );

  const registerUser = useCallback(
    async (userData: RegisterRequest) => {
     // Register thunk'ının sonucunu component'a döndürebiliriz (başarı/hata mesajı için)
      const resultAction = await dispatch(register(userData));
      if (register.fulfilled.match(resultAction)) {
           return { success: true, message: resultAction.payload.message };
      } else if(register.rejected.match(resultAction)) {
           return { success: false, error: resultAction.payload };
      }
      // Beklenmedik durum
      return { success: false, error: 'An unknown registration error occurred.'};
    },
    [dispatch]
  );

  const logoutUser = useCallback(async () => {
    await dispatch(logout());
  }, [dispatch]);

  const resetError = useCallback(() => {
     dispatch(resetAuthErrorAction());
  },[dispatch]);


  return {
    login: loginUser,
    register: registerUser,
    logout: logoutUser,
    resetAuthError: resetError, // Hata mesajını temizlemek için
    isLoading, // Genel auth yüklenme durumu
    error,     // Genel auth hatası
  };
};