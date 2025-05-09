// components/exchange/ExchangeResult.tsx
import React from 'react';
import { formatCurrency } from '../../utils/formatCurrency';
import { formatDate } from '../../utils/formatDate';

interface ExchangeResultProps {
  result: {
    status: string;
    message: string;
    executedPrice?: number;
    timestamp?: string;
    fromAmount?: number;
    fromCurrency?: string;
    toAmount?: number;
    toCurrency?: string;
  };
  className?: string;
}

export const ExchangeResult: React.FC<ExchangeResultProps> = ({ result, className = '' }) => {
  const statusColor = result.status === 'SUCCESS' ? 'text-green-600' : 'text-red-600';
  
  return (
    <div className={`p-4 bg-gray-50 rounded-lg border ${className}`}>
      <h3 className="font-bold text-lg mb-2">Transaction Result</h3>
      
      <div className="space-y-2">
        <div className="flex justify-between">
          <span className="text-gray-600">Status:</span>
          <span className={`font-medium ${statusColor}`}>{result.status}</span>
        </div>
        
        <div className="flex justify-between">
          <span className="text-gray-600">Message:</span>
          <span className="font-medium">{result.message}</span>
        </div>
        
        {result.executedPrice && (
          <div className="flex justify-between">
            <span className="text-gray-600">Exchange Rate:</span>
            <span className="font-medium">{result.executedPrice.toFixed(4)}</span>
          </div>
        )}
        
        {result.fromAmount && result.fromCurrency && (
          <div className="flex justify-between">
            <span className="text-gray-600">Spent:</span>
            <span className="font-medium">{formatCurrency(result.fromAmount, result.fromCurrency)}</span>
          </div>
        )}
        
        {result.toAmount && result.toCurrency && (
          <div className="flex justify-between">
            <span className="text-gray-600">Received:</span>
            <span className="font-medium">{formatCurrency(result.toAmount, result.toCurrency)}</span>
          </div>
        )}
        
        {result.timestamp && (
          <div className="flex justify-between">
            <span className="text-gray-600">Time:</span>
            <span className="font-medium">{formatDate(result.timestamp)}</span>
          </div>
        )}
      </div>
    </div>
  );
};

