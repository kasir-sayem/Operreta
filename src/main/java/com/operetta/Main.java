package com.operetta;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import com.operetta.util.DataImporter;
import com.operetta.util.DatabaseUtil;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create data directory if it doesn't exist
            File dataDir = new File("data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
                System.out.println("Created data directory");
            }
            
            // Create the css directory if it doesn't exist
            File cssDir = new File("src/main/resources/css");
            if (!cssDir.exists()) {
                cssDir.mkdirs();
                System.out.println("Created css directory");
            }
            
            // Initialize database and import data
            initializeData();
            
            // Test database connection
            testDatabaseConnection();
            
            // Load the main FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            // Set up the main scene with CSS
            Scene scene = new Scene(root, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            primaryStage.setTitle("Operetta Database Application");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Error starting application: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize database and import data
     */
    private void initializeData() {
        try {
            // Initialize database (create tables)
            DatabaseUtil.initializeDatabase();
            
            // Import data if not already imported
            if (!DataImporter.isDataImported()) {
                DataImporter.importAllData();
            }
        } catch (Exception e) {
            System.err.println("Error initializing data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test database connection and check table data
     */
    private void testDatabaseConnection() {
        try {
            // Test DB connection
            Connection conn = DatabaseUtil.getConnection();
            if (conn != null) {
                System.out.println("Database connection successful!");
                
                // Check if tables exist and have data
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rsWorks = stmt.executeQuery("SELECT COUNT(*) FROM works");
                    if (rsWorks.next()) {
                        System.out.println("Found " + rsWorks.getInt(1) + " works in database");
                    }
                    
                    ResultSet rsCreators = stmt.executeQuery("SELECT COUNT(*) FROM creators");
                    if (rsCreators.next()) {
                        System.out.println("Found " + rsCreators.getInt(1) + " creators in database");
                    }
                    
                    ResultSet rsConnections = stmt.executeQuery("SELECT COUNT(*) FROM connections");
                    if (rsConnections.next()) {
                        System.out.println("Found " + rsConnections.getInt(1) + " connections in database");
                    }
                }
                
                conn.close();
            }
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}