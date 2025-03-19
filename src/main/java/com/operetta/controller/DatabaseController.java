package com.operetta.controller;

import com.operetta.model.Connection;
import com.operetta.model.ConnectionType;
import com.operetta.model.Creator;
import com.operetta.model.Work;
import com.operetta.service.DatabaseService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the database view
 */
public class DatabaseController implements Initializable {
    
    private final DatabaseService databaseService = new DatabaseService();
    
    // Tab pane and tabs
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab readTab;
    
    @FXML
    private Tab read2Tab;
    
    @FXML
    private Tab writeTab;
    
    @FXML
    private Tab changeTab;
    
    @FXML
    private Tab deleteTab;
    
    // Read tab components
    @FXML
    private TableView<Connection> connectionTable;
    
    @FXML
    private TableColumn<Connection, Integer> idColumn;
    
    @FXML
    private TableColumn<Connection, String> workColumn;
    
    @FXML
    private TableColumn<Connection, String> connectionTypeColumn;
    
    @FXML
    private TableColumn<Connection, String> creatorColumn;
    
    // Read2 tab components
    @FXML
    private TextField workFilterField;
    
    @FXML
    private TextField creatorFilterField;
    
    @FXML
    private ComboBox<ConnectionType> connectionTypeComboBox;
    
    @FXML
    private CheckBox exactMatchCheckBox;
    
    @FXML
    private Button filterButton;
    
    @FXML
    private TableView<Connection> filteredConnectionTable;
    
    @FXML
    private TableColumn<Connection, Integer> filteredIdColumn;
    
    @FXML
    private TableColumn<Connection, String> filteredWorkColumn;
    
    @FXML
    private TableColumn<Connection, String> filteredConnectionTypeColumn;
    
    @FXML
    private TableColumn<Connection, String> filteredCreatorColumn;
    
    // Write tab components
    @FXML
    private TextField newWorkIdField;
    
    @FXML
    private TextField newWorkTitleField;
    
    @FXML
    private TextField newWorkOriginalField;
    
    @FXML
    private TextField newWorkTheatreField;
    
    @FXML
    private TextField newWorkYearField;
    
    @FXML
    private TextField newWorkActsField;
    
    @FXML
    private TextField newWorkScenesField;
    
    @FXML
    private Button addWorkButton;
    
    @FXML
    private Label addWorkStatusLabel;
    
    // Change tab components
    @FXML
    private ComboBox<Work> workSelectComboBox;
    
    @FXML
    private TextField editWorkTitleField;
    
    @FXML
    private TextField editWorkOriginalField;
    
    @FXML
    private TextField editWorkTheatreField;
    
    @FXML
    private TextField editWorkYearField;
    
    @FXML
    private TextField editWorkActsField;
    
    @FXML
    private TextField editWorkScenesField;
    
    @FXML
    private Button updateWorkButton;
    
    @FXML
    private Label updateWorkStatusLabel;
    
    // Delete tab components
    @FXML
    private ComboBox<Work> deleteWorkComboBox;
    
    @FXML
    private Button deleteWorkButton;
    
    @FXML
    private Label deleteWorkStatusLabel;
    
    @FXML
    private VBox workDetailsBox;
    
    @FXML
    private Label workTitleLabel;
    
    @FXML
    private Label workTheatreLabel;
    
