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
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

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
    
    // Advanced filtering components
    @FXML
    private Slider minYearSlider;
    
    @FXML
    private Slider maxYearSlider;
    
    @FXML
    private Label minYearLabel;
    
    @FXML
    private Label maxYearLabel;
    
    @FXML
    private CheckBox musicCheckBox;
    
    @FXML
    private CheckBox librettoCheckBox;
    
    @FXML
    private CheckBox translationCheckBox;
    
    @FXML
    private ComboBox<Creator> creatorComboBox;
    
    @FXML
    private ChoiceBox<String> creatorRoleChoice;
    
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
    
    @FXML
    private TableView<Connection> workConnectionsTable;
    
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
    
    // For filtered list
    private FilteredList<Connection> filteredData;
    private ObservableList<Connection> allConnections;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set IDs for CSS styling
        readTab.setId("readTab");
        read2Tab.setId("read2Tab");
        writeTab.setId("writeTab");
        changeTab.setId("changeTab");
        deleteTab.setId("deleteTab");
        
        // Initialize tables and UI components
        initializeReadTab();
        initializeRead2Tab();
        initializeWriteTab();
        initializeChangeTab();
        initializeDeleteTab();
        
        // Setup numeric validation for all fields that should only contain numbers
        setupNumericValidation();
        
        // Add tab selection listener to load data when tabs are selected
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == changeTab) {
                System.out.println("Change tab selected - refreshing work dropdown");
                refreshWorkComboBox();
            } else if (newTab == deleteTab) {
                System.out.println("Delete tab selected - refreshing delete dropdown");
                refreshDeleteWorkComboBox();
            }
        });
        
        // Immediately populate the dropdowns for the currently selected tab
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == changeTab) {
            refreshWorkComboBox();
        } else if (currentTab == deleteTab) {
            refreshDeleteWorkComboBox();
        }
    }
    
    /**
     * Setup numeric validation for numeric fields
     */
    private void setupNumericValidation() {
        // Define a pattern that only accepts integers
        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty() || newText.matches("\\d*")) {
                return change;
            }
            return null;
        };
        
        // Apply to all numeric fields
        newWorkYearField.setTextFormatter(new TextFormatter<>(integerFilter));
        newWorkActsField.setTextFormatter(new TextFormatter<>(integerFilter));
        newWorkScenesField.setTextFormatter(new TextFormatter<>(integerFilter));
        editWorkYearField.setTextFormatter(new TextFormatter<>(integerFilter));
        editWorkActsField.setTextFormatter(new TextFormatter<>(integerFilter));
        editWorkScenesField.setTextFormatter(new TextFormatter<>(integerFilter));
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
        
        // Style the connection type column
        connectionTypeColumn.setCellFactory(column -> new TableCell<Connection, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label label = new Label(item);
                    label.getStyleClass().add("connection-type-chip");
                    
                    if (item.equals("MUSIC")) {
                        label.getStyleClass().add("music-chip");
                    } else if (item.equals("LIBRETTO")) {
                        label.getStyleClass().add("libretto-chip");
                    } else if (item.equals("TRANSLATION")) {
                        label.getStyleClass().add("translation-chip");
                    }
                    
                    setGraphic(label);
                }
            }
        });
        
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
        
        // Style the connection type column
        filteredConnectionTypeColumn.setCellFactory(column -> new TableCell<Connection, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    Label label = new Label(item);
                    label.getStyleClass().add("connection-type-chip");
                    
                    if (item.equals("MUSIC")) {
                        label.getStyleClass().add("music-chip");
                    } else if (item.equals("LIBRETTO")) {
                        label.getStyleClass().add("libretto-chip");
                    } else if (item.equals("TRANSLATION")) {
                        label.getStyleClass().add("translation-chip");
                    }
                    
                    setGraphic(label);
                }
            }
        });
        
        // Set up filter components
        connectionTypeComboBox.setItems(FXCollections.observableArrayList(ConnectionType.values()));
        connectionTypeComboBox.setPromptText("Select connection type");
        
        // Initialize connection type checkboxes
        musicCheckBox.setSelected(true);
        librettoCheckBox.setSelected(true);
        translationCheckBox.setSelected(true);
        
        // Setup creator role choice
        creatorRoleChoice.setItems(FXCollections.observableArrayList(
                "Any Role", "Composer", "Librettist", "Translator"));
        creatorRoleChoice.setValue("Any Role");
        
        // Populate creator combo box
        List<Creator> creators = databaseService.getAllCreators();
        creatorComboBox.setItems(FXCollections.observableArrayList(creators));
        creatorComboBox.setCellFactory(new Callback<ListView<Creator>, ListCell<Creator>>() {
            @Override
            public ListCell<Creator> call(ListView<Creator> param) {
                return new ListCell<Creator>() {
                    @Override
                    protected void updateItem(Creator item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });
        creatorComboBox.setButtonCell(new ListCell<Creator>() {
            @Override
            protected void updateItem(Creator item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select Creator");
                } else {
                    setText(item.getName());
                }
            }
        });
        
        // Load and setup year range slider
        setupYearRangeSlider();
        
        // Load all connections initially
        allConnections = FXCollections.observableArrayList(databaseService.getAllConnectionsWithDetails());
        filteredData = new FilteredList<>(allConnections, p -> true);
        filteredConnectionTable.setItems(filteredData);
        
        // Filter button action
        filterButton.setOnAction(event -> applyFilters());
    }
    
    /**
     * Setup year range slider with min/max years from database
     */
    private void setupYearRangeSlider() {
        List<Work> works = databaseService.getAllWorks();
        int minYear = Integer.MAX_VALUE;
        int maxYear = Integer.MIN_VALUE;
        int currentYear = Year.now().getValue();
        
        for (Work work : works) {
            if (work.getPremiereYear() != null) {
                minYear = Math.min(minYear, work.getPremiereYear());
                maxYear = Math.max(maxYear, work.getPremiereYear());
            }
        }
        
        // Default values if no data
        if (minYear == Integer.MAX_VALUE) minYear = 1880;
        if (maxYear == Integer.MIN_VALUE) maxYear = currentYear;
        
        // Configure min year slider
        minYearSlider.setMin(minYear);
        minYearSlider.setMax(maxYear);
        minYearSlider.setValue(minYear);
        
        // Configure max year slider
        maxYearSlider.setMin(minYear);
        maxYearSlider.setMax(maxYear);
        maxYearSlider.setValue(maxYear);
        
        // Update labels
        minYearLabel.setText(String.valueOf(minYear));
        maxYearLabel.setText(String.valueOf(maxYear));
        
        // Add listeners to update labels
        minYearSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            minYearLabel.setText(String.valueOf(value));
            
            // Ensure min doesn't exceed max
            if (value > maxYearSlider.getValue()) {
                maxYearSlider.setValue(value);
            }
        });
        
        maxYearSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int value = newVal.intValue();
            maxYearLabel.setText(String.valueOf(value));
            
            // Ensure max doesn't go below min
            if (value < minYearSlider.getValue()) {
                minYearSlider.setValue(value);
            }
        });
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
                
                // Load connections for this work
                loadWorkConnections(selectedWork.getId());
            }
        });
        
        // Update work button action
        updateWorkButton.setOnAction(event -> updateWork());
        
        // Setup the workConnectionsTable
        setupWorkConnectionsTable();
    }
    
    /**
     * Setup connections table for selected work
     */
    private void setupWorkConnectionsTable() {
        // Clear any existing columns to avoid duplicates
        if (workConnectionsTable.getColumns().size() == 0) {
            TableColumn<Connection, String> typeColumn = new TableColumn<>("Connection Type");
            typeColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getConnectionType().name()));
            typeColumn.setPrefWidth(150);
            
            TableColumn<Connection, String> creatorColumn = new TableColumn<>("Creator");
            creatorColumn.setCellValueFactory(data -> 
                new SimpleStringProperty(data.getValue().getCreatorName()));
            creatorColumn.setPrefWidth(250);
            
            workConnectionsTable.getColumns().setAll(typeColumn, creatorColumn);
        }
    }
    
    /**
     * Load connections for a specific work
     */
    private void loadWorkConnections(int workId) {
        List<Connection> connections = databaseService.getFilteredConnections(null, null, null)
            .stream()
            .filter(c -> c.getWorkId() == workId)
            .collect(Collectors.toList());
        
        if (workConnectionsTable != null) {
            workConnectionsTable.setItems(FXCollections.observableArrayList(connections));
        }
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
                
                // Count connections to show warning
                int connectionCount = countWorkConnections(selectedWork.getId());
                if (connectionCount > 0) {
                    deleteWorkStatusLabel.setText("Warning: This will also delete " + connectionCount + 
                                                " connection" + (connectionCount > 1 ? "s" : ""));
                    deleteWorkStatusLabel.setTextFill(Color.ORANGE);
                } else {
                    deleteWorkStatusLabel.setText("");
                }
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
     * Count connections for a work
     */
    private int countWorkConnections(int workId) {
        return (int) allConnections.stream()
            .filter(c -> c.getWorkId() == workId)
            .count();
    }
    
    /**
     * Refresh the connection table data
     */
    private void refreshConnectionTable() {
        List<Connection> connections = databaseService.getAllConnectionsWithDetails();
        ObservableList<Connection> data = FXCollections.observableArrayList(connections);
        connectionTable.setItems(data);
        
        // Update the filtered data list too
        allConnections = FXCollections.observableArrayList(connections);
        if (filteredData != null) {
            filteredData = new FilteredList<>(allConnections, p -> true);
            filteredConnectionTable.setItems(filteredData);
        }
    }
    
    /**
     * Apply filters to the connection table
     */
    private void applyFilters() {
        String workTitleFilter = workFilterField.getText().trim();
        String creatorNameFilter = creatorFilterField.getText().trim();
        ConnectionType connectionTypeFilter = connectionTypeComboBox.getValue();
        boolean exactMatch = exactMatchCheckBox.isSelected();
        
        // Get year range
        int minYear = (int) minYearSlider.getValue();
        int maxYear = (int) maxYearSlider.getValue();
        
        // Get connection types to include
        List<ConnectionType> typesToInclude = new ArrayList<>();
        if (musicCheckBox.isSelected()) typesToInclude.add(ConnectionType.MUSIC);
        if (librettoCheckBox.isSelected()) typesToInclude.add(ConnectionType.LIBRETTO);
        if (translationCheckBox.isSelected()) typesToInclude.add(ConnectionType.TRANSLATION);
        
        // Get creator and role filter
        Creator selectedCreator = creatorComboBox.getValue();
        String creatorRole = creatorRoleChoice.getValue();
        
        // Set predicate for filtering
        filteredData.setPredicate(connection -> {
            // Check work title
            if (workTitleFilter != null && !workTitleFilter.isEmpty()) {
                if (exactMatch) {
                    if (!connection.getWorkTitle().equals(workTitleFilter)) {
                        return false;
                    }
                } else {
                    if (!connection.getWorkTitle().toLowerCase().contains(workTitleFilter.toLowerCase())) {
                        return false;
                    }
                }
            }
            
            // Check creator name
            if (creatorNameFilter != null && !creatorNameFilter.isEmpty()) {
                if (exactMatch) {
                    if (!connection.getCreatorName().equals(creatorNameFilter)) {
                        return false;
                    }
                } else {
                    if (!connection.getCreatorName().toLowerCase().contains(creatorNameFilter.toLowerCase())) {
                        return false;
                    }
                }
            }
            
            // Check connection type
            if (connectionTypeFilter != null) {
                if (connection.getConnectionType() != connectionTypeFilter) {
                    return false;
                }
            }
            
            // Check connection types to include
            if (!typesToInclude.contains(connection.getConnectionType())) {
                return false;
            }
            
            // Check year range (need to get the work details)
            Optional<Work> work = databaseService.getWorkById(connection.getWorkId());
            if (work.isPresent()) {
                Integer premiereYear = work.get().getPremiereYear();
                if (premiereYear != null) {
                    if (premiereYear < minYear || premiereYear > maxYear) {
                        return false;
                    }
                }
            }
            
            // Check specific creator and role
            if (selectedCreator != null) {
                if (connection.getCreatorId() != selectedCreator.getId()) {
                    return false;
                }
                
                // Check role if specified
                if (!"Any Role".equals(creatorRole)) {
                    ConnectionType requiredType = null;
                    if ("Composer".equals(creatorRole)) {
                        requiredType = ConnectionType.MUSIC;
                    } else if ("Librettist".equals(creatorRole)) {
                        requiredType = ConnectionType.LIBRETTO;
                    } else if ("Translator".equals(creatorRole)) {
                        requiredType = ConnectionType.TRANSLATION;
                    }
                    
                    if (requiredType != null && connection.getConnectionType() != requiredType) {
                        return false;
                    }
                }
            }
            
            return true;
        });
    }
    
    /**
     * Add a new work to the database
     */
    private void addNewWork() {
        try {
            // Reset status before proceeding
            addWorkStatusLabel.setText("");
            
            int id = Integer.parseInt(newWorkIdField.getText().trim());
            String title = newWorkTitleField.getText().trim();
            
            if (title.isEmpty()) {
                addWorkStatusLabel.setText("Error: Title is required");
                addWorkStatusLabel.getStyleClass().remove("status-label-success");
                addWorkStatusLabel.getStyleClass().add("status-label-error");
                return;
            }
            
            String original = newWorkOriginalField.getText().trim();
            String theatre = newWorkTheatreField.getText().trim();
            
            Integer premiereYear = null;
            if (!newWorkYearField.getText().trim().isEmpty()) {
                try {
                    premiereYear = Integer.parseInt(newWorkYearField.getText().trim());
                } catch (NumberFormatException e) {
                    addWorkStatusLabel.setText("Error: Invalid year format");
                    addWorkStatusLabel.getStyleClass().remove("status-label-success");
                    addWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            Integer acts = null;
            if (!newWorkActsField.getText().trim().isEmpty()) {
                try {
                    acts = Integer.parseInt(newWorkActsField.getText().trim());
                } catch (NumberFormatException e) {
                    addWorkStatusLabel.setText("Error: Invalid acts format");
                    addWorkStatusLabel.getStyleClass().remove("status-label-success");
                    addWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            Integer scenes = null;
            if (!newWorkScenesField.getText().trim().isEmpty()) {
                try {
                    scenes = Integer.parseInt(newWorkScenesField.getText().trim());
                } catch (NumberFormatException e) {
                    addWorkStatusLabel.setText("Error: Invalid scenes format");
                    addWorkStatusLabel.getStyleClass().remove("status-label-success");
                    addWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            Work work = new Work(id, title, original.isEmpty() ? null : original, 
                                theatre.isEmpty() ? null : theatre, premiereYear, acts, scenes);
            
            boolean success = databaseService.addWork(work);
            
            if (success) {
                addWorkStatusLabel.setText("Work added successfully");
                addWorkStatusLabel.getStyleClass().remove("status-label-error");
                addWorkStatusLabel.getStyleClass().add("status-label-success");
                
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
                addWorkStatusLabel.getStyleClass().remove("status-label-success");
                addWorkStatusLabel.getStyleClass().add("status-label-error");
            }
        } catch (NumberFormatException e) {
            addWorkStatusLabel.setText("Error: Invalid number format");
            addWorkStatusLabel.getStyleClass().remove("status-label-success");
            addWorkStatusLabel.getStyleClass().add("status-label-error");
        } catch (Exception e) {
            addWorkStatusLabel.setText("Error: " + e.getMessage());
            addWorkStatusLabel.getStyleClass().remove("status-label-success");
            addWorkStatusLabel.getStyleClass().add("status-label-error");
        }
    }
    
    /**
     * Update an existing work
     */
    private void updateWork() {
        Work selectedWork = workSelectComboBox.getValue();
        
        if (selectedWork == null) {
            updateWorkStatusLabel.setText("Error: No work selected");
            updateWorkStatusLabel.getStyleClass().remove("status-label-success");
            updateWorkStatusLabel.getStyleClass().add("status-label-error");
            return;
        }
        
        try {
            String title = editWorkTitleField.getText().trim();
            String original = editWorkOriginalField.getText().trim();
            String theatre = editWorkTheatreField.getText().trim();
            
            Integer premiereYear = null;
            if (!editWorkYearField.getText().trim().isEmpty()) {
                try {
                    premiereYear = Integer.parseInt(editWorkYearField.getText().trim());
                } catch (NumberFormatException e) {
                    updateWorkStatusLabel.setText("Error: Invalid year format");
                    updateWorkStatusLabel.getStyleClass().remove("status-label-success");
                    updateWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            Integer acts = null;
            if (!editWorkActsField.getText().trim().isEmpty()) {
                try {
                    acts = Integer.parseInt(editWorkActsField.getText().trim());
                } catch (NumberFormatException e) {
                    updateWorkStatusLabel.setText("Error: Invalid acts format");
                    updateWorkStatusLabel.getStyleClass().remove("status-label-success");
                    updateWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            Integer scenes = null;
            if (!editWorkScenesField.getText().trim().isEmpty()) {
                try {
                    scenes = Integer.parseInt(editWorkScenesField.getText().trim());
                } catch (NumberFormatException e) {
                    updateWorkStatusLabel.setText("Error: Invalid scenes format");
                    updateWorkStatusLabel.getStyleClass().remove("status-label-success");
                    updateWorkStatusLabel.getStyleClass().add("status-label-error");
                    return;
                }
            }
            
            // Validate required fields
            if (title.isEmpty()) {
                updateWorkStatusLabel.setText("Error: Title is required");
                updateWorkStatusLabel.getStyleClass().remove("status-label-success");
                updateWorkStatusLabel.getStyleClass().add("status-label-error");
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
                updateWorkStatusLabel.getStyleClass().remove("status-label-error");
                updateWorkStatusLabel.getStyleClass().add("status-label-success");
                
                // Refresh work combo boxes and connection tables
                refreshWorkComboBox();
                refreshDeleteWorkComboBox();
                refreshConnectionTable();
            } else {
                updateWorkStatusLabel.setText("Error updating work");
                updateWorkStatusLabel.getStyleClass().remove("status-label-success");
                updateWorkStatusLabel.getStyleClass().add("status-label-error");
            }
        } catch (NumberFormatException e) {
            updateWorkStatusLabel.setText("Error: Invalid number format");
            updateWorkStatusLabel.getStyleClass().remove("status-label-success");
            updateWorkStatusLabel.getStyleClass().add("status-label-error");
        } catch (Exception e) {
            updateWorkStatusLabel.setText("Error: " + e.getMessage());
            updateWorkStatusLabel.getStyleClass().remove("status-label-success");
            updateWorkStatusLabel.getStyleClass().add("status-label-error");
        }
    }
    
    /**
     * Delete a work from the database
     */
    private void deleteWork() {
        Work selectedWork = deleteWorkComboBox.getValue();
        
        if (selectedWork == null) {
            deleteWorkStatusLabel.setText("Error: No work selected");
            deleteWorkStatusLabel.setTextFill(Color.RED);
            return;
        }
        
        // Count connections
        int connectionCount = countWorkConnections(selectedWork.getId());
        String confirmMessage = "Are you sure you want to delete the work '" + selectedWork.getTitle() + "'?";
        if (connectionCount > 0) {
            confirmMessage += "\nThis will also delete " + connectionCount + 
                " connection" + (connectionCount > 1 ? "s" : "") + ".";
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Work");
        confirmAlert.setContentText(confirmMessage);
        
        // Style the buttons
        DialogPane dialogPane = confirmAlert.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = databaseService.deleteWork(selectedWork.getId());
            
            if (success) {
                deleteWorkStatusLabel.setText("Work deleted successfully");
                deleteWorkStatusLabel.setTextFill(Color.GREEN);
                
                // Refresh work combo boxes and connection tables
                refreshWorkComboBox();
                refreshDeleteWorkComboBox();
                refreshConnectionTable();
                
                // Hide work details
                workDetailsBox.setVisible(false);
                deleteWorkComboBox.setValue(null);
            } else {
                deleteWorkStatusLabel.setText("Error deleting work");
                deleteWorkStatusLabel.setTextFill(Color.RED);
            }
        }
    }
    
    /**
     * Manual refresh for work combo box
     */
    @FXML
    private void manualRefreshWorkComboBox() {
        refreshWorkComboBox();
    }
    
    /**
     * Manual refresh for delete work combo box
     */
    @FXML
    private void manualRefreshDeleteComboBox() {
        refreshDeleteWorkComboBox();
    }
    
    /**
     * Refresh the work combo box in the Change tab
     */
    private void refreshWorkComboBox() {
        try {
            System.out.println("Refreshing work combo box...");
            List<Work> works = databaseService.getAllWorks();
            System.out.println("Found " + works.size() + " works");
            
            if (workSelectComboBox != null) {
                workSelectComboBox.getItems().clear();
                workSelectComboBox.getItems().addAll(works);
                workSelectComboBox.setPromptText(works.isEmpty() ? 
                    "No works available" : "Select a work to edit");
            } else {
                System.err.println("workSelectComboBox is null!");
            }
            
            // Set cell factory for better display
            workSelectComboBox.setCellFactory(new Callback<ListView<Work>, ListCell<Work>>() {
                @Override
                public ListCell<Work> call(ListView<Work> param) {
                    return new ListCell<Work>() {
                        @Override
                        protected void updateItem(Work item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getId() + " - " + item.getTitle());
                            }
                        }
                    };
                }
            });
            
            workSelectComboBox.setButtonCell(new ListCell<Work>() {
                @Override
                protected void updateItem(Work item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select a work");
                    } else {
                        setText(item.getId() + " - " + item.getTitle());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error refreshing work combo box: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Refresh the work combo box in the Delete tab
     */
    private void refreshDeleteWorkComboBox() {
        try {
            System.out.println("Refreshing delete work combo box...");
            List<Work> works = databaseService.getAllWorks();
            System.out.println("Found " + works.size() + " works");
            
            if (deleteWorkComboBox != null) {
                deleteWorkComboBox.getItems().clear();
                deleteWorkComboBox.getItems().addAll(works);
                deleteWorkComboBox.setPromptText(works.isEmpty() ? 
                    "No works available" : "Select a work to delete");
            } else {
                System.err.println("deleteWorkComboBox is null!");
            }
            
            // Set cell factory for better display
            deleteWorkComboBox.setCellFactory(new Callback<ListView<Work>, ListCell<Work>>() {
                @Override
                public ListCell<Work> call(ListView<Work> param) {
                    return new ListCell<Work>() {
                        @Override
                        protected void updateItem(Work item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                setText(item.getId() + " - " + item.getTitle());
                            }
                        }
                    };
                }
            });
            
            deleteWorkComboBox.setButtonCell(new ListCell<Work>() {
                @Override
                protected void updateItem(Work item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select a work to delete");
                    } else {
                        setText(item.getId() + " - " + item.getTitle());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("Error refreshing delete work combo box: " + e.getMessage());
            e.printStackTrace();
        }
    }
}