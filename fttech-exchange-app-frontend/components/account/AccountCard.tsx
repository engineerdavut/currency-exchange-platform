import React from "react";
import { WalletItem } from "../../types/account";
import { Button } from "../common/Button";
import { Card } from "../common/Card";
import { formatCurrency } from "../../utils/formatCurrency";

interface AccountCardProps {
  account: WalletItem;
  onDeposit: (currencyType: string) => void;
  onWithdraw: (currencyType: string) => void;
}

export const AccountCard: React.FC<AccountCardProps> = ({
  account,
  onDeposit,
  onWithdraw,
}) => {
  return (
    <Card className="hover:shadow-lg transition-shadow">
      <div className="flex flex-col h-full">
        <div className="mb-4">
          <h3 className="text-xl font-semibold text-blue-600">
            {account.currencyType}
          </h3>
          <p className="text-gray-700 mt-2">
            <span className="font-medium">Balance:</span>{" "}
            {formatCurrency(account.balance, account.currencyType)}
          </p>
        </div>

        <div className="mt-auto flex space-x-2">
          <Button
            variant="success"
            onClick={() => onDeposit(account.currencyType)}
            className="flex-1"
          >
            Deposit
          </Button>
          <Button
            variant="danger"
            onClick={() => onWithdraw(account.currencyType)}
            className="flex-1"
          >
            Withdraw
          </Button>
        </div>
      </div>
    </Card>
  );
};
