"use client";

import React, { useEffect } from "react";
import { Provider, useDispatch } from "react-redux";
import { Inter } from "next/font/google";
import { Layout } from "../components/common/Layout";
import { store, AppDispatch } from "../store/store";
import { checkAuthStatus } from "../store/authSlice";
import "./globals.css";

const inter = Inter({ subsets: ["latin"] });

function AppInitializer({ children }: { children: React.ReactNode }) {
  const dispatch = useDispatch<AppDispatch>();

  useEffect(() => {
    dispatch(checkAuthStatus());
  }, [dispatch]);

  return <>{children}</>;
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className={inter.className}>
        <Provider store={store}>
          <AppInitializer>
            <Layout>{children}</Layout>
          </AppInitializer>
        </Provider>
      </body>
    </html>
  );
}
