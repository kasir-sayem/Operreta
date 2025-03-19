package com.operetta.service;

import javax.xml.soap.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for connecting to the Hungarian National Bank SOAP web service
 */
public class SoapService {
    
    private static final String SOAP_URL = "http://www.mnb.hu/arfolyamok.asmx";
    private static final String NAMESPACE = "http://www.mnb.hu/webservices/";
    
    /**
     * Get all available currencies
     */
    public List<String> getCurrencies() throws Exception {
        String response = sendSoapRequest("GetCurrencies", null);
        return parseCurrenciesResponse(response);
    }
    
    /**
     * Get exchange rates for a specific date range and currency
     */
    public Map<String, Double> getExchangeRates(String currency, Date startDate, Date endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        Map<String, String> params = new HashMap<>();
        params.put("startDate", dateFormat.format(startDate));
        params.put("endDate", dateFormat.format(endDate));
        params.put("currencyNames", currency);
        
        String response = sendSoapRequest("GetExchangeRates", params);
        return parseExchangeRatesResponse(response, currency);
    }
    
    /**
     * Get current exchange rates for all currencies
     */
    public Map<String, Double> getCurrentExchangeRates() throws Exception {
        String response = sendSoapRequest("GetCurrentExchangeRates", null);
        return parseCurrentExchangeRatesResponse(response);
    }
    
    /**
     * Download all exchange rate data to a file
     */
    public void downloadAllData(String filePath) throws Exception {
        String response = sendSoapRequest("GetExchangeRates", null);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(response);
        }
    }
    
    /**
     * Download specific exchange rate data to a file based on parameters
     */
    public void downloadFilteredData(String filePath, String currency, Date startDate, Date endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        Map<String, String> params = new HashMap<>();
        params.put("startDate", dateFormat.format(startDate));
        params.put("endDate", dateFormat.format(endDate));
        params.put("currencyNames", currency);
        
        String response = sendSoapRequest("GetExchangeRates", params);
        
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(response);
        }
    }
    
    /**
     * Send a SOAP request to the MNB web service
     */
    private String sendSoapRequest(String methodName, Map<String, String> params) throws Exception {
        URL url = new URL(SOAP_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // Set up the connection
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestProperty("SOAPAction", NAMESPACE + methodName);
        connection.setDoOutput(true);
        
        // Create SOAP message
        String soapRequest = createSoapRequest(methodName, params);
        
        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapRequest.getBytes(StandardCharsets.UTF_8));
        }
        
        // Get the response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }
    
    /**
     * Create a SOAP request message
     */
    private String createSoapRequest(String methodName, Map<String, String> params) {
        StringBuilder soapRequest = new StringBuilder();
        
        // SOAP envelope
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        soapRequest.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
        soapRequest.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
        soapRequest.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
        
        // SOAP body
        soapRequest.append("<soap:Body>");
        soapRequest.append("<").append(methodName).append(" xmlns=\"").append(NAMESPACE).append("\">");
        
        // Add parameters if any
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                soapRequest.append("<").append(param.getKey()).append(">");
                soapRequest.append(param.getValue());
                soapRequest.append("</").append(param.getKey()).append(">");
            }
        }
        
        // Close tags
        soapRequest.append("</").append(methodName).append(">");
        soapRequest.append("</soap:Body>");
        soapRequest.append("</soap:Envelope>");
        
        return soapRequest.toString();
    }
    
    /**
     * Parse currencies from the SOAP response
     */
    private List<String> parseCurrenciesResponse(String response) {
        List<String> currencies = new ArrayList<>();
        
        // Simple XML parsing for currencies
        String startTag = "<GetCurrenciesResult>";
        String endTag = "</GetCurrenciesResult>";
        
        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);
        
        if (startIndex != -1 && endIndex != -1) {
            String result = response.substring(startIndex + startTag.length(), endIndex);
            
            // Parse the currency strings
            for (String currency : result.split(",")) {
                currencies.add(currency.trim());
            }
        }
        
        return currencies;
    }
    
    /**
     * Parse exchange rates from the SOAP response
     */
    private Map<String, Double> parseExchangeRatesResponse(String response, String currency) {
        Map<String, Double> exchangeRates = new HashMap<>();
        
        // Simple XML parsing for exchange rates
        String startTag = "<GetExchangeRatesResult>";
        String endTag = "</GetExchangeRatesResult>";
        
        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);
        
        if (startIndex != -1 && endIndex != -1) {
            String result = response.substring(startIndex + startTag.length(), endIndex);
            
            // Parse the result XML for exchange rates
            // This is a simplified parser for demonstration
            String[] lines = result.split("\\n");
            String currentDate = null;
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("<Day date=")) {
                    // Extract date
                    int dateStartIndex = line.indexOf("\"") + 1;
                    int dateEndIndex = line.indexOf("\"", dateStartIndex);
                    currentDate = line.substring(dateStartIndex, dateEndIndex);
                } else if (line.contains("<" + currency) && currentDate != null) {
                    // Extract rate
                    int rateStartIndex = line.indexOf(">") + 1;
                    int rateEndIndex = line.indexOf("<", rateStartIndex);
                    String rateStr = line.substring(rateStartIndex, rateEndIndex);
                    
                    try {
                        double rate = Double.parseDouble(rateStr.replace(",", "."));
                        exchangeRates.put(currentDate, rate);
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
        }
        
        return exchangeRates;
    }
    
    /**
     * Parse current exchange rates from the SOAP response
     */
    private Map<String, Double> parseCurrentExchangeRatesResponse(String response) {
        Map<String, Double> exchangeRates = new HashMap<>();
        
        // Simple XML parsing for current exchange rates
        String startTag = "<GetCurrentExchangeRatesResult>";
        String endTag = "</GetCurrentExchangeRatesResult>";
        
        int startIndex = response.indexOf(startTag);
        int endIndex = response.indexOf(endTag);
        
        if (startIndex != -1 && endIndex != -1) {
            String result = response.substring(startIndex + startTag.length(), endIndex);
            
            // Parse the result XML for exchange rates
            // This is a simplified parser for demonstration
            String[] lines = result.split("\\n");
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.startsWith("<Rate")) {
                    // Extract currency
                    int currStartIndex = line.indexOf("curr=\"") + 6;
                    int currEndIndex = line.indexOf("\"", currStartIndex);
                    String currency = line.substring(currStartIndex, currEndIndex);
                    
                    // Extract rate
                    int rateStartIndex = line.indexOf(">") + 1;
                    int rateEndIndex = line.indexOf("<", rateStartIndex);
                    String rateStr = line.substring(rateStartIndex, rateEndIndex);
                    
                    try {
                        double rate = Double.parseDouble(rateStr.replace(",", "."));
                        exchangeRates.put(currency, rate);
                    } catch (NumberFormatException e) {
                        // Skip invalid numbers
                    }
                }
            }
        }
        
        return exchangeRates;
    }
}