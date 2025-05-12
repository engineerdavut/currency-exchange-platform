package com.exchangeservice.manager;

import java.math.BigDecimal;

public interface PriceManager {
    BigDecimal[] getGoldPrices() throws Exception;
    BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws Exception;
    ExchangeRateInfo getExchangeRateInfo(String fromCurrency, String toCurrency) throws Exception;
}