import React from "react";
import { Transaction } from "../../types/transaction";
import TransactionItem from "./TransactionItem";
import { Loading } from "../common/Loading";
import { ErrorMessage } from "../common/ErrorMessage";

interface TransactionListProps {
  transactions: Transaction[];
  isLoading?: boolean;
  error?: string | null;
  className?: string;
  showCurrencyFilter?: boolean;
}

export const TransactionList: React.FC<TransactionListProps> = ({
  transactions,
  isLoading = false,
  error = null,
  className = "",
  showCurrencyFilter = true,
}) => {
  const [selectedCurrency, setSelectedCurrency] = React.useState("ALL");
  if (isLoading) {
    return <Loading size="md" className="mx-auto my-8" />;
  }

  if (error) {
    return <ErrorMessage message={error} />;
  }

  if (transactions.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        No transactions found.
      </div>
    );
  }

  const filteredTransactions = transactions.filter((transaction) => {
    if (selectedCurrency === "ALL") return true;
    return transaction.currencyType === selectedCurrency;
  });

  const handleCurrencyChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedCurrency(e.target.value);
  };

  return (
    <div className={`space-y-3 ${className}`}>
      {showCurrencyFilter && (
        <div className="currency-filter">
          <label
            htmlFor="currencySelect"
            className="mr-2 font-medium text-gray-700"
          >
            Filter by Currency:
          </label>
          <select
            id="currencySelect"
            value={selectedCurrency}
            onChange={handleCurrencyChange}
            className="mt-1 block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md"
          >
            <option value="ALL">All Currencies</option>
            <option value="USD">USD</option>
            <option value="EUR">EUR</option>
            <option value="GBP">GBP</option>
          </select>
        </div>
      )}

      {filteredTransactions.map((transaction) => (
        <TransactionItem
          key={transaction.transactionId}
          id={transaction.transactionId}
          timestamp={transaction.timestamp}
          amount={transaction.amount}
          description={transaction.description}
          currencyType={transaction.currencyType}
          transactionType={transaction.transactionType}
          fromCurrency={transaction.fromCurrency}
          toCurrency={transaction.toCurrency}
        />
      ))}
    </div>
  );
};

export default TransactionList;