    @FXML
    private Label workYearLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize tables and UI components
        initializeReadTab();
        initializeRead2Tab();
        initializeWriteTab();
        initializeChangeTab();
        initializeDeleteTab();
    }
    
    /**
     * Initialize the Read tab
     */
    private void initializeReadTab() {
        // Set up table columns
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        workColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkTitle()));
        connectionTypeColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getConnectionType().name()));
        creatorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatorName()));
        
        // Load all connections with details
        refreshConnectionTable();
    }
    
    /**
     * Initialize the Read2 tab (filtered view)
     */
    private void initializeRead2Tab() {
        // Set up table columns
        filteredIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        filteredWorkColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getWorkTitle()));
        filteredConnectionTypeColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getConnectionType().name()));
        filteredCreatorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCreatorName()));
        
        // Set up filter components
        connectionTypeComboBox.setItems(FXCollections.observableArrayList(ConnectionType.values()));
        connectionTypeComboBox.setPromptText("Select connection type");
        
        filterButton.setOnAction(event -> applyFilters());
    }
    
    /**
     * Initialize the Write tab
     */
    private void initializeWriteTab() {
        // Set next available ID
        newWorkIdField.setText(String.valueOf(databaseService.getNextWorkId()));
        
        // Add work button action
        addWorkButton.setOnAction(event -> addNewWork());
    }
    
    /**
     * Initialize the Change tab
     */
    private void initializeChangeTab() {
        // Load works into combo box
        refreshWorkComboBox();
        
        // Work selection listener
        workSelectComboBox.setOnAction(event -> {
            Work selectedWork = workSelectComboBox.getValue();
            if (selectedWork != null) {
                editWorkTitleField.setText(selectedWork.getTitle());
                editWorkOriginalField.setText(selectedWork.getOriginal());
                editWorkTheatreField.setText(selectedWork.getTheatre());
                
                editWorkYearField.setText(selectedWork.getPremiereYear() != null ? 
                        selectedWork.getPremiereYear().toString() : "");
                
                editWorkActsField.setText(selectedWork.getActs() != null ? 
                        selectedWork.getActs().toString() : "");
                
                editWorkScenesField.setText(selectedWork.getScenes() != null ? 
                        selectedWork.getScenes().toString() : "");
            }
        });
        
        // Update work button action
        updateWorkButton.setOnAction(event -> updateWork());
    }
    
    /**
     * Initialize the Delete tab
     */
    private void initializeDeleteTab() {
        // Load works into combo box
        refreshDeleteWorkComboBox();
        
        // Work selection listener
        deleteWorkComboBox.setOnAction(event -> {
            Work selectedWork = deleteWorkComboBox.getValue();
            if (selectedWork != null) {
                workTitleLabel.setText(selectedWork.getTitle());
                workTheatreLabel.setText(selectedWork.getTheatre() != null ? selectedWork.getTheatre() : "N/A");
                workYearLabel.setText(selectedWork.getPremiereYear() != null ? 
                        selectedWork.getPremiereYear().toString() : "N/A");
                
                workDetailsBox.setVisible(true);
            } else {
                workDetailsBox.setVisible(false);
            }
        });
        
        // Hide work details initially
        workDetailsBox.setVisible(false);
        
        // Delete work button action
        deleteWorkButton.setOnAction(event -> deleteWork());
    }
    
    /**
     * Refresh the connection table data
     */
    private void refreshConnectionTable() {
        List<Connection> connections = databaseService.getAllConnectionsWithDetails();
        ObservableList<Connection> data = FXCollections.observableArrayList(connections);
        connectionTable.setItems(data);
    }
    
    /**
     * Apply filters to the connection table
     */
    private void applyFilters() {
        String workTitleFilter = workFilterField.getText().trim();
        String creatorNameFilter = creatorFilterField.getText().trim();
        ConnectionType connectionTypeFilter = connectionTypeComboBox.getValue();
        
        List<Connection> filteredConnections = databaseService.getFilteredConnections(
                workTitleFilter.isEmpty() ? null : workTitleFilter,
                creatorNameFilter.isEmpty() ? null : creatorNameFilter,
                connectionTypeFilter
        );
        
        ObservableList<Connection> data = FXCollections.observableArrayList(filteredConnections);
        filteredConnectionTable.setItems(data);
    }
    
    /**
     * Add a new work to the database
     */
    private void addNewWork() {
        try {
            int id = Integer.parseInt(newWorkIdField.getText().trim());
            String title = newWorkTitleField.getText().trim();
            String original = newWorkOriginalField.getText().trim();
            String theatre = newWorkTheatreField.getText().trim();
            
            Integer premiereYear = null;
            if (!newWorkYearField.getText().trim().isEmpty()) {
                premiereYear = Integer.parseInt(newWorkYearField.getText().trim());
            }
            
            Integer acts = null;
            if (!newWorkActsField.getText().trim().isEmpty()) {
                acts = Integer.parseInt(newWorkActsField.getText().trim());
            }
            
            Integer scenes = null;
            if (!newWorkScenesField.getText().trim().isEmpty()) {
                scenes = Integer.parseInt(newWorkScenesField.getText().trim());
            }
            
            // Validate required fields
            if (title.isEmpty()) {
                addWorkStatusLabel.setText("Error: Title is required");
                return;
            }
            
            Work work = new Work(id, title, original.isEmpty() ? null : original, 
                                theatre.isEmpty() ? null : theatre, premiereYear, acts, scenes);
            
            boolean success = databaseService.addWork(work);
            
            if (success) {
                addWorkStatusLabel.setText("Work added successfully");
                
                // Clear form fields
                newWorkIdField.setText(String.valueOf(databaseService.getNextWorkId()));
                newWorkTitleField.clear();
                newWorkOriginalField.clear();
                newWorkTheatreField.clear();
                newWorkYearField.clear();
                newWorkActsField.clear();
                newWorkScenesField.clear();
                
                // Refresh work combo boxes
                refreshWorkComboBox();
                refreshDeleteWorkComboBox();
            } else {
                addWorkStatusLabel.setText("Error adding work");
            }
        } catch (NumberFormatException e) {
            addWorkStatusLabel.setText("Error: Invalid number format");
        } catch (Exception e) {
            addWorkStatusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing work
     */
    private void updateWork() {
        Work selectedWork = workSelectComboBox.getValue();
        
        if (selectedWork == null) {
            updateWorkStatusLabel.setText("Error: No work selected");
            return;
        }
        
        try {
            String title = editWorkTitleField.getText().trim();
            String original = editWorkOriginalField.getText().trim();
            String theatre = editWorkTheatreField.getText().trim();
            
            Integer premiereYear = null;
            if (!editWorkYearField.getText().trim().isEmpty()) {
                premiereYear = Integer.parseInt(editWorkYearField.getText().trim());
            }
            
            Integer acts = null;
            if (!editWorkActsField.getText().trim().isEmpty()) {
                acts = Integer.parseInt(editWorkActsField.getText().trim());
            }
            
            Integer scenes = null;
            if (!editWorkScenesField.getText().trim().isEmpty()) {
                scenes = Integer.parseInt(editWorkScenesField.getText().trim());
            }
            
            // Validate required fields
            if (title.isEmpty()) {
                updateWorkStatusLabel.setText("Error: Title is required");
                return;
            }
            
            // Update work object
            selectedWork.setTitle(title);
            selectedWork.setOriginal(original.isEmpty() ? null : original);
            selectedWork.setTheatre(theatre.isEmpty() ? null : theatre);
            selectedWork.setPremiereYear(premiereYear);
            selectedWork.setActs(acts);
            selectedWork.setScenes(scenes);
            
            boolean success = databaseService.updateWork(selectedWork);
            
            if (success) {
                updateWorkStatusLabel.setText("Work updated successfully");
                
                // Refresh work combo boxes
                refreshWorkComboBox();
                refreshDeleteWorkComboBox();
            } else {
                updateWorkStatusLabel.setText("Error updating work");
            }
        } catch (NumberFormatException e) {
            updateWorkStatusLabel.setText("Error: Invalid number format");
        } catch (Exception e) {
            updateWorkStatusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    /**
     * Delete a work from the database
     */
    private void deleteWork() {
        Work selectedWork = deleteWorkComboBox.getValue();
        
        if (selectedWork == null) {
            deleteWorkStatusLabel.setText("Error: No work selected");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Work");
        confirmAlert.setContentText("Are you sure you want to delete the work '" + 
                                   selectedWork.getTitle() + "'?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = databaseService.deleteWork(selectedWork.getId());
            
            if (success) {
                deleteWorkStatusLabel.setText("Work deleted successfully");
                
                // Refresh work combo boxes
                refreshWorkComboBox();
                refreshDeleteWorkComboBox();
                
                // Hide work details
                workDetailsBox.setVisible(false);
                deleteWorkComboBox.setValue(null);
            } else {
                deleteWorkStatusLabel.setText("Error deleting work");
            }
        }
    }
    
    /**
     * Refresh the work combo box in the Change tab
     */
    private void refreshWorkComboBox() {
        List<Work> works = databaseService.getAllWorks();
        ObservableList<Work> data = FXCollections.observableArrayList(works);
        workSelectComboBox.setItems(data);
    }
    
    /**
     * Refresh the work combo box in the Delete tab
     */
    private void refreshDeleteWorkComboBox() {
        List<Work> works = databaseService.getAllWorks();
        ObservableList<Work> data = FXCollections.observableArrayList(works);
        deleteWorkComboBox.setItems(data);
    }
}