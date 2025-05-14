"use client";
import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useSelector } from "react-redux";
import { Card } from "../../components/common/Card";
import { Button } from "../../components/common/Button";
import { ErrorMessage } from "../../components/common/ErrorMessage";
import { Loading } from "../../components/common/Loading";
import { accountApi } from "../../lib/api";
import { formatCurrency } from "../../utils/formatCurrency";
import { DepositWithdrawModal } from "@/components/account/DepositWithdrawModal";
import { RootState } from "../../store/store";
import { AxiosError } from "axios";

interface WalletItem {
  accountId: number;
  currencyType: string;
  balance: number;
}

export default function AccountPage() {
  const router = useRouter();
  const {
    user,
    isAuthenticated,
    isLoading: isAuthLoading,
  } = useSelector((state: RootState) => state.auth);
  const [wallet, setWallet] = useState<WalletItem[]>([]);
  const [isLoadingWallet, setIsLoadingWallet] = useState(true);
  const [error, setError] = useState("");
  const [modalOpen, setModalOpen] = useState(false);
  const [modalType, setModalType] = useState<"deposit" | "withdraw">("deposit");
  const [selectedCurrency, setSelectedCurrency] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!isAuthLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isAuthLoading, router]);

  useEffect(() => {
    if (isAuthenticated && !isAuthLoading) {
      const fetchWallet = async () => {
        setIsLoadingWallet(true);
        setError("");
        try {
          const response = await accountApi.getWallet();
          if (Array.isArray(response.data)) {
            setWallet(response.data);
          } else {
            setError("Invalid wallet data format");
            setWallet([]);
          }
        } catch (error: unknown) {
          const errorMessage =
            error instanceof Error
              ? error.message
              : "An unknown error occurred";
          setError(`Failed to fetch wallet data: ${errorMessage}`);
          setWallet([]);
        } finally {
          setIsLoadingWallet(false);
        }
      };

      fetchWallet();
    } else {
      setWallet([]);
      setIsLoadingWallet(false);
    }
  }, [isAuthenticated, isAuthLoading, router]);

  const openModal = (type: "deposit" | "withdraw", currency: string) => {
    setModalType(type);
    setSelectedCurrency(currency);
    setModalOpen(true);
  };

  const closeModal = () => {
    setModalOpen(false);
    setError("");
  };

  const handleModalSubmit = async (amountValue: string) => {
    if (!user?.username) {
      setError("User information is not available.");
      return;
    }
    const username = user.username;

    const numericAmount = Number(amountValue);
    if (!isFinite(numericAmount) || numericAmount <= 0) {
      setError("Please enter a valid positive amount");
      return;
    }

    if (modalType === "withdraw") {
      const currentWallet = wallet.find(
        (w) => w.currencyType === selectedCurrency
      );
      if (!currentWallet || numericAmount > currentWallet.balance) {
        setError(
          `Insufficient balance. Your current balance is ${formatCurrency(
            currentWallet?.balance || 0,
            selectedCurrency
          )}`
        );
        return;
      }
    }

    const payload = {
      username,
      currencyType: selectedCurrency,
      amount: numericAmount,
      description: `${modalType} operation for ${username}`,
      transactionType: modalType,
    };
    setIsSubmitting(true);
    setError("");

    try {
      if (modalType === "deposit") {
        await accountApi.deposit(payload);
      } else {
        await accountApi.withdraw(payload);
      }
      closeModal();
      if (isAuthenticated && !isAuthLoading) {
        const response = await accountApi.getWallet();
        setWallet(response.data);
      }
    } catch (error: unknown) {
      let errorMessage = "Deposit/Withdraw failed. Please try again.";
      if (error instanceof AxiosError) {
        errorMessage = error.response?.data?.message || error.message;
      } else if (error instanceof Error) {
        errorMessage = error.message;
      }
      setError(`${modalType} failed: ${errorMessage}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isAuthLoading || !isAuthenticated) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <Loading size="lg" />
      </div>
    );
  }
  if (isLoadingWallet) {
    return (
      <div className="max-w-4xl mx-auto p-4">
        <h2 className="text-2xl font-bold mb-6 text-gray-800">
          Account Wallet
        </h2>
        <Loading />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto p-4">
      <h2 className="text-2xl font-bold mb-6 text-gray-800">Account Wallet</h2>

      {error && <ErrorMessage message={error} className="mb-4" />}

      {wallet.length > 0 ? (
        <div className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
          {wallet.map((acc) => (
            <Card
              key={acc.accountId}
              className="hover:shadow-lg transition-shadow"
            >
              <div className="flex flex-col h-full">
                <div className="mb-4">
                  <h3 className="text-xl font-semibold text-blue-600">
                    {acc.currencyType}
                  </h3>
                  <p className="text-gray-700 mt-2">
                    <span className="font-medium">Balance:</span>{" "}
                    {formatCurrency(acc.balance, acc.currencyType)}
                  </p>
                </div>

                <div className="mt-auto flex space-x-2">
                  <Button
                    variant="success"
                    onClick={() => openModal("deposit", acc.currencyType)}
                    className="flex-1"
                    disabled={isSubmitting}
                  >
                    Deposit
                  </Button>
                  <Button
                    variant="danger"
                    onClick={() => openModal("withdraw", acc.currencyType)}
                    className="flex-1"
                    disabled={isSubmitting}
                  >
                    Withdraw
                  </Button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      ) : (
        <div className="text-center p-8 bg-gray-50 rounded-lg">
          <p className="text-gray-600">No wallet information found.</p>
        </div>
      )}

      <DepositWithdrawModal
        isOpen={modalOpen}
        onClose={closeModal}
        onSubmit={handleModalSubmit}
        type={modalType}
        currencyType={selectedCurrency}
        isLoading={isSubmitting}
      />
    </div>
  );
}
