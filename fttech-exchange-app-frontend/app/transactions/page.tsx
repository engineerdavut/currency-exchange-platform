"use client";
import React, { useState, useEffect } from "react";
import { Card } from "../../components/common/Card";
import { ErrorMessage } from "../../components/common/ErrorMessage";
import { Loading } from "../../components/common/Loading";
import { TransactionList } from "../../components/transactions/TransactionList";
import { useTransactions } from "../../hooks/useTransactions";
import { CurrencyType } from "../../types/account";
import { Transaction } from "../../types/transaction";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { RootState } from "../../store/store";

export default function TransactionsPage() {
  const router = useRouter();
  const { isAuthenticated, isLoading: isAuthLoading } = useSelector(
    (state: RootState) => state.auth
  );
  const [currencyFilter, setCurrencyFilter] = useState<CurrencyType | "ALL">(
    "ALL"
  );
  const {
    transactions: allTransactions,
    isLoading: isLoadingTransactions,
    error: transactionsError,
  } = useTransactions();
  const [filteredTransactions, setFilteredTransactions] = useState<
    Transaction[]
  >([]);

  useEffect(() => {
    if (!isAuthLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isAuthLoading, router]);

  useEffect(() => {
    if (!isLoadingTransactions) {
      if (currencyFilter === "ALL") {
        setFilteredTransactions(allTransactions.slice(0, 5));
      } else {
        setFilteredTransactions(
          allTransactions
            .filter((t) => t.currencyType === currencyFilter)
            .slice(0, 5)
        );
      }
    } else {
      setFilteredTransactions([]);
    }
  }, [currencyFilter, allTransactions, isLoadingTransactions]);

  const handleCurrencyChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setCurrencyFilter(e.target.value as CurrencyType | "ALL");
  };

  if (isAuthLoading || (!isAuthenticated && !isAuthLoading)) {
    return (
      <div className="flex justify-center items-center h-64">
        <Loading size="lg" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      <Card className="mb-6">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4 sm:mb-0">
            Transaction History
          </h2>

          <div className="flex items-center">
            <label htmlFor="currencyFilter" className="mr-2 text-gray-700">
              Filter by Currency:
            </label>
            <select
              id="currencyFilter"
              className="border p-2 rounded bg-white text-gray-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={currencyFilter}
              onChange={handleCurrencyChange}
              disabled={isLoadingTransactions}
            >
              <option value="ALL">All Currencies</option>
              <option value={CurrencyType.TRY}>TRY</option>
              <option value={CurrencyType.USD}>USD</option>
              <option value={CurrencyType.EUR}>EUR</option>
              <option value={CurrencyType.GOLD}>GOLD</option>
            </select>
          </div>
        </div>

        {transactionsError && (
          <ErrorMessage message={transactionsError} className="mb-4" />
        )}
        {isLoadingTransactions ? (
          <div className="py-8">
            <Loading size="lg" className="mx-auto" />
          </div>
        ) : filteredTransactions.length > 0 ? (
          <TransactionList
            transactions={filteredTransactions}
            showCurrencyFilter={false}
            isLoading={isLoadingTransactions}
          />
        ) : (
          <div className="text-center py-8 text-gray-500">
            {currencyFilter === "ALL"
              ? "No transactions found."
              : "No transactions found for this currency."}
          </div>
        )}
      </Card>
    </div>
  );
}
