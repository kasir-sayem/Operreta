package com.operetta.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {
    private static final String DB_URL;
    
    static {
        // Look for database file in the project root directory
        File dbFile = new File("operetta.db");
        if (dbFile.exists()) {
            DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        } else {
            // Fall back to the data subdirectory
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            dbFile = new File(dataDir, "operetta.db");
            DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        }
        System.out.println("Using database at: " + dbFile.getAbsolutePath());
    }
    
    /**
     * Get a connection to the database
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Check if a table exists in the database
     */
    public static boolean tableExists(String tableName) {
        try (Connection conn = getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking if table exists: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Initialize the database tables if they don't exist
     */
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create the works table
            stmt.execute("CREATE TABLE IF NOT EXISTS works (" +
                    "id INTEGER PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "original TEXT, " +
                    "theatre TEXT, " +
                    "pyear INTEGER, " +
                    "acts INTEGER, " +
                    "scenes INTEGER)");
            System.out.println("Works table created or already exists");
            
            // Create the creators table
            stmt.execute("CREATE TABLE IF NOT EXISTS creators (" +
                    "id INTEGER PRIMARY KEY, " +
                    "cname TEXT NOT NULL)");
            System.out.println("Creators table created or already exists");
            
            // Create the connections table
            stmt.execute("CREATE TABLE IF NOT EXISTS connections (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "workid INTEGER NOT NULL, " +
                    "ctype TEXT NOT NULL, " +
                    "creatorid INTEGER NOT NULL, " +
                    "FOREIGN KEY (workid) REFERENCES works(id), " +
                    "FOREIGN KEY (creatorid) REFERENCES creators(id))");
            System.out.println("Connections table created or already exists");
            
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            boolean isConnected = conn != null && !conn.isClosed();
            if (isConnected) {
                System.out.println("Database connection successful");
            } else {
                System.err.println("Database connection failed");
            }
            return isConnected;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return false;
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