import React from 'react';
import { WalletItem } from '../../types/account';
import { AccountCard } from './AccountCard';

interface WalletGridProps {
  wallet: WalletItem[];
  onDeposit: (currencyType: string) => void;
  onWithdraw: (currencyType: string) => void;
}

export const WalletGrid: React.FC<WalletGridProps> = ({ 
  wallet, 
  onDeposit, 
  onWithdraw 
}) => {
  if (wallet.length === 0) {
    return (
      <div className="text-center p-8 bg-gray-50 rounded-lg">
        <p className="text-gray-600">No wallet information found.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-6 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
      {wallet.map((account) => (
        <AccountCard
          key={account.accountId}
          account={account}
          onDeposit={onDeposit}
          onWithdraw={onWithdraw}
        />
      ))}
    </div>
  );
};
