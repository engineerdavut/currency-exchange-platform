export interface ExchangeRequest {
    username: string;
    fromCurrency: string;
    toCurrency: string;
    amount: number;
    transactionType: string;
    accountId?: number;
  }
  
  export interface ExchangeResponse {
    status: string;
    message: string;
    executedPrice?: number;
    timestamp?: string;
  }
  
  export interface ExchangeRate {
    fromCurrency: string;
    toCurrency: string;
    rate: number;
    lastUpdated: string;
  }
  