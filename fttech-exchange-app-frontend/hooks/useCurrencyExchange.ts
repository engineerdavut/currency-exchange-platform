// src/hooks/useCurrencyExchange.ts (Güncellenmiş)
import { useState } from 'react';
import { exchangeApi } from '../lib/api'; // Doğru import
import { ExchangeRequest, ExchangeResponse } from '../types/exchange'; // Doğru import
// import { getUsername } from '../utils/tokenUtils'; // KALDIRILDI

export const useCurrencyExchange = () => {
  const [result, setResult] = useState<ExchangeResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  // executeExchange artık username'i parametre olarak alıyor
  const executeExchange = async (
    formData: {
      fromCurrency: string;
      toCurrency: string;
      amount: string;
      transactionType: string;
    },
    username: string | undefined // Username parametresi eklendi
  ) => {
    setIsLoading(true);
    setError(null);
    setResult(null); // Önceki sonucu temizle

    if (!username) { // Username kontrolü
      setError('User not authenticated');
      setIsLoading(false);
      // Return null or throw error, depending on how you want to handle in the component
      return null; // Veya bir hata objesi döndür
    }

    try {
      const { fromCurrency, toCurrency, amount, transactionType } = formData;

      const request: ExchangeRequest = { // Tam ExchangeRequest tipini kullan
        username, // Parametreden gelen username
        fromCurrency,
        toCurrency,
        amount: parseFloat(amount), // amount'u number yap
        transactionType,
      };

      const response = await exchangeApi.processExchange(request); // API çağrısı
      setResult(response.data);
      return response.data as ExchangeResponse; // Başarılı sonucu döndür
    } catch (err: any) {
      const errorMessage = err.response?.data?.message || err.message || 'Exchange failed. Please try again.';
      setError(errorMessage);
      // Return null or an error object
      return { status: 'FAILED', message: errorMessage } as ExchangeResponse; // Hata objesi döndür
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
