package com.operetta.service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for connecting to the Hungarian National Bank SOAP web service
 * Implements all six methods as documented by MNB
 */
public class SoapService {
    
    // The official MNB arfolyam service endpoint
    private static final String SOAP_URL = "http://www.mnb.hu/arfolyamok.asmx";
    private static final String NAMESPACE = "http://www.mnb.hu/webservices/";
    
    /**
     * Get information about queriable data
     * Implements GetInfo method from MNB documentation
     */
    public Map<String, Object> getInfo() throws Exception {
        String response = sendSoapRequest("GetInfo", null);
        return parseInfoResponse(response);
    }
    
    /**
     * Get a list of all available currencies
     * Implements GetCurrencies method from MNB documentation
     */
    public List<String> getCurrencies() throws Exception {
        String response = sendSoapRequest("GetCurrencies", null);
        return parseCurrenciesResponse(response);
    }
    
    /**
     * Get currency units for specified currencies
     * Implements GetCurrencyUnits method from MNB documentation
     */
    public Map<String, Integer> getCurrencyUnits(String currencyNames) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("currencyNames", currencyNames);
        
        String response = sendSoapRequest("GetCurrencyUnits", params);
        return parseCurrencyUnitsResponse(response);
    }
    
    /**
     * Get current exchange rates (most recent daily quote)
     * Implements GetCurrentExchangeRates method from MNB documentation
     */
    public Map<String, Double> getCurrentExchangeRates() throws Exception {
        String response = sendSoapRequest("GetCurrentExchangeRates", null);
        return parseCurrentExchangeRatesResponse(response);
    }
    
    /**
     * Get the date interval covered by the exchange rate database
     * Implements GetDateInterval method from MNB documentation
     */
    public Map<String, String> getDateInterval() throws Exception {
        String response = sendSoapRequest("GetDateInterval", null);
        return parseDateIntervalResponse(response);
    }
    
    /**
     * Get exchange rates for specific currencies within a date range
     * Implements GetExchangeRates method from MNB documentation
     */
    public Map<String, Map<String, Double>> getExchangeRates(String startDate, String endDate, String currencyNames) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("currencyNames", currencyNames);
        
        String response = sendSoapRequest("GetExchangeRates", params);
        return parseExchangeRatesResponse(response);
    }
    
    /**
     * Get exchange rates for a single currency within a date range (simplified version)
     */
    public Map<String, Double> getExchangeRates(String currency, Date startDate, Date endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedStartDate = dateFormat.format(startDate);
        String formattedEndDate = dateFormat.format(endDate);
        
        Map<String, Map<String, Double>> allRates = getExchangeRates(formattedStartDate, formattedEndDate, currency);
        
        // Extract rates for the specified currency
        Map<String, Double> currencyRates = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Double>> entry : allRates.entrySet()) {
            String date = entry.getKey();
            Map<String, Double> rates = entry.getValue();
            
            if (rates.containsKey(currency)) {
                currencyRates.put(date, rates.get(currency));
            }
        }
        
        return currencyRates;
    }
    
    /**
     * Download all exchange rate data to a file
     */
    public void downloadAllData(String filePath) throws Exception {
        // Get date interval
        Map<String, String> dateInterval = getDateInterval();
        String startDate = dateInterval.getOrDefault("startDate", "2000-01-01");
        String endDate = dateInterval.getOrDefault("endDate", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        
        // Get currencies
        List<String> currencies = getCurrencies();
        String currencyList = String.join(",", currencies);
        
        // Get exchange rates for all currencies and dates
        Map<String, String> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("currencyNames", currencyList);
        
        String response = sendSoapRequest("GetExchangeRates", params);
        
        // Create directory if it doesn't exist
        File directory = new File(filePath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
        
        // Write the response to file with correct UTF-8 encoding
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(response);
        }
    }
    
    /**
     * Download specific exchange rate data to a file based on parameters
     */
    public void downloadFilteredData(String filePath, String currency, Date startDate, Date endDate) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedStartDate = dateFormat.format(startDate);
        String formattedEndDate = dateFormat.format(endDate);
        
        Map<String, String> params = new HashMap<>();
        params.put("startDate", formattedStartDate);
        params.put("endDate", formattedEndDate);
        params.put("currencyNames", currency);
        
        String response = sendSoapRequest("GetExchangeRates", params);
        
        // Create directory if it doesn't exist
        File directory = new File(filePath).getParentFile();
        if (directory != null && !directory.exists()) {
            directory.mkdirs();
        }
        
        // Write the response to file with correct UTF-8 encoding
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
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
        
        // For debugging
        System.out.println("Sending request to: " + SOAP_URL);
        System.out.println("SOAPAction: " + NAMESPACE + methodName);
        System.out.println("Request body: " + soapRequest);
        
        // Send the request
        try (OutputStream os = connection.getOutputStream()) {
            os.write(soapRequest.getBytes(StandardCharsets.UTF_8));
        }
        
        // Check response code
        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            // Read error stream if available
            InputStream errorStream = connection.getErrorStream();
            String errorResponse = "";
            if (errorStream != null) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(errorStream, StandardCharsets.UTF_8))) {
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    errorResponse = sb.toString();
                }
            }
            throw new IOException("HTTP error code: " + responseCode + ", Error: " + errorResponse);
        }
        
        // Get the response with UTF-8 encoding
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
            }
        }
        
        // For debugging
        System.out.println("Response: " + response.toString());
        
        return response.toString();
    }
    
    /**
     * Create a SOAP request message according to MNB's SOAP specification
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
     * Parse the GetInfo response
     * Format as shown in documentation:
     * <MNBExchangeRatesQueryValues>
     *   <FirstDate>1949-01-03</FirstDate>
     *   <LastDate>2015-07-23</LastDate>
     *   <Currencies>
     *     <Curr>HUF</Curr>
     *     <Curr>EUR</Curr>
     *     ... more currencies ...
     *   </Currencies>
     * </MNBExchangeRatesQueryValues>
     */
    private Map<String, Object> parseInfoResponse(String response) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetInfoResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document infoDoc = parseXmlResponse(resultXml);
                
                // Extract dates
                NodeList firstDateNodes = infoDoc.getElementsByTagName("FirstDate");
                NodeList lastDateNodes = infoDoc.getElementsByTagName("LastDate");
                
                if (firstDateNodes.getLength() > 0) {
                    result.put("firstDate", firstDateNodes.item(0).getTextContent());
                }
                
                if (lastDateNodes.getLength() > 0) {
                    result.put("lastDate", lastDateNodes.item(0).getTextContent());
                }
                
                // Extract currencies
                List<String> currencies = new ArrayList<>();
                NodeList currNodes = infoDoc.getElementsByTagName("Curr");
                
                for (int i = 0; i < currNodes.getLength(); i++) {
                    currencies.add(currNodes.item(i).getTextContent());
                }
                
                result.put("currencies", currencies);
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetInfo response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * Parse the GetCurrencies response
     * Format as shown in documentation:
     * <MNBCurrencies>
     *   <Currencies>
     *     <Curr>HUF</Curr>
     *     <Curr>EUR</Curr>
     *     ... more currencies ...
     *   </Currencies>
     * </MNBCurrencies>
     */
    private List<String> parseCurrenciesResponse(String response) {
        List<String> currencies = new ArrayList<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetCurrenciesResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document currenciesDoc = parseXmlResponse(resultXml);
                
                // Extract currencies
                NodeList currNodes = currenciesDoc.getElementsByTagName("Curr");
                
                for (int i = 0; i < currNodes.getLength(); i++) {
                    currencies.add(currNodes.item(i).getTextContent());
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetCurrencies response: " + e.getMessage());
            e.printStackTrace();
            
            // Return a fallback list of common currencies
            currencies.add("EUR");
            currencies.add("USD");
            currencies.add("GBP");
            currencies.add("CHF");
            currencies.add("JPY");
        }
        
        return currencies;
    }
    
    /**
     * Parse the GetCurrencyUnits response
     * Format as shown in documentation:
     * <MNBCurrencyUnits>
     *   <Units>
     *     <Unit curr="EUR">1</Unit>
     *     <Unit curr="USD">1</Unit>
     *     ... more units ...
     *   </Units>
     * </MNBCurrencyUnits>
     */
    private Map<String, Integer> parseCurrencyUnitsResponse(String response) {
        Map<String, Integer> units = new HashMap<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetCurrencyUnitsResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document unitsDoc = parseXmlResponse(resultXml);
                
                // Extract units
                NodeList unitNodes = unitsDoc.getElementsByTagName("Unit");
                
                for (int i = 0; i < unitNodes.getLength(); i++) {
                    Element unitElement = (Element) unitNodes.item(i);
                    String currency = unitElement.getAttribute("curr");
                    int unit = Integer.parseInt(unitElement.getTextContent());
                    units.put(currency, unit);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetCurrencyUnits response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return units;
    }
    
    /**
     * Parse the GetCurrentExchangeRates response
     * Format as shown in documentation:
     * <MNBCurrentExchangeRates>
     *   <Day date="2015-07-23">
     *     <Rate unit="1" curr="AUD">208,27</Rate>
     *     <Rate unit="1" curr="BGN">157,88</Rate>
     *     ... more rates ...
     *   </Day>
     * </MNBCurrentExchangeRates>
     */
    private Map<String, Double> parseCurrentExchangeRatesResponse(String response) {
        Map<String, Double> rates = new HashMap<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetCurrentExchangeRatesResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document ratesDoc = parseXmlResponse(resultXml);
                
                // Extract rates
                NodeList rateNodes = ratesDoc.getElementsByTagName("Rate");
                
                for (int i = 0; i < rateNodes.getLength(); i++) {
                    Element rateElement = (Element) rateNodes.item(i);
                    String currency = rateElement.getAttribute("curr");
                    String rateStr = rateElement.getTextContent().replace(",", ".");
                    
                    try {
                        double rate = Double.parseDouble(rateStr);
                        rates.put(currency, rate);
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid rate value for " + currency + ": " + rateStr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetCurrentExchangeRates response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return rates;
    }
    
    /**
     * Parse the GetDateInterval response
     * Format as shown in documentation:
     * <MNBStoredInterval>
     *   <DateInterval startdate="1949-01-03" enddate="2015-07-23" />
     * </MNBStoredInterval>
     */
    private Map<String, String> parseDateIntervalResponse(String response) {
        Map<String, String> interval = new HashMap<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetDateIntervalResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document intervalDoc = parseXmlResponse(resultXml);
                
                // Extract date interval
                NodeList intervalNodes = intervalDoc.getElementsByTagName("DateInterval");
                
                if (intervalNodes.getLength() > 0) {
                    Element intervalElement = (Element) intervalNodes.item(0);
                    interval.put("startDate", intervalElement.getAttribute("startdate"));
                    interval.put("endDate", intervalElement.getAttribute("enddate"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetDateInterval response: " + e.getMessage());
            e.printStackTrace();
            
            // Set default dates if parsing fails
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            
            // Default end date is today
            interval.put("endDate", format.format(cal.getTime()));
            
            // Default start date is 3 months ago
            cal.add(Calendar.MONTH, -3);
            interval.put("startDate", format.format(cal.getTime()));
        }
        
        return interval;
    }
    
    /**
     * Parse the GetExchangeRates response
     * Format as shown in documentation:
     * <MNBExchangeRates>
     *   <Day date="2014-12-31">
     *     <Rate unit="1" curr="EUR">314,89</Rate>
     *     <Rate unit="1" curr="USD">259,13</Rate>
     *     ... more rates ...
     *   </Day>
     *   ... more days ...
     * </MNBExchangeRates>
     */
    private Map<String, Map<String, Double>> parseExchangeRatesResponse(String response) {
        Map<String, Map<String, Double>> ratesByDate = new LinkedHashMap<>();
        
        try {
            // Extract the result XML from the SOAP response
            Document doc = parseXmlResponse(response);
            NodeList resultNodes = doc.getElementsByTagName("GetExchangeRatesResult");
            
            if (resultNodes.getLength() > 0) {
                Element resultElement = (Element) resultNodes.item(0);
                String resultXml = resultElement.getTextContent();
                
                // Parse the inner XML
                Document ratesDoc = parseXmlResponse(resultXml);
                
                // Extract days
                NodeList dayNodes = ratesDoc.getElementsByTagName("Day");
                
                for (int i = 0; i < dayNodes.getLength(); i++) {
                    Element dayElement = (Element) dayNodes.item(i);
                    String date = dayElement.getAttribute("date");
                    Map<String, Double> rates = new HashMap<>();
                    
                    // Extract rates for this day
                    NodeList rateNodes = dayElement.getElementsByTagName("Rate");
                    
                    for (int j = 0; j < rateNodes.getLength(); j++) {
                        Element rateElement = (Element) rateNodes.item(j);
                        String currency = rateElement.getAttribute("curr");
                        String rateStr = rateElement.getTextContent().replace(",", ".");
                        
                        try {
                            double rate = Double.parseDouble(rateStr);
                            rates.put(currency, rate);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid rate value for " + currency + " on " + date + ": " + rateStr);
                        }
                    }
                    
                    ratesByDate.put(date, rates);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing GetExchangeRates response: " + e.getMessage());
            e.printStackTrace();
        }
        
        return ratesByDate;
    }
    
    /**
     * Parse XML string to Document object
     */
    private Document parseXmlResponse(String xmlString) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xmlString));
        return builder.parse(is);
    }
}