package com.exchangeservice.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import java.math.BigDecimal;


import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExchangeRateAPIManager implements PriceManager {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateAPIManager.class);

    @Value("${api.exchangerate.key}")
    private String apiKey;
    @Value("${api.exchangerate.url}")
    private String apiUrl;

    @Override
    public BigDecimal[] getGoldPrices() throws Exception {
        throw new UnsupportedOperationException("Gold prices not supported by this provider");
    }

    @Override
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) throws Exception {
        try {
            HttpResponse<String> response = Unirest.get(apiUrl + apiKey + "/latest/" + fromCurrency)
                .asString();

            JSONObject json = new JSONObject(response.getBody());
            return json.getJSONObject("conversion_rates").getBigDecimal(toCurrency);
        } catch (Exception e) {
            logger.error("Error fetching exchange rate from ExchangeRate-API", e);
            throw new RuntimeException("Failed to fetch exchange rate");
        }
    }

    @Override
    public ExchangeRateInfo getExchangeRateInfo(String fromCurrency, String toCurrency) throws Exception {
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        if (fromCurrency.equals("USD") && toCurrency.equals("TRY") || 
            fromCurrency.equals("EUR") && toCurrency.equals("TRY") ||
            fromCurrency.equals("GOLD") && toCurrency.equals("TRY")) {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }
        else if (toCurrency.equals("USD") && fromCurrency.equals("TRY") || 
                 toCurrency.equals("EUR") && fromCurrency.equals("TRY") ||
                 toCurrency.equals("GOLD") && fromCurrency.equals("TRY")) {
            return new ExchangeRateInfo(getExchangeRate(toCurrency, fromCurrency), OperationType.DIVIDE);
        }else if ((fromCurrency.equals("USD") && toCurrency.equals("EUR")) || 
            (fromCurrency.equals("EUR") && toCurrency.equals("USD"))) {
            return new ExchangeRateInfo(rate, OperationType.MULTIPLY);
        }
        else {
            OperationType operation = rate.compareTo(BigDecimal.ONE) > 0 ? 
                OperationType.MULTIPLY : OperationType.DIVIDE;
            return new ExchangeRateInfo(rate, operation);
        }
    }
    
}