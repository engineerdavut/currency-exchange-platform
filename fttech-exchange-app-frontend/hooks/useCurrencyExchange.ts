import { useState } from 'react';
import { exchangeApi } from '../lib/api';
import { ExchangeRequest, ExchangeResponse } from '../types/exchange';
import axios from 'axios';

export const useCurrencyExchange = () => {
  const [result, setResult] = useState<ExchangeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const executeExchange = async (
    formData: {
      fromCurrency: string;
      toCurrency: string;
      amount: string;
      transactionType: string;
    },
    username: string | undefined
  ) => {
    setIsLoading(true);
    setError(null);
    setResult(null);

    if (!username) {
      setError('User not authenticated');
      setIsLoading(false);

      return null;
    }

    try {
      const { fromCurrency, toCurrency, amount, transactionType } = formData;

      const request: ExchangeRequest = {
        username,
        fromCurrency,
        toCurrency,
        amount: parseFloat(amount),
        transactionType,
      };

      const response = await exchangeApi.processExchange(request);
      setResult(response.data);
      return response.data as ExchangeResponse;
    } catch (err: unknown) {
      let errorMessage: string;
      if (axios.isAxiosError(err)) {
        errorMessage = err.response?.data?.message || err.message;
      } else if (err instanceof Error) {
        errorMessage = err.message;
      } else {
        errorMessage = 'Exchange failed. Please try again.';
      }
      setError(errorMessage);
      return { status: 'FAILED', message: errorMessage } as ExchangeResponse;
    } finally {
      setIsLoading(false);
    }
  };

  return {
    executeExchange,
    result,
    error,
    isLoading,
    clearResult: () => setResult(null),
    clearError: () => setError(null),
  };
};
