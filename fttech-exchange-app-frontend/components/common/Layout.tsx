"use client";
import React from "react";
import { useSelector, useDispatch } from "react-redux";
import { Header } from "./Header";
import { Footer } from "./Footer";
import { RootState, AppDispatch } from "../../store/store";
import { logout } from "../../store/authSlice";
import { Loading } from "./Loading";

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const {
    isAuthenticated,
    user,
    isLoading: isAuthLoading,
  } = useSelector((state: RootState) => state.auth);
  const dispatch = useDispatch<AppDispatch>();

  const handleLogout = () => {
    dispatch(logout());
  };

  if (isAuthLoading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loading size="lg" />
      </div>
    );
  }

  return (
    <div className="flex flex-col min-h-screen">
      <Header
        isAuthenticated={isAuthenticated}
        username={user?.username}
        onLogout={handleLogout}
      />
      <main className="flex-grow bg-gray-50 py-6">
        <div className="container mx-auto px-4">{children}</div>
      </main>
      <Footer />
    </div>
  );
};
