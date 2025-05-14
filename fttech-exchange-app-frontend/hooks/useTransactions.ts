import { useState, useEffect } from 'react';
import { Transaction } from '../types/transaction';
import { transactionApi } from '../lib/api';


interface ApiTransactionResponse {
  transactionId: string;
  accountId: string;
  amount: number;
  description: string;
  currencyType: string;
  timestamp: string;
  transactionType: string;
  fromCurrency?: string;
  toCurrency?: string;
}

export const useTransactions = (currencyType?: string) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchTransactions = async () => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await transactionApi.getTransactions(currencyType);

        const formattedTransactions: Transaction[] = response.data.map((apiTransaction: ApiTransactionResponse) => {
          const transactionId = parseInt(apiTransaction.transactionId, 10);
          const accountId = parseInt(apiTransaction.accountId, 10);

          let transaction: Transaction = {
            ...apiTransaction,
            transactionId,
            accountId,
            fromCurrency: undefined,
            toCurrency: undefined
          };

          if (apiTransaction.description.includes('Exchange')) {
            const isIncoming = apiTransaction.amount > 0;

            const match = apiTransaction.description.match(/from (\w+) to (\w+)/i);
            const fromCurrency = match ? match[1] : undefined;
            const toCurrency = match ? match[2] : undefined;

            transaction = {
              ...transaction,
              transactionType: isIncoming ? 'EXCHANGE_IN' : 'EXCHANGE_OUT',
              fromCurrency,
              toCurrency
            };
          }

          return transaction;
        });

        setTransactions(formattedTransactions);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch transactions');
      } finally {
        setIsLoading(false);
      }
    };

    fetchTransactions();
  }, [currencyType]);

  return { transactions, isLoading, error };
};

