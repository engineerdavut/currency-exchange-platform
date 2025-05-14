package com.exchangeservice.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@Profile("!test") 
public class CollectApiPriceManager {
    private static final Logger logger = LoggerFactory.getLogger(CollectApiPriceManager.class);
    @Value("${api.collectapi.key}")
    private String apiKey;
    @Value("${api.collectapi.url}")
    private String apiUrl;
    private final String API_KEY = "apikey "+apiKey;

    public BigDecimal[] fetchGoldPrice() {
        try {
            HttpResponse<String> response = Unirest.get(apiUrl+"/economy/goldPrice")
                .header("content-type", "application/json")
                .header("authorization", API_KEY)
                .asString();
    
            if (response.getStatus() != 200) {
                logger.error("Gold price API call failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch gold price: HTTP " + response.getStatus());
            }
    
            JSONObject jsonResponse = new JSONObject(response.getBody());
            if (!jsonResponse.getBoolean("success")) {
                logger.error("Gold price API returned unsuccessful response: {}", jsonResponse.toString());
                throw new RuntimeException("Gold price API unsuccessful");
            }
    
            JSONArray resultArray = jsonResponse.getJSONArray("result");
            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject item = resultArray.getJSONObject(i);
                if (item.getString("name").equals("Gram Altın")) {
                    BigDecimal buy;
                    BigDecimal sell;
                    
                    if (item.has("buying")) {
                        buy = new BigDecimal(item.getDouble("buying"));
                    } else if (item.has("buyingstr")) {
                        buy = new BigDecimal(item.getString("buyingstr"));
                    } else if (item.has("buy")) {
                        buy = new BigDecimal(item.getString("buy"));
                    } else {
                        buy = new BigDecimal("2000.00");
                        logger.warn("Buy price not found in API response, using default value: {}", buy);
                    }
                    
                    if (item.has("selling")) {
                        sell = new BigDecimal(item.getDouble("selling"));
                    } else if (item.has("sellingstr")) {
                        sell = new BigDecimal(item.getString("sellingstr"));
                    } else if (item.has("sell")) {
                        sell = new BigDecimal(item.getString("sell"));
                    } else {
                        sell = buy.multiply(new BigDecimal("1.02"));
                        logger.warn("Sell price not found in API response, using calculated value: {}", sell);
                    }
                    
                    logger.info("Fetched gold prices - Buy: {}, Sell: {}", buy, sell);
                    return new BigDecimal[]{buy, sell};
                }
            }
            
            BigDecimal defaultBuy = new BigDecimal("2000.00");
            BigDecimal defaultSell = new BigDecimal("2040.00");
            logger.warn("Gram Altın not found in response, using default values - Buy: {}, Sell: {}", defaultBuy, defaultSell);
            return new BigDecimal[]{defaultBuy, defaultSell};
        } catch (Exception e) {
            logger.error("Error fetching gold price", e);
            BigDecimal defaultBuy = new BigDecimal("2000.00");
            BigDecimal defaultSell = new BigDecimal("2040.00");
            logger.warn("Using default gold prices due to error - Buy: {}, Sell: {}", defaultBuy, defaultSell);
            return new BigDecimal[]{defaultBuy, defaultSell};
        }
    }
    

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        try {
            if (fromCurrency.equals("GOLD") || toCurrency.equals("GOLD")) {
                return handleGoldExchangeRate(fromCurrency, toCurrency);
            }
            
            BigDecimal directRate = fetchDirectExchangeRate(fromCurrency, toCurrency);
            BigDecimal inverseRate = null;
            
            try {
                BigDecimal fetchedInverseRate = fetchDirectExchangeRate(toCurrency, fromCurrency);

                inverseRate = BigDecimal.ONE.divide(fetchedInverseRate, 6, RoundingMode.HALF_UP);
                logger.info("Calculated inverse rate from {} to {}: {}", fromCurrency, toCurrency, inverseRate);
            } catch (Exception e) {
                logger.warn("Could not fetch inverse rate, will use direct rate only: {}", e.getMessage());
            }
            
            if (directRate != null && inverseRate != null) {
                if (directRate.compareTo(BigDecimal.ONE) > 0 && inverseRate.compareTo(BigDecimal.ONE) < 0) {
                    logger.info("Using direct rate {} as it's larger than 1", directRate);
                    return directRate.setScale(4, RoundingMode.HALF_UP);
                } else if (inverseRate.compareTo(BigDecimal.ONE) > 0 && directRate.compareTo(BigDecimal.ONE) < 0) {
                    logger.info("Using inverse rate {} as it's larger than 1", inverseRate);
                    return inverseRate.setScale(4, RoundingMode.HALF_UP);
                } else {
                    if (directRate.abs().compareTo(inverseRate.abs()) >= 0) {
                        logger.info("Using direct rate {} as it has larger absolute value", directRate);
                        return directRate.setScale(4, RoundingMode.HALF_UP);
                    } else {
                        logger.info("Using inverse rate {} as it has larger absolute value", inverseRate);
                        return inverseRate.setScale(4, RoundingMode.HALF_UP);
                    }
                }
            }
            if (directRate == null) {
                logger.error("Direct rate is null, cannot calculate exchange rate for {} to {}", fromCurrency, toCurrency);
                throw new RuntimeException("Failed to fetch the primary exchange rate.");
           }
           logger.info("Using direct rate {} as final rate", directRate);
            return directRate.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception e) {
            logger.error("Error calculating exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw new RuntimeException("Failed to calculate exchange rate: " + e.getMessage());
        }
    }
    
    private BigDecimal handleGoldExchangeRate(String fromCurrency, String toCurrency) {
        try {
            BigDecimal[] goldPrices = fetchGoldPrice();
            BigDecimal buyPrice = goldPrices[0]; 
            BigDecimal sellPrice = goldPrices[1]; 
            
            logger.info("Using gold prices - Buy: {}, Sell: {}", buyPrice, sellPrice);
            
            if (fromCurrency.equals("GOLD") && toCurrency.equals("TRY")) {
                return buyPrice.setScale(4, RoundingMode.HALF_UP);
            } else if (fromCurrency.equals("TRY") && toCurrency.equals("GOLD")) {
                return sellPrice.setScale(4, RoundingMode.HALF_UP);
            } else if (fromCurrency.equals("GOLD")) {
                BigDecimal goldToTry = buyPrice;
                BigDecimal tryToTarget = getExchangeRate("TRY", toCurrency);
                return goldToTry.divide(tryToTarget, 6, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
            } else {
                BigDecimal sourceToTry = getExchangeRate(fromCurrency, "TRY");
                return sourceToTry.divide(sellPrice, 6, RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            logger.error("Error handling gold exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw new RuntimeException("Failed to calculate gold exchange rate: " + e.getMessage());
        }
    }
    
    
    private BigDecimal fetchDirectExchangeRate(String fromCurrency, String toCurrency) {
        try {
            HttpResponse<String> response = Unirest.get(apiUrl+"/economy/exchange?base=" + fromCurrency + "&to=" + toCurrency)
                .header("content-type", "application/json")
                .header("authorization", API_KEY)
                .asString();

            if (response.getStatus() != 200) {
                logger.error("Exchange rate API call failed with status: {}", response.getStatus());
                throw new RuntimeException("Failed to fetch exchange rate: HTTP " + response.getStatus());
            }

            JSONObject jsonResponse = new JSONObject(response.getBody());
            if (!jsonResponse.getBoolean("success")) {
                logger.error("Exchange rate API returned unsuccessful response: {}", jsonResponse.toString());
                throw new RuntimeException("Exchange rate API unsuccessful");
            }

            JSONArray dataArray = jsonResponse.getJSONObject("result").getJSONArray("data");
            if (dataArray.length() == 0) {
                throw new RuntimeException("No exchange rate data returned");
            }
            
            BigDecimal rate = new BigDecimal(dataArray.getJSONObject(0).getString("rate"));
            logger.info("Fetched direct exchange rate {} to {}: {}", fromCurrency, toCurrency, rate);
            return rate;
        } catch (Exception e) {
            logger.error("Error fetching direct exchange rate from {} to {}", fromCurrency, toCurrency, e);
            throw e;
        }
    }
}
