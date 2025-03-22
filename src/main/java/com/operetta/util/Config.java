package com.operetta.util;

import com.oanda.v20.account.AccountID;

/**
 * Configuration class for storing API credentials and constants.
 * For a real application, you would want to store these securely
 * or load them from environment variables or a properties file.
 */
// public class Config {
//     private Config() {}  // Prevent instantiation
    
//     // Oanda API configuration
//     public static final String URL = "https://api-fxpractice.oanda.com";
//     public static final String TOKEN = "YOUR_OANDA_API_TOKEN"; // Replace with your actual token
//     public static final AccountID ACCOUNT_ID = new AccountID("YOUR_OANDA_ACCOUNT_ID"); // Replace with your actual account ID
// }

public class Config {
    private Config() {}

    public static final String URL = "https://api-fxpractice.oanda.com"; // URL de prática
    public static final String TOKEN = "5f5036f59934645111ab53c85b6149d9-44e5618b7dd38fe118681d87b77717d7"; // Substitua por seu token gerado na OANDA
    public static final AccountID ACCOUNTID = new AccountID("101-004-30416899-001"); // Substitua pelo seu ID da conta
}