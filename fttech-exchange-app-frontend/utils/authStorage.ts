// src/utils/authStorage.ts
// Artık User tipine ihtiyacımız yok burada
// import { User } from '../types/auth';

const USERNAME_STORAGE_KEY = 'username'; // Anahtarı değiştirelim

export const getStoredUsername = (): string | null => {
  if (typeof window === 'undefined') return null;
  return sessionStorage.getItem(USERNAME_STORAGE_KEY);
};

export const setStoredUsername = (username: string | null): void => {
  if (typeof window !== 'undefined') {
    if (username) {
       sessionStorage.setItem(USERNAME_STORAGE_KEY, username);
    } else {
       clearStoredUsername(); // null gelirse temizle
    }
  }
};

export const clearStoredUsername = (): void => {
  if (typeof window !== 'undefined') {
    sessionStorage.removeItem(USERNAME_STORAGE_KEY);
  }
};