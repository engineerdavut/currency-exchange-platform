export interface WalletItem {
    accountId: number;
    currencyType: string;
    balance: number;
  }
  
  export interface AccountBalance {
    id: number;
    userId: number;
    currencyType: CurrencyType;
    balance: number;
  }
  
  export enum CurrencyType {
    TRY = "TRY",
    USD = "USD",
    EUR = "EUR",
    GOLD = "GOLD"
  }
  
  export interface DepositWithdrawRequest {
    username: string;
    currencyType: string;
    amount: number;
    description: string;
    transactionType: "deposit" | "withdraw";
  }
  