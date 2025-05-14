"use client";
import React, { useEffect, useState } from "react";
import { exchangeApi, authApi } from "../../lib/api";
import ExchangeForm from "../../components/exchange/ExchangeForm";
import { ExchangeResult } from "../../components/exchange/ExchangeResult";
import { Card } from "../../components/common/Card";
import { ErrorMessage } from "../../components/common/ErrorMessage";
import { useSelector } from "react-redux";
import { Loading } from "../../components/common/Loading";
import { useRouter } from "next/navigation";
import { RootState } from "../../store/store";
import { AxiosError } from "axios";

interface ExchangeResultType {
  status: string;
  message: string;
  executedPrice?: number;
  timestamp?: string;
  fromAmount?: number;
  fromCurrency?: string;
  toAmount?: number;
  toCurrency?: string;
}

export default function ExchangePage() {
  const router = useRouter();
  const {
    user,
    isAuthenticated,
    isLoading: isAuthLoading,
  } = useSelector((state: RootState) => state.auth);
  const [result, setResult] = useState<ExchangeResultType | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isAuthLoading && !isAuthenticated) {
      console.log(
        "ExchangePage useEffect: Not authenticated or auth loading finished. Redirecting to login."
      );
      router.push("/login");
    }
  }, [isAuthenticated, isAuthLoading, router]);

  const handleExchange = async (formData: {
    fromCurrency: string;
    toCurrency: string;
    amount: string;
    transactionType: string;
  }) => {
    if (!user?.username) {
      setError("User not found in state. Cannot perform exchange.");
      return;
    }

    setIsLoading(true);
    setError(null);
    setResult(null);

    try {
      console.log("Attempting pre-exchange auth check...");
      await authApi.checkAuth();
      console.log(
        "Pre-exchange auth check successful (200 OK). Proceeding with exchange."
      );

      const requestData = {
        fromCurrency: formData.fromCurrency,
        toCurrency: formData.toCurrency,
        amount: parseFloat(formData.amount),
        transactionType: formData.transactionType,
      };

      console.log("Sending exchange request:", requestData);
      const response = await exchangeApi.processExchange(requestData);
      console.log("Exchange request successful:", response.data);
      setResult(response.data);
    } catch (err: unknown) {
      console.error("Error during handleExchange:", err);
      let errorMessage = "Operation failed. Please try again.";
      if (err instanceof AxiosError) {
        if (err.config?.url?.endsWith("/api/auth/check")) {
          errorMessage = `Authentication check failed before exchange: ${
            err.response?.status === 401
              ? "Unauthorized (401). Please login again."
              : err.response?.data?.message || err.message
          }`;
        } else if (err.config?.url?.endsWith("/api/exchange/process")) {
          errorMessage = `Exchange failed: ${
            err.response?.status === 401
              ? "Unauthorized (401)"
              : err.response?.data?.message || err.message
          }`;
        } else {
          errorMessage = err.response?.data?.message || err.message;
        }
      } else if (err instanceof Error) {
        errorMessage = err.message;
      }
      setError(errorMessage);
      setResult(null);
    } finally {
      setIsLoading(false);
    }
  };
  if (isAuthLoading || (!isAuthenticated && !isAuthLoading)) {
    console.log("ExchangePage rendering Loading state:", {
      isAuthLoading,
      isAuthenticated,
    });
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loading size="lg" />
      </div>
    );
  }

  console.log("ExchangePage rendering main content:", {
    user,
    isAuthenticated,
  });
  return (
    <div className="flex flex-col items-center justify-center w-full max-w-4xl mx-auto py-8">
      <Card className="w-full max-w-md">
        <h2 className="text-3xl font-bold mb-6 text-center text-blue-600">
          Currency & Gold Exchange
        </h2>

        {error && (
          <ErrorMessage message={error} className="mb-4 text-red-600" />
        )}

        <ExchangeForm onSubmit={handleExchange} isLoading={isLoading} />

        {result && (
          <ExchangeResult result={result} className="mt-6 text-gray-800" />
        )}
      </Card>
    </div>
  );
}
