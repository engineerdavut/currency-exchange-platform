// src/components/common/Layout.tsx (Redux State Kullanımı ile Güncellenmiş)
"use client"; // useSelector kullandığı için client bileşeni olmalı

import React from 'react';
import { useSelector, useDispatch } from 'react-redux'; // Redux hook'ları
import { Header } from './Header'; // Doğru import
import { Footer } from './Footer'; // Doğru import
import { RootState, AppDispatch } from '../../store/store'; // Tipler
import { logout } from '../../store/authSlice'; // logout thunk'ı
import { Loading } from './Loading'; // Loading bileşeni

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  // Redux store'dan state'leri seç
  const { isAuthenticated, user, isLoading: isAuthLoading } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();

  const handleLogout = () => {
    dispatch(logout());
  };

  // İlk auth kontrolü yüklenirken bir loading state gösterebiliriz
  if (isAuthLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loading size="lg" />
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header'a Redux state'inden gelen bilgileri ve logout fonksiyonunu geç */}
      <Header
        isAuthenticated={isAuthenticated}
        username={user?.username}
        onLogout={handleLogout}
      />
      <main className="flex-grow bg-gray-50 py-6">
        <div className="container mx-auto px-4">
          {children}
        </div>
      </main>
      <Footer />
    </div>
  );
};