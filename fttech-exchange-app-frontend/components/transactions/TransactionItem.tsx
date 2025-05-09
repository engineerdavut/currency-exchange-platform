// components/transactions/TransactionItem.tsx
import React from 'react';
import { formatDate } from '../../utils/formatDate';
import { formatCurrency } from '../../utils/formatCurrency';

interface TransactionItemProps {
  id: number;
  timestamp: string;
  amount: number;
  description: string;
  currencyType: string;
  transactionType: string;
  fromCurrency?: string;
  toCurrency?: string;
  className?: string;
}

export const TransactionItem: React.FC<TransactionItemProps> = ({
  timestamp,
  amount,
  description,
  currencyType,
  transactionType,
  fromCurrency,
  toCurrency,
  className = ''
}) => {
  const amountColor = transactionType === 'DEPOSIT' || transactionType === 'EXCHANGE_IN' 
    ? 'text-green-600' : 'text-red-600';
  
  const displayType = () => {
    if (transactionType === 'EXCHANGE') {
      return `Exchange: ${fromCurrency} → ${toCurrency}`;
    } else if (transactionType === 'EXCHANGE_IN') {
      return `Received from ${fromCurrency}`;
    } else if (transactionType === 'EXCHANGE_OUT') {
      return `Converted to ${toCurrency}`;
    } else {
      return transactionType.charAt(0).toUpperCase() + transactionType.slice(1).toLowerCase();
    }
  };
  
  return (
    <div className={`p-4 bg-white rounded-lg shadow border mb-3 ${className}`}>
      <div className="flex justify-between items-center">
        <div>
          <p className="text-sm text-gray-500">{formatDate(timestamp)}</p>
          <p className="font-medium text-gray-800">{description}</p>
          <span className="inline-block px-2 py-1 text-xs rounded bg-gray-100 text-gray-700 mt-1">
            {displayType()}
          </span>
        </div>
        <div className="text-right">
          <p className={`font-semibold ${amountColor}`}>
            {transactionType === 'DEPOSIT' || transactionType === 'EXCHANGE_IN' ? '+' : '-'}
            {formatCurrency(Math.abs(amount), currencyType)}
          </p>
          <p className="text-sm text-gray-500">{currencyType}</p>
        </div>
      </div>
    </div>
  );
};

export default TransactionItem;
