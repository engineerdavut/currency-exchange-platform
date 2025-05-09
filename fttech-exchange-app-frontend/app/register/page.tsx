// app/register/page.tsx
"use client";
import React, { useState, useEffect, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { ErrorMessage } from "../../components/common/ErrorMessage";
import { useAuthActions } from "../../hooks/useAuth"; // Yeni hook

export default function RegisterPage() {
  const router = useRouter();
  const { register, isLoading, error: authError, resetAuthError } = useAuthActions();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [localError, setLocalError] = useState("");
  // const [success, setSuccess] = useState(""); // Başarı mesajı yerine hemen yönlendireceğiz

  useEffect(() => {
    // Component mount olduğunda önceki hataları temizle
    resetAuthError();
  }, [resetAuthError]);

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setLocalError("");
    // setSuccess(""); // Kaldırıldı
    resetAuthError(); // Önceki Redux auth hatalarını temizle

    // ... (form validasyonları aynı) ...
    if (!username || !password || !confirmPassword) {
      setLocalError("Please fill in all fields");
      return;
    }
    if (password !== confirmPassword) {
      setLocalError("Passwords do not match");
      return;
    }
    if (password.length < 6) {
      setLocalError("Password must be at least 6 characters long");
      return;
    }

    console.log("Calling register action...");
    const result = await register({username, password});
    console.log("Register action result:", result);

    if (result.success) {
      console.log("Registration successful, redirecting to /login immediately.");
      // Formu temizlemeye gerek yok, zaten yönleniyor
      // setUsername("");
      // setPassword("");
      // setConfirmPassword("");
      // setSuccess(result.message || "Registration successful! Redirecting to login..."); // Kaldırıldı
      router.push("/login"); // Hemen yönlendir
    } else {
      // Hata varsa göster
      setLocalError(result.error || "Registration failed. Please try again.");
    }
  };

  return (
    <div className="max-w-md mx-auto mt-10">
      <Card>
        <h2 className="text-2xl font-bold mb-6 text-center">Create an Account</h2>

        {/* Hata mesajlarını göster */}
        {(localError || authError) && <ErrorMessage message={localError || authError || ''} className="mb-4" />}
        {/* Başarı mesajı kaldırıldı */}
        {/* {success && ( ... )} */}

        <form onSubmit={handleRegister} className="space-y-4">
          {/* Inputlar aynı */}
           <div>
             <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">Username</label>
             <input id="username" type="text" value={username} onChange={(e) => setUsername(e.target.value)} className="w-full text-gray-800 p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required disabled={isLoading}/>
           </div>
           <div>
             <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">Password</label>
             <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full text-gray-800 p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required disabled={isLoading}/>
           </div>
           <div>
             <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
             <input id="confirmPassword" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="w-full text-gray-800 p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500" required disabled={isLoading}/>
           </div>
          <Button type="submit" variant="primary" className="w-full" isLoading={isLoading} disabled={isLoading}>
           {isLoading ? 'Registering...' : 'Register'}
          </Button>
        </form>

        <div className="mt-4 text-center">
          <p className="text-gray-600">
            Already have an account?{" "}
            <Link href="/login" className="text-blue-600 hover:text-blue-800">
              Login here
            </Link>
          </p>
        </div>
      </Card>
    </div>
  );
}
