package com.operetta.util;

import com.operetta.model.ConnectionType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for importing data from text files to the database
 */
public class DataImporter {
    
    /**
     * Check if data has already been imported
     */
    public static boolean isDataImported() {
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM works")) {
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Found " + count + " records in works table");
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if data is imported: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Import all data from text files
     */
    public static void importAllData() {
        if (isDataImported()) {
            System.out.println("Data already imported, skipping import.");
            return;
        }
        
        // Check if tables exist
        if (!DatabaseUtil.tableExists("works") || 
            !DatabaseUtil.tableExists("creators") || 
            !DatabaseUtil.tableExists("connections")) {
            System.out.println("Database tables do not exist. Please initialize the database first.");
            return;
        }
        
        System.out.println("Starting data import...");
        
        // Import in correct order and check for success at each step
        boolean worksImported = importWorks();
        if (worksImported) {
            boolean creatorsImported = importCreators();
            if (creatorsImported) {
                importConnections();
            }
        }
        
        System.out.println("Data import completed.");
    }
    
    /**
     * Import works data from works.txt
     */
    private static boolean importWorks() {
        String filePath = "data/works.txt";
        ensureFileExists(filePath, "/works.txt");
        
        try (Connection conn = DatabaseUtil.getConnection();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            System.out.println("Importing works...");
            String line = reader.readLine(); // Skip header
            
            // Prepare batch insert
            conn.setAutoCommit(false);
            String sql = "INSERT INTO works (id, title, original, theatre, pyear, acts, scenes) VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int batchSize = 0;
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t");
                    
                    if (parts.length >= 1) {
                        int id = Integer.parseInt(parts[0]);
                        String title = parts.length > 1 ? parts[1] : "";
                        String original = parts.length > 2 && !parts[2].isEmpty() ? parts[2] : null;
                        String theatre = parts.length > 3 && !parts[3].isEmpty() ? parts[3] : null;
                        
                        Integer pyear = null;
                        if (parts.length > 4 && !parts[4].isEmpty()) {
                            try {
                                pyear = Integer.parseInt(parts[4]);
                            } catch (NumberFormatException e) {
                                // Skip conversion if not a valid number
                            }
                        }
                        
                        Integer acts = null;
                        if (parts.length > 5 && !parts[5].isEmpty()) {
                            try {
                                acts = Integer.parseInt(parts[5]);
                            } catch (NumberFormatException e) {
                                // Skip conversion if not a valid number
                            }
                        }
                        
                        Integer scenes = null;
                        if (parts.length > 6 && !parts[6].isEmpty()) {
                            try {
                                scenes = Integer.parseInt(parts[6]);
                            } catch (NumberFormatException e) {
                                // Skip conversion if not a valid number
                            }
                        }
                        
                        stmt.setInt(1, id);
                        stmt.setString(2, title);
                        stmt.setString(3, original);
                        stmt.setString(4, theatre);
                        
                        if (pyear != null) {
                            stmt.setInt(5, pyear);
                        } else {
                            stmt.setNull(5, java.sql.Types.INTEGER);
                        }
                        
                        if (acts != null) {
                            stmt.setInt(6, acts);
                        } else {
                            stmt.setNull(6, java.sql.Types.INTEGER);
                        }
                        
                        if (scenes != null) {
                            stmt.setInt(7, scenes);
                        } else {
                            stmt.setNull(7, java.sql.Types.INTEGER);
                        }
                        
                        stmt.addBatch();
                        batchSize++;
                        
                        if (batchSize >= 100) {
                            stmt.executeBatch();
                            batchSize = 0;
                        }
                    }
                }
                
                if (batchSize > 0) {
                    stmt.executeBatch();
                }
                
                conn.commit();
                conn.setAutoCommit(true);
                System.out.println("Works import completed.");
                return true;
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error importing works: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Import creators data from creators.txt
     */
    private static boolean importCreators() {
        String filePath = "data/creators.txt";
        ensureFileExists(filePath, "/creators.txt");
        
        try (Connection conn = DatabaseUtil.getConnection();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            System.out.println("Importing creators...");
            String line = reader.readLine(); // Skip header
            
            // Prepare batch insert
            conn.setAutoCommit(false);
            String sql = "INSERT INTO creators (id, cname) VALUES (?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int batchSize = 0;
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t");
                    
                    if (parts.length >= 2) {
                        int id = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        
                        stmt.setInt(1, id);
                        stmt.setString(2, name);
                        
                        stmt.addBatch();
                        batchSize++;
                        
                        if (batchSize >= 100) {
                            stmt.executeBatch();
                            batchSize = 0;
                        }
                    }
                }
                
                if (batchSize > 0) {
                    stmt.executeBatch();
                }
                
                conn.commit();
                conn.setAutoCommit(true);
                System.out.println("Creators import completed.");
                return true;
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error importing creators: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Import connections data from connections.txt
     */
    private static boolean importConnections() {
        String filePath = "data/connections.txt";
        ensureFileExists(filePath, "/connections.txt");
        
        try (Connection conn = DatabaseUtil.getConnection();
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            System.out.println("Importing connections...");
            String line = reader.readLine(); // Skip header
            
            // Prepare batch insert
            conn.setAutoCommit(false);
            String sql = "INSERT INTO connections (workid, ctype, creatorid) VALUES (?, ?, ?)";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                int batchSize = 0;
                
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\t");
                    
                    if (parts.length >= 3) {
                        int workId = Integer.parseInt(parts[0]);
                        String connectionType = parts[1];
                        int creatorId = Integer.parseInt(parts[2]);
                        
                        stmt.setInt(1, workId);
                        stmt.setString(2, connectionType);
                        stmt.setInt(3, creatorId);
                        
                        stmt.addBatch();
                        batchSize++;
                        
                        if (batchSize >= 100) {
                            stmt.executeBatch();
                            batchSize = 0;
                        }
                    }
                }
                
                if (batchSize > 0) {
                    stmt.executeBatch();
                }
                
                conn.commit();
                conn.setAutoCommit(true);
                System.out.println("Connections import completed.");
                return true;
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error importing connections: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Ensure that a file exists at the given path
     */
    private static void ensureFileExists(String targetPath, String resourcePath) {
        File targetFile = new File(targetPath);
        
        if (!targetFile.exists()) {
            System.out.println("File not found at " + targetPath);
            throw new RuntimeException("Required file not found: " + targetPath);
        } else {
            System.out.println("Found file at " + targetPath);
        }
    }
}