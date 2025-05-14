"use client";
import React, { useState, FormEvent, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { ErrorMessage } from "../../components/common/ErrorMessage";
import { useAuthActions } from "../../hooks/useAuth";
import { RootState } from "../../store/store";

export default function LoginPage() {
  const {
    login,
    isLoading,
    error: authError,
    resetAuthError,
  } = useAuthActions();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [localError, setLocalError] = useState("");
  const router = useRouter();

  const isAuthenticated = useSelector(
    (state: RootState) => state.auth.isAuthenticated
  );

  useEffect(() => {
    resetAuthError();
  }, [resetAuthError]);

  useEffect(() => {
    console.log("Checking authentication state:", isAuthenticated);
    if (isAuthenticated) {
      console.log("Authenticated, redirecting to /account...");
      router.push("/account");
    }
  }, [isAuthenticated, router]);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setLocalError("");
    resetAuthError();

    if (!username || !password) {
      setLocalError("Please enter both username and password");
      return;
    }

    console.log("Dispatching login action...");
    try {
      await login({ username, password });
      console.log("Login action dispatched.");
    } catch (error) {
      console.error(
        "Error dispatching login (should not happen if thunk handles rejection):",
        error
      );
      setLocalError("An unexpected error occurred during login dispatch.");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10">
      <Card>
        <h2 className="text-2xl font-bold mb-6 text-center">
          Login to Your Account
        </h2>

        {(localError || authError) && (
          <ErrorMessage
            message={localError || authError || ""}
            className="mb-4"
          />
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label
              htmlFor="username"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Username
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full text-gray-800 p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
              disabled={isLoading}
            />
          </div>
          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-700 mb-1"
            >
              Password
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full text-gray-800 p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              required
              disabled={isLoading}
            />
          </div>
          <Button
            type="submit"
            variant="primary"
            className="w-full"
            isLoading={isLoading}
            disabled={isLoading}
          >
            {isLoading ? "Logging in..." : "Login"}
          </Button>
        </form>

        <div className="mt-4 text-center">
          <p className="text-gray-600">
            Don&apos;t have an account?{" "}
            <Link
              href="/register"
              className="text-blue-600 hover:text-blue-800"
            >
              Register here
            </Link>
          </p>
        </div>
      </Card>
    </div>
  );
}
