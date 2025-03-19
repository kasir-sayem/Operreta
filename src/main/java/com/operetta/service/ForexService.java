package com.operetta.service;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.pricing.PricingGetRequest;
import com.oanda.v20.pricing.PricingGetResponse;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.instrument.InstrumentCandlesResponse;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeSpecifier;
import com.operetta.controller.ForexController.CandleData;
import com.operetta.controller.ForexController.PositionData;
import com.operetta.util.Config;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ForexService {
    private final Context context;
    private final AccountID accountId;
    
    public ForexService() {
        // Initialize the Oanda API context
        context = new ContextBuilder(Config.URL)
                .setToken(Config.TOKEN)
                .setApplication("OperettaForexApp")
                .build();
        
        accountId = Config.ACCOUNT_ID;
    }
}
    
    /**
     * Get account information and return it as a map
     * @return Map of account properties and values
     * @throws Exception if API call fails
     */
    public Map<String, String> getAccountInformation() throws Exception {
        Map<String, String> accountInfo = new LinkedHashMap<>(); // LinkedHashMap to maintain order
        
        AccountSummary summary = context.account.summary(accountId).getAccount();
        
        // Add account details to the map
        accountInfo.put("Account ID", summary.getId().toString());
        accountInfo.put("Name", summary.getAlias());
        accountInfo.put("Currency", summary.getCurrency().toString());
        accountInfo.put("Balance", formatNumber(summary.getBalance()));
        accountInfo.put("Unrealized P/L", formatNumber(summary.getUnrealizedPL()));
        accountInfo.put("Realized P/L", formatNumber(summary.getPL()));
        accountInfo.put("Margin Used", formatNumber(summary.getMarginUsed()));
        accountInfo.put("Margin Available", formatNumber(summary.getMarginAvailable()));
        accountInfo.put("Open Trade Count", String.valueOf(summary.getOpenTradeCount()));
        accountInfo.put("Open Position Count", String.valueOf(summary.getOpenPositionCount()));
        accountInfo.put("Created Time", formatDateTime(summary.getCreatedTime().toString()));
        
        return accountInfo;
    }
    
    /**
     * Get current price for a specific instrument
     * @param instrument the currency pair (e.g., "EUR_USD")
     * @return Formatted price information
     * @throws Exception if API call fails
     */
    public String getCurrentPrice(String instrument) throws Exception {
        List<String> instruments = Collections.singletonList(instrument);
        PricingGetRequest request = new PricingGetRequest(accountId, instruments);
        
        PricingGetResponse response = context.pricing.get(request);
        List<ClientPrice> prices = response.getPrices();
        
        if (prices.isEmpty()) {
            return "No price data available for " + instrument;
        }
        
        ClientPrice price = prices.get(0);
        StringBuilder result = new StringBuilder();
        
        result.append("Instrument: ").append(price.getInstrument()).append("\n");
        result.append("Time: ").append(formatDateTime(price.getTime().toString())).append("\n");
        
        // Bid prices (best available)
        result.append("Bid Price: ").append(formatNumber(price.getCloseoutBid())).append("\n");
        
        // Ask prices (best available)
        result.append("Ask Price: ").append(formatNumber(price.getCloseoutAsk())).append("\n");
        
        // Spread
        double spread = price.getCloseoutAsk() - price.getCloseoutBid();
        result.append("Spread: ").append(formatNumber(spread));
        
        return result.toString();
    }
    
    /**
     * Get historical prices for a specific instrument
     * @param instrument the currency pair (e.g., "EUR_USD")
     * @param from start date in ISO format (yyyy-MM-dd)
     * @param to end date in ISO format (yyyy-MM-dd)
     * @return List of candle data
     * @throws Exception if API call fails
     */
    public List<CandleData> getHistoricalPrices(String instrument, String from, String to) throws Exception {
        InstrumentCandlesRequest request = new InstrumentCandlesRequest(new InstrumentName(instrument));
        
        // Set granularity to hourly (H1)
        request.setGranularity(CandlestickGranularity.H1);
        
        // Convert dates to proper format and set time bounds
        LocalDate fromDate = LocalDate.parse(from);
        LocalDate toDate = LocalDate.parse(to);
        
        request.setFrom(fromDate.atStartOfDay().toString() + "Z");
        request.setTo(toDate.plusDays(1).atStartOfDay().toString() + "Z");
        
        InstrumentCandlesResponse response = context.instrument.candles(request);
        List<Candlestick> candles = response.getCandles();
        
        List<CandleData> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (Candlestick candle : candles) {
            // Parse the timestamp
            String timestamp = candle.getTime().toString();
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                    Instant.parse(timestamp), 
                    ZoneId.systemDefault()
            );
            String formattedDate = dateTime.format(dateFormatter);
            
            // Add to result list
            result.add(new CandleData(
                    formattedDate,
                    candle.getMid().getO(),
                    candle.getMid().getH(),
                    candle.getMid().getL(),
                    candle.getMid().getC()
            ));
        }
        
        return result;
    }
    
    /**
     * Open a position for a specific instrument
     * @param instrument the currency pair (e.g., "EUR_USD")
     * @param units the number of units (positive for buy, negative for sell)
     * @return Result message with the trade details
     * @throws Exception if API call fails
     */
    public String openPosition(String instrument, int units) throws Exception {
        OrderCreateRequest request = new OrderCreateRequest(accountId);
        
        MarketOrderRequest marketOrderRequest = new MarketOrderRequest();
        marketOrderRequest.setInstrument(new InstrumentName(instrument));
        marketOrderRequest.setUnits(units);
        
        request.setOrder(marketOrderRequest);
        OrderCreateResponse response = context.order.create(request);
        
        StringBuilder result = new StringBuilder();
        result.append("Position opened successfully!\n\n");
        result.append("Trade ID: ").append(response.getOrderFillTransaction().getId()).append("\n");
        result.append("Instrument: ").append(instrument).append("\n");
        result.append("Units: ").append(units).append("\n");
        result.append("Price: ").append(formatNumber(response.getOrderFillTransaction().getPrice())).append("\n");
        result.append("Time: ").append(formatDateTime(response.getOrderFillTransaction().getTime().toString()));
        
        return result.toString();
    }
    
    /**
     * Close a position with a specific ID
     * @param tradeId the ID of the trade to close
     * @return Result message
     * @throws Exception if API call fails
     */
    public String closePosition(String tradeId) throws Exception {
        TradeCloseRequest request = new TradeCloseRequest(accountId, new TradeSpecifier(tradeId));
        context.trade.close(request);
        
        return "Position with ID " + tradeId + " has been closed successfully.";
    }
    
    /**
     * Get all open positions
     * @return List of position data
     * @throws Exception if API call fails
     */
    public List<PositionData> getOpenPositions() throws Exception {
        List<Trade> trades = context.trade.listOpen(accountId).getTrades();
        List<PositionData> result = new ArrayList<>();
        
        for (Trade trade : trades) {
            result.add(new PositionData(
                    trade.getId(),
                    trade.getInstrument().toString(),
                    trade.getCurrentUnits(),
                    trade.getPrice(),
                    trade.getUnrealizedPL(),
                    formatDateTime(trade.getOpenTime().toString())
            ));
        }
        
        return result;
    }
    
    /**
     * Format a number as a string with 5 decimal places
     * @param number the number to format
     * @return Formatted number string
     */
    private String formatNumber(double number) {
        DecimalFormat df = new DecimalFormat("#0.00000");
        return df.format(number);
    }
    
    /**
     * Format a datetime string from ISO format to a more readable format
     * @param isoDateTime ISO datetime string
     * @return Formatted datetime string
     */
    private String formatDateTime(String isoDateTime) {
        try {
            Instant instant = Instant.parse(isoDateTime);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            // Return original if parsing fails
            return isoDateTime;
        }
    }