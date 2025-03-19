package com.operetta.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Controller for the main window
 */
public class MainController implements Initializable {
    
    @FXML
    private BorderPane mainPane;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize the main window
    }
    
    /**
     * Load the parallel demo view
     */
    @FXML
    private void loadParallelView() {
        loadView("/fxml/parallel.fxml");
    }
    
    /**
     * Load the database view
     */
    @FXML
    private void loadDatabaseView() {
        loadView("/fxml/database.fxml");
    }
    
    /**
     * Load the SOAP client view
     */
    @FXML
    private void loadSoapView() {
        loadView("/fxml/soap.fxml");
    }
    
    /**
     * Load the Forex view
     */
    @FXML
    private void loadForexView() {
        loadView("/fxml/forex.fxml");
    }
    
    /**
     * Helper method to load a view into the main pane
     */
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainPane.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}