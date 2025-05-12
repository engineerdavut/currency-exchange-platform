// components/exchange/ExchangeForm.tsx
import React, { useState } from 'react';
import { Button } from '../common/Button';

interface ExchangeFormProps {
  onSubmit: (data: {
    fromCurrency: string;
    toCurrency: string;
    amount: string;
    transactionType: string;
  }) => void;
  isLoading: boolean;
}

export default function ExchangeForm({ onSubmit, isLoading }: ExchangeFormProps) {
  const [fromCurrency, setFromCurrency] = useState('TRY');
  const [toCurrency, setToCurrency] = useState('USD');
  const [amount, setAmount] = useState('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({
      fromCurrency,
      toCurrency,
      amount,
      transactionType:'BUY',
    });
  };

  // Swap currencies function
  const handleSwapCurrencies = () => {
    setFromCurrency(toCurrency);
    setToCurrency(fromCurrency);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="flex flex-col space-y-4 md:flex-row md:space-y-0 md:space-x-4">
        <div className="flex-1">
          <label htmlFor="fromCurrencySelect" className="block mb-1 text-gray-700 font-medium">From</label>
          <select 
            id="fromCurrencySelect"
            className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-800" 
            value={fromCurrency} 
            onChange={(e) => setFromCurrency(e.target.value)}
            >
            <option value="TRY">Turkish Lira (TRY)</option>
            <option value="USD">US Dollar (USD)</option>
            <option value="EUR">Euro (EUR)</option>
            <option value="GOLD">Gold (Gram)</option>
          </select>
        </div>
        
        <div className="flex items-center justify-center md:pt-6">
          <button 
            type="button"
            onClick={handleSwapCurrencies}
            className="p-2 rounded-full hover:bg-gray-100 transition-colors"
            aria-label="Swap currencies"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
            </svg>
          </button>
        </div>
        
        <div className="flex-1">
          <label htmlFor="toCurrencySelect" className="block mb-1 text-gray-700 font-medium">To</label>
          <select 
            id="toCurrencySelect"
            className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-800" 
            value={toCurrency} 
            onChange={(e) => setToCurrency(e.target.value)}
          >
            <option value="TRY">Turkish Lira (TRY)</option>
            <option value="USD">US Dollar (USD)</option>
            <option value="EUR">Euro (EUR)</option>
            <option value="GOLD">Gold (Gram)</option>
          </select>
        </div>
      </div>
      
      <div>
        <label className="block mb-1 text-gray-700 font-medium">Amount</label>
        <input 
            type="number" 
            className="border p-2 rounded w-full focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-800" 
            value={amount} 
            onChange={(e) => setAmount(e.target.value)}
            min="0.01"
            step="0.01"
            placeholder="Enter amount"
            required
          />
      </div>
      
      <Button
        type="submit"
        variant="primary"
        className="w-full"
        isLoading={isLoading}
        disabled={isLoading || !amount}
      >
        {isLoading ? 'Processing...' : 'Execute Transaction'}
      </Button>
    </form>
  );
}
