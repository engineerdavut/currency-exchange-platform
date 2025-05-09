// app/layout.tsx
// src/app/layout.tsx (Redux Provider ile Güncellenmiş)
"use client"; // Provider ve AppInitializer client bileşeni gerektirir

import React, { useEffect } from "react";
import { Provider, useDispatch } from 'react-redux'; // react-redux importları
import { Inter } from 'next/font/google';
import { Layout } from '../components/common/Layout'; // Doğru import
import { store, AppDispatch } from '../store/store'; // Redux store import
import { checkAuthStatus } from "../store/authSlice"; // checkAuthStatus thunk import
import './globals.css';

const inter = Inter({ subsets: ['latin'] });

// Uygulama ilk yüklendiğinde auth durumunu kontrol eden bileşen
function AppInitializer({ children }: { children: React.ReactNode }) {
  const dispatch = useDispatch<AppDispatch>(); // Typed dispatch

  useEffect(() => {
    dispatch(checkAuthStatus()); // Auth durumunu kontrol et
  }, [dispatch]);

  return <>{children}</>; // Sadece children'ı render et
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={inter.className}>
        {/* Redux Provider ile tüm uygulamayı sar */}
        <Provider store={store}>
          {/* AppInitializer Provider *içinde* olmalı ki dispatch kullanabilsin */}
          <AppInitializer>
            <Layout>{children}</Layout>
          </AppInitializer>
        </Provider>
      </body>
    </html>
  )
}