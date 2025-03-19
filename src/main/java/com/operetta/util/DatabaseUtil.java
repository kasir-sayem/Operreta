package com.operetta.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:operetta.db";
    
    /**
     * Get a connection to the database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Initialize the database tables if they don't exist
     */
    public static void initializeDatabase() {
        File dbFile = new File("operetta.db");
        boolean databaseExists = dbFile.exists();
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Only create tables if the database is new
            if (!databaseExists) {
                System.out.println("Initializing database...");
                
                // Create the works table
                stmt.execute("CREATE TABLE IF NOT EXISTS works (" +
                        "id INTEGER PRIMARY KEY, " +
                        "title TEXT NOT NULL, " +
                        "original TEXT, " +
                        "theatre TEXT, " +
                        "pyear INTEGER, " +
                        "acts INTEGER, " +
                        "scenes INTEGER)");
                
                // Create the creators table
                stmt.execute("CREATE TABLE IF NOT EXISTS creators (" +
                        "id INTEGER PRIMARY KEY, " +
                        "cname TEXT NOT NULL)");
                
                // Create the connections table
                stmt.execute("CREATE TABLE IF NOT EXISTS connections (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "workid INTEGER NOT NULL, " +
                        "ctype TEXT NOT NULL, " +
                        "creatorid INTEGER NOT NULL, " +
                        "FOREIGN KEY (workid) REFERENCES works(id), " +
                        "FOREIGN KEY (creatorid) REFERENCES creators(id))");
                
                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Close a database connection
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}