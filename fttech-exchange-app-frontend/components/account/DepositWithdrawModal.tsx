import React, { useState } from 'react';
import { Modal } from '../common/Modal';
import { Button } from '../common/Button';

interface DepositWithdrawModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (amount: string) => void;
  type: 'deposit' | 'withdraw';
  currencyType: string;
  isLoading: boolean;
}

export const DepositWithdrawModal: React.FC<DepositWithdrawModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  type,
  currencyType,
  isLoading
}) => {
  const [amount, setAmount] = useState('');

  const handleSubmit = () => {
    const numericAmount = Number(amount);
    if (!isFinite(numericAmount) || numericAmount <= 0) {
      return;
    }
    onSubmit(amount);
  };

  const resetAndClose = () => {
    setAmount('');
    onClose();
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={resetAndClose}
      title={`${type === 'deposit' ? 'Deposit' : 'Withdraw'} - ${currencyType}`}
      footer={
        <>
          <Button variant="primary" onClick={handleSubmit} isLoading={isLoading} className="mr-4">
            Confirm
          </Button>
          <Button variant="secondary" onClick={resetAndClose} disabled={isLoading}className="mr-4">
            Cancel
          </Button>
        </>
      }
    >
      <div className="mb-4">
        <label className="block text-gray-700 mb-2">Amount</label>
        <input
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          placeholder="Enter amount"
          className="w-full p-2 border rounded focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-gray-800"
          min="0.01"
          step="0.01"
        />
      </div>
    </Modal>
  );
};
