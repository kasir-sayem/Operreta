package com.operetta.controller;

import com.operetta.service.SoapService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the SOAP client view
 * Implements all features according to MNB documentation
 */
public class SoapController implements Initializable {
    
    private final SoapService soapService = new SoapService();
    private ExecutorService executorService;
    
    // Tab pane and tabs
    @FXML
    private TabPane tabPane;
    
    @FXML
    private Tab downloadTab;
    
    @FXML
    private Tab download2Tab;
    
    @FXML
    private Tab graphTab;
    
    // Download tab components
    @FXML
    private Button downloadAllButton;
    
    @FXML
    private ProgressIndicator downloadProgress;
    
    @FXML
    private Label downloadStatusLabel;
    
    // Download2 tab components
    @FXML
    private ComboBox<String> currencyComboBox;
    
    @FXML
    private DatePicker startDatePicker;
    
    @FXML
    private DatePicker endDatePicker;
    
    @FXML
    private Button downloadFilteredButton;
    
    @FXML
    private ProgressIndicator downloadFilteredProgress;
    
    @FXML
    private Label downloadFilteredStatusLabel;
    
    // Graph tab components
    @FXML
    private ComboBox<String> graphCurrencyComboBox;
    
    @FXML
    private DatePicker graphStartDatePicker;
    
    @FXML
    private DatePicker graphEndDatePicker;
    
    @FXML
    private Button generateGraphButton;
    
    @FXML
    private ProgressIndicator graphProgress;
    
    @FXML
    private LineChart<Number, Number> exchangeRateChart;
    
    @FXML
    private VBox chartContainer;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialize date pickers with default dates
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        
        // Configure date pickers
        setupDatePicker(startDatePicker, oneMonthAgo);
        setupDatePicker(endDatePicker, now);
        setupDatePicker(graphStartDatePicker, oneMonthAgo);
        setupDatePicker(graphEndDatePicker, now);
        
        // Initialize currency combo boxes - first check available date range
        getDateRangeAndCurrencies();
        
        // Set up button actions
        downloadAllButton.setOnAction(event -> downloadAllData());
        downloadFilteredButton.setOnAction(event -> downloadFilteredData());
        generateGraphButton.setOnAction(event -> generateGraph());
        
        // Set up initial progress indicators state
        downloadProgress.setVisible(false);
        downloadFilteredProgress.setVisible(false);
        graphProgress.setVisible(false);
        
