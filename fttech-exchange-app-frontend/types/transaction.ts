export interface Transaction {
    transactionId: number;
    accountId: number;
    currencyType: string;
    timestamp: string;
    amount: number;
    description: string;
    transactionType: string;
    fromCurrency?: string;
    toCurrency?: string;
  }
  
  export interface TransactionHistoryRequest {
    username: string;
    currencyType: string;
    page?: number;
    limit?: number;
  }
  