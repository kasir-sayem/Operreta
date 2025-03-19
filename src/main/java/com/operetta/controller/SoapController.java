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
        
        startDatePicker.setValue(oneMonthAgo);
        endDatePicker.setValue(now);
        
        graphStartDatePicker.setValue(oneMonthAgo);
        graphEndDatePicker.setValue(now);
        
        // Initialize currency combo boxes
        loadCurrencies();
        
        // Set up button actions
        downloadAllButton.setOnAction(event -> downloadAllData());
        downloadFilteredButton.setOnAction(event -> downloadFilteredData());
        generateGraphButton.setOnAction(event -> generateGraph());
    }
    
    /**
     * Load available currencies from the MNB web service
     */
    private void loadCurrencies() {
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return soapService.getCurrencies();
            }
        };
        
        task.setOnSucceeded(event -> {
            List<String> currencies = task.getValue();
            currencyComboBox.setItems(FXCollections.observableArrayList(currencies));
            graphCurrencyComboBox.setItems(FXCollections.observableArrayList(currencies));
            
            if (!currencies.isEmpty()) {
                currencyComboBox.setValue(currencies.get(0));
                graphCurrencyComboBox.setValue(currencies.get(0));
            }
        });
        
        task.setOnFailed(event -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load currencies");
            alert.setContentText("An error occurred while loading currencies: " + 
                               task.getException().getMessage());
            alert.showAndWait();
        });
        
        executorService.submit(task);
    }
    
    /**
     * Download all exchange rate data to a file
     */
    private void downloadAllData() {
        downloadProgress.setVisible(true);
        downloadStatusLabel.setText("Downloading...");
        
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
            downloadStatusLabel.setText("Download completed successfully.");
        });
        
        task.setOnFailed(event -> {
            downloadProgress.setVisible(false);
            downloadStatusLabel.setText("Download failed: " + task.getException().getMessage());
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
        downloadFilteredStatusLabel.setText("Downloading...");
        
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
            downloadFilteredStatusLabel.setText("Download completed successfully.");
        });
        
        task.setOnFailed(event -> {
            downloadFilteredProgress.setVisible(false);
            downloadFilteredStatusLabel.setText("Download failed: " + task.getException().getMessage());
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Missing Data");
            alert.setContentText("Please select all required fields.");
            alert.showAndWait();
            return;
        }
        
        if (endDate.isBefore(startDate)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Invalid Date Range");
            alert.setContentText("End date must be after start date.");
            alert.showAndWait();
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
            createExchangeRateChart(exchangeRates, currency);
            graphProgress.setVisible(false);
        });
        
        task.setOnFailed(event -> {
            graphProgress.setVisible(false);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to Generate Graph");
            alert.setContentText("An error occurred: " + task.getException().getMessage());
            alert.showAndWait();
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
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        // Add data points to the series
        int index = 0;
        for (Map.Entry<String, Double> entry : sortedEntries) {
            try {
                String dateStr = entry.getKey();
                double rate = entry.getValue();
                
                // Use index for x-axis to ensure even spacing
                series.getData().add(new XYChart.Data<>(index++, rate));
                
            } catch (Exception e) {
                System.err.println("Error parsing date: " + e.getMessage());
            }
        }
        
        // Add the series to the chart
        Platform.runLater(() -> {
            exchangeRateChart.getData().add(series);
            
            // Update chart title
            exchangeRateChart.setTitle(currency + " Exchange Rate");
            
            // Make the chart visible
            exchangeRateChart.setVisible(true);
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