        // Initialize status labels
        downloadStatusLabel.setText("Click the button to download all exchange rate data.");
        downloadFilteredStatusLabel.setText("Select parameters and click the button to download filtered data.");
    }
    
    /**
     * Set up date picker with initial value and validation
     */
    private void setupDatePicker(DatePicker datePicker, LocalDate initialValue) {
        datePicker.setValue(initialValue);
        
        // Add validation to prevent future dates
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isAfter(LocalDate.now()));
            }
        });
    }
    
    /**
     * Get available date range and currencies from the MNB service
     */
    private void getDateRangeAndCurrencies() {
        downloadProgress.setVisible(true);
        downloadFilteredProgress.setVisible(true);
        graphProgress.setVisible(true);
        
        downloadStatusLabel.setText("Loading available currencies and date range...");
        downloadFilteredStatusLabel.setText("Loading available currencies and date range...");
        
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                // Use GetInfo method to get both date range and currencies
                try {
                    return soapService.getInfo();
                } catch (Exception e) {
                    // If GetInfo fails, try GetCurrencies as fallback
                    Map<String, Object> result = new HashMap<>();
                    result.put("currencies", soapService.getCurrencies());
                    return result;
                }
            }
        };
        
        task.setOnSucceeded(event -> {
            Map<String, Object> info = task.getValue();
            
            // Set date range if available
            if (info.containsKey("firstDate") && info.containsKey("lastDate")) {
                String firstDate = (String) info.get("firstDate");
                String lastDate = (String) info.get("lastDate");
                
                downloadStatusLabel.setText("Available data from " + firstDate + " to " + lastDate);
            }
            
            // Set currencies in combo boxes
            if (info.containsKey("currencies")) {
                @SuppressWarnings("unchecked")
                List<String> currencies = (List<String>) info.get("currencies");
                
                if (!currencies.isEmpty()) {
                    // Set items for combo boxes
                    currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
                    graphCurrencyComboBox.setItems(FXCollections.observableArrayList(currencies));
                    
                    // Set default selections - prefer EUR or USD if available
                    String defaultCurrency = "HUF";
                    if (currencies.contains("EUR")) defaultCurrency = "EUR";
                    else if (currencies.contains("USD")) defaultCurrency = "USD";
                    else if (!currencies.isEmpty()) defaultCurrency = currencies.get(0);
                    
                    currencyComboBox.setValue(defaultCurrency);
                    graphCurrencyComboBox.setValue(defaultCurrency);
                }
            }
            
            // Hide progress indicators
            downloadProgress.setVisible(false);
            downloadFilteredProgress.setVisible(false);
            graphProgress.setVisible(false);
        });
        
        task.setOnFailed(event -> {
            // Handle error and hide progress indicators
            downloadProgress.setVisible(false);
            downloadFilteredProgress.setVisible(false);
            graphProgress.setVisible(false);
            
            Throwable exception = task.getException();
            System.err.println("Error loading currencies: " + exception.getMessage());
            exception.printStackTrace();
            
            downloadStatusLabel.setText("Error loading currencies. Please try again.");
            downloadFilteredStatusLabel.setText("Error loading currencies. Please try again.");
            
            showAlert("Error", "Failed to load currencies", 
                     "An error occurred while loading currencies: " + exception.getMessage());
            
            // Load hardcoded currencies as fallback
            List<String> fallbackCurrencies = Arrays.asList("EUR", "USD", "GBP", "CHF", "JPY");
            currencyComboBox.setItems(FXCollections.observableArrayList(fallbackCurrencies));
            graphCurrencyComboBox.setItems(FXCollections.observableArrayList(fallbackCurrencies));
            currencyComboBox.setValue("EUR");
            graphCurrencyComboBox.setValue("EUR");
        });
        
        executorService.submit(task);
    }
    
    /**
     * Download all exchange rate data to a file
     */
    private void downloadAllData() {
        downloadProgress.setVisible(true);
        downloadStatusLabel.setText("Downloading all exchange rate data...");
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File dir = new File("c:/data");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                soapService.downloadAllData("c:/data/Bank.txt");
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            downloadProgress.setVisible(false);
            downloadStatusLabel.setText("Download completed successfully. File saved to c:/data/Bank.txt");
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Download Complete", "All exchange rate data has been downloaded successfully.");
        });
        
        task.setOnFailed(event -> {
            downloadProgress.setVisible(false);
            
            Throwable exception = task.getException();
            System.err.println("Error downloading data: " + exception.getMessage());
            exception.printStackTrace();
            
            downloadStatusLabel.setText("Download failed: " + exception.getMessage());
            
            // Show error message
            showAlert("Error", "Download Failed", 
                     "An error occurred during download: " + exception.getMessage());
        });
        
        executorService.submit(task);
    }
    
    /**
     * Download filtered exchange rate data to a file
     */
    private void downloadFilteredData() {
        String currency = currencyComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (currency == null || startDate == null || endDate == null) {
            downloadFilteredStatusLabel.setText("Error: Please select all required fields.");
            return;
        }
        
        if (endDate.isBefore(startDate)) {
            downloadFilteredStatusLabel.setText("Error: End date must be after start date.");
            return;
        }
        
        downloadFilteredProgress.setVisible(true);
        downloadFilteredStatusLabel.setText("Downloading filtered exchange rate data...");
        
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File dir = new File("c:/data");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                soapService.downloadFilteredData("c:/data/Bank.txt", currency, start, end);
                return null;
            }
        };
        
        task.setOnSucceeded(event -> {
            downloadFilteredProgress.setVisible(false);
            downloadFilteredStatusLabel.setText("Download completed successfully. File saved to c:/data/Bank.txt");
            
            // Show success message
            showAlert(Alert.AlertType.INFORMATION, "Success", 
                     "Download Complete", "Filtered exchange rate data has been downloaded successfully.");
        });
        
        task.setOnFailed(event -> {
            downloadFilteredProgress.setVisible(false);
            
            Throwable exception = task.getException();
            System.err.println("Error downloading filtered data: " + exception.getMessage());
            exception.printStackTrace();
            
            downloadFilteredStatusLabel.setText("Download failed: " + exception.getMessage());
            
            // Show error message
            showAlert("Error", "Download Failed", 
                     "An error occurred during download: " + exception.getMessage());
        });
        
        executorService.submit(task);
    }
    
    /**
     * Generate exchange rate graph
     */
    private void generateGraph() {
        String currency = graphCurrencyComboBox.getValue();
        LocalDate startDate = graphStartDatePicker.getValue();
        LocalDate endDate = graphEndDatePicker.getValue();
        
        if (currency == null || startDate == null || endDate == null) {
            showAlert("Error", "Missing Data", "Please select all required fields.");
            return;
        }
        
        if (endDate.isBefore(startDate)) {
            showAlert("Error", "Invalid Date Range", "End date must be after start date.");
            return;
        }
        
        graphProgress.setVisible(true);
        
        Task<Map<String, Double>> task = new Task<>() {
            @Override
            protected Map<String, Double> call() throws Exception {
                Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                
                return soapService.getExchangeRates(currency, start, end);
            }
        };
        
        task.setOnSucceeded(event -> {
            Map<String, Double> exchangeRates = task.getValue();
            
            if (exchangeRates.isEmpty()) {
                graphProgress.setVisible(false);
                showAlert("Warning", "No Data Available", 
                         "No exchange rate data found for the selected currency and date range.");
                return;
            }
            
            createExchangeRateChart(exchangeRates, currency);
            graphProgress.setVisible(false);
        });
        
        task.setOnFailed(event -> {
            graphProgress.setVisible(false);
            
            Throwable exception = task.getException();
            System.err.println("Error generating graph: " + exception.getMessage());
            exception.printStackTrace();
            
            showAlert("Error", "Failed to Generate Graph", 
                     "An error occurred: " + exception.getMessage());
        });
        
        executorService.submit(task);
    }
    
    /**
     * Create an exchange rate chart from the data
     */
    private void createExchangeRateChart(Map<String, Double> exchangeRates, String currency) {
        // Clear previous chart data
        exchangeRateChart.getData().clear();
        
        // Create a new series
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(currency + " Exchange Rate");
        
        // Sort the exchange rates by date
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(exchangeRates.entrySet());
        sortedEntries.sort(Comparator.comparing(Map.Entry::getKey));
        
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd");
        
        // Add data points to the series
        int index = 0;
        Map<Integer, String> dateLabels = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : sortedEntries) {
            try {
                String dateStr = entry.getKey();
                double rate = entry.getValue();
                
                // Parse the date for better display
                Date date = inputFormat.parse(dateStr);
                String formattedDate = displayFormat.format(date);
                
                // Store the index-to-date mapping for axis labels
                dateLabels.put(index, formattedDate);
                
                // Use index for x-axis to ensure even spacing
                series.getData().add(new XYChart.Data<>(index++, rate));
                
            } catch (Exception e) {
                System.err.println("Error parsing date: " + e.getMessage());
            }
        }
        
        // Add the series to the chart
        Platform.runLater(() -> {
            exchangeRateChart.getData().add(series);
            
            // Update chart title and axis labels
            exchangeRateChart.setTitle(currency + " Exchange Rate (" + 
                                     graphStartDatePicker.getValue() + " to " + 
                                     graphEndDatePicker.getValue() + ")");
            
            // Customize the chart for better visibility
            if (series.getData().size() > 0) {
                // Improve the y-axis scaling
                NumberAxis yAxis = (NumberAxis) exchangeRateChart.getYAxis();
                
                // Find min and max values
                DoubleSummaryStatistics stats = exchangeRates.values().stream()
                    .mapToDouble(Double::doubleValue)
                    .summaryStatistics();
                
                double min = stats.getMin();
                double max = stats.getMax();
                double padding = (max - min) * 0.1; // 10% padding
                
                if (max - min > 0.0001) { // Check if there's significant variation
                    yAxis.setAutoRanging(false);
                    yAxis.setLowerBound(Math.max(0, min - padding));
                    yAxis.setUpperBound(max + padding);
                    yAxis.setTickUnit((max - min) / 8);
                }
                
                // Customize the x-axis
                NumberAxis xAxis = (NumberAxis) exchangeRateChart.getXAxis();
                xAxis.setLabel("Date");
                
                // Add tooltips to the data points for better user experience
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    int dataIndex = data.getXValue().intValue();
                    String date = dateLabels.get(dataIndex);
                    double value = data.getYValue().doubleValue();
                    
                    Tooltip tooltip = new Tooltip(
                        date + "\nValue: " + String.format("%.4f", value));
                    
                    Tooltip.install(data.getNode(), tooltip);
                    
                    // Make the data points more visible
                    if (data.getNode() != null) {
                        data.getNode().setStyle("-fx-background-color: #3498db;");
                    }
                }
            }
            
            // Make the chart visible
            exchangeRateChart.setVisible(true);
        });
    }
    
    /**
     * Show an alert dialog with the specified parameters
     */
    private void showAlert(String title, String header, String content) {
        showAlert(Alert.AlertType.ERROR, title, header, content);
    }
    
    /**
     * Show an alert dialog with the specified parameters and type
     */
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Clean up resources when the controller is no longer needed
     */
    public void shutdown() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}