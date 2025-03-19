package com.operetta.controller;

import com.operetta.service.ForexService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ForexController {

    @FXML private BorderPane forexPane;
    
    // All the different views
    @FXML private VBox accountInfoView;
    @FXML private VBox currentPricesView;
    @FXML private VBox historicalPricesView;
    @FXML private VBox positionOpeningView;
    @FXML private VBox positionClosingView;
    @FXML private VBox openedPositionsView;
    @FXML private VBox defaultView;
    
    // Account Information elements
    @FXML private TableView<AccountProperty> accountInfoTable;
    @FXML private TableColumn<AccountProperty, String> propertyColumn;
    @FXML private TableColumn<AccountProperty, String> valueColumn;
    
    // Current Prices elements
    @FXML private ComboBox<String> currencyPairCombo;
    @FXML private TextArea priceResultArea;
    
    // Historical Prices elements
    @FXML private ComboBox<String> histCurrencyPairCombo;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<CandleData> historicalPricesTable;
    @FXML private TableColumn<CandleData, String> dateColumn;
    @FXML private TableColumn<CandleData, Double> openColumn;
    @FXML private TableColumn<CandleData, Double> highColumn;
    @FXML private TableColumn<CandleData, Double> lowColumn;
    @FXML private TableColumn<CandleData, Double> closeColumn;
    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    
    // Position Opening elements
    @FXML private ComboBox<String> openPositionCurrencyCombo;
    @FXML private TextField quantityField;
    @FXML private RadioButton buyRadioButton;
    @FXML private RadioButton sellRadioButton;
    @FXML private ToggleGroup directionGroup;
    @FXML private TextArea openPositionResultArea;
    
    // Position Closing elements
    @FXML private TextField positionIdField;
    @FXML private TextArea closePositionResultArea;
    
    // Opened Positions elements
    @FXML private TableView<PositionData> openPositionsTable;
    @FXML private TableColumn<PositionData, String> positionIdColumn;
    @FXML private TableColumn<PositionData, String> instrumentColumn;
    @FXML private TableColumn<PositionData, Double> unitsColumn;
    @FXML private TableColumn<PositionData, Double> priceColumn;
    @FXML private TableColumn<PositionData, Double> profitColumn;
    @FXML private TableColumn<PositionData, String> openTimeColumn;
    
    // Service for Forex operations
    private ForexService forexService;
    
    // Currency pairs
    private final List<String> currencyPairs = Arrays.asList(
            "EUR_USD", "USD_JPY", "GBP_USD", "USD_CHF", 
            "AUD_USD", "NZD_USD", "EUR_GBP", "USD_CAD", "EUR_JPY"
    );
    
    @FXML
    public void initialize() {
        forexService = new ForexService();
        
        // Initialize all ComboBoxes with currency pairs
        currencyPairCombo.setItems(FXCollections.observableArrayList(currencyPairs));
        histCurrencyPairCombo.setItems(FXCollections.observableArrayList(currencyPairs));
        openPositionCurrencyCombo.setItems(FXCollections.observableArrayList(currencyPairs));
        
        // Select first pair by default
        currencyPairCombo.getSelectionModel().selectFirst();
        histCurrencyPairCombo.getSelectionModel().selectFirst();
        openPositionCurrencyCombo.getSelectionModel().selectFirst();
        
        // Setup DatePickers
        setupDatePickers();
        
        // Setup TableViews
        setupAccountInfoTable();
        setupHistoricalPricesTable();
        setupOpenPositionsTable();
        
        // Show default view initially
        showView(defaultView);
    }
    
    private void setupDatePickers() {
        // Setup date pickers with default values (today and one week ago)
        LocalDate today = LocalDate.now();
        LocalDate oneWeekAgo = today.minusWeeks(1);
        
        startDatePicker.setValue(oneWeekAgo);
        endDatePicker.setValue(today);
        
        // Date format converter
        StringConverter<LocalDate> converter = new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };
        
        startDatePicker.setConverter(converter);
        endDatePicker.setConverter(converter);
    }
    
    private void setupAccountInfoTable() {
        propertyColumn.setCellValueFactory(new PropertyValueFactory<>("property"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
    }
    
    private void setupHistoricalPricesTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        openColumn.setCellValueFactory(new PropertyValueFactory<>("open"));
        highColumn.setCellValueFactory(new PropertyValueFactory<>("high"));
        lowColumn.setCellValueFactory(new PropertyValueFactory<>("low"));
        closeColumn.setCellValueFactory(new PropertyValueFactory<>("close"));
    }
    
    private void setupOpenPositionsTable() {
        positionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        instrumentColumn.setCellValueFactory(new PropertyValueFactory<>("instrument"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        profitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));
        openTimeColumn.setCellValueFactory(new PropertyValueFactory<>("openTime"));
    }
    
    // Method to show a specific view and hide others
    private void showView(VBox viewToShow) {
        // Hide all views
        accountInfoView.setVisible(false);
        currentPricesView.setVisible(false);
        historicalPricesView.setVisible(false);
        positionOpeningView.setVisible(false);
        positionClosingView.setVisible(false);
        openedPositionsView.setVisible(false);
        defaultView.setVisible(false);
        
        // Show selected view
        viewToShow.setVisible(true);
    }
    
    // Method called from MainController to determine which view to show
    public void showSubView(String viewName) {
        switch (viewName) {
            case "Account Information":
                showView(accountInfoView);
                refreshAccountInfo();
                break;
            case "Current Prices":
                showView(currentPricesView);
                break;
            case "Historical Prices":
                showView(historicalPricesView);
                break;
            case "Position Opening":
                showView(positionOpeningView);
                break;
            case "Position Closing":
                showView(positionClosingView);
                break;
            case "Opened Positions":
                showView(openedPositionsView);
                refreshOpenPositions();
                break;
            default:
                showView(defaultView);
                break;
        }
    }
    
    // Account Information methods
    @FXML
    private void refreshAccountInfo() {
        try {
            Map<String, String> accountDetails = forexService.getAccountInformation();
            ObservableList<AccountProperty> accountProperties = FXCollections.observableArrayList();
            
            for (Map.Entry<String, String> entry : accountDetails.entrySet()) {
                accountProperties.add(new AccountProperty(entry.getKey(), entry.getValue()));
            }
            
            accountInfoTable.setItems(accountProperties);
        } catch (Exception e) {
            showError("Failed to load account information", e);
        }
    }
    
    // Current Prices methods
    @FXML
    private void getCurrentPrice() {
        String instrument = currencyPairCombo.getValue();
        if (instrument == null || instrument.isEmpty()) {
            showAlert("Error", "Please select a currency pair");
            return;
        }
        
        try {
            String price = forexService.getCurrentPrice(instrument);
            priceResultArea.setText(price);
        } catch (Exception e) {
            showError("Failed to get current price", e);
        }
    }
    
    // Historical Prices methods
    @FXML
    private void getHistoricalPrices() {
        String instrument = histCurrencyPairCombo.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (instrument == null || instrument.isEmpty()) {
            showAlert("Error", "Please select a currency pair");
            return;
        }
        
        if (startDate == null || endDate == null) {
            showAlert("Error", "Please select start and end dates");
            return;
        }
        
        if (endDate.isBefore(startDate)) {
            showAlert("Error", "End date cannot be before start date");
            return;
        }
        
        try {
            List<CandleData> candles = forexService.getHistoricalPrices(
                    instrument, 
                    startDate.format(DateTimeFormatter.ISO_DATE), 
                    endDate.format(DateTimeFormatter.ISO_DATE)
            );
            
            if (candles.isEmpty()) {
                showAlert("Information", "No historical data found for the selected period");
                return;
            }
            
            // Update table
            historicalPricesTable.setItems(FXCollections.observableArrayList(candles));
            
            // Update chart
            updatePriceChart(candles);
            
        } catch (Exception e) {
            // Use a simpler error message to avoid reflection issues
            showAlert("Error", "Failed to get historical prices: " + e.getMessage());
        }
    }
    
    private void updatePriceChart(List<CandleData> candles) {
        priceChart.getData().clear();
        
        XYChart.Series<String, Number> closeSeries = new XYChart.Series<>();
        closeSeries.setName("Close Price");
        
        for (CandleData candle : candles) {
            closeSeries.getData().add(new XYChart.Data<>(candle.getDate(), candle.getClose()));
        }
        
        priceChart.getData().add(closeSeries);
    }
    
    // Position Opening methods
    @FXML
    private void openPosition() {
        String instrument = openPositionCurrencyCombo.getValue();
        String quantityText = quantityField.getText();
        boolean isBuy = buyRadioButton.isSelected();
        
        if (instrument == null || instrument.isEmpty()) {
            showAlert("Error", "Please select a currency pair");
            return;
        }
        
        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                throw new NumberFormatException("Quantity must be positive");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid positive integer for quantity");
            return;
        }
        
        // Adjust for direction
        if (!isBuy) {
            quantity = -quantity;  // Negative for selling/short
        }
        
        try {
            String result = forexService.openPosition(instrument, quantity);
            openPositionResultArea.setText(result);
        } catch (Exception e) {
            showError("Failed to open position", e);
        }
    }
    
    // Position Closing methods
    @FXML
    private void closePosition() {
        String positionId = positionIdField.getText();
        
        if (positionId == null || positionId.isEmpty()) {
            showAlert("Error", "Please enter a position ID");
            return;
        }
        
        try {
            String result = forexService.closePosition(positionId);
            closePositionResultArea.setText(result);
        } catch (Exception e) {
            showError("Failed to close position", e);
        }
    }
    
    // Opened Positions methods
    @FXML
    private void refreshOpenPositions() {
        try {
            List<PositionData> positions = forexService.getOpenPositions();
            openPositionsTable.setItems(FXCollections.observableArrayList(positions));
        } catch (Exception e) {
            showError("Failed to load open positions", e);
        }
    }
    
    // // Utility methods for displaying alerts
    // private void showAlert(String title, String content) {
    //     Alert alert = new Alert(Alert.AlertType.WARNING);
    //     alert.setTitle(title);
    //     alert.setHeaderText(null);
    //     alert.setContentText(content);
    //     alert.showAndWait();
    // }

    private void showError(String message, Exception e) {
        // Simple version without using reflection
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(message);
        
        // Just show the message without trying to access the exception details
        alert.setContentText(e.getMessage() != null ? e.getMessage() : "An unknown error occurred");
        
        alert.showAndWait();
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // private void showError(String message, Exception e) {
    //     Alert alert = new Alert(Alert.AlertType.ERROR);
    //     alert.setTitle("Error");
    //     alert.setHeaderText(message);
        
    //     // Simplified error content that avoids reflection
    //     String errorContent = e.getMessage();
    //     if (errorContent == null || errorContent.isEmpty()) {
    //         errorContent = e.getClass().getName();
    //     }
    //     alert.setContentText(errorContent);
        
    //     // Simple text area for stack trace without expandable content
    //     StringBuilder stackTrace = new StringBuilder();
    //     for (StackTraceElement element : e.getStackTrace()) {
    //         stackTrace.append(element.toString()).append("\n");
    //         // Limit stack trace to avoid overwhelming the dialog
    //         if (stackTrace.length() > 500) {
    //             stackTrace.append("...");
    //             break;
    //         }
    //     }
        
    //     TextArea textArea = new TextArea(stackTrace.toString());
    //     textArea.setEditable(false);
    //     textArea.setWrapText(true);
    //     textArea.setMaxWidth(Double.MAX_VALUE);
    //     textArea.setMaxHeight(200);
        
    //     alert.getDialogPane().setExpandableContent(textArea);
    //     alert.getDialogPane().setExpanded(false);
        
    //     alert.showAndWait();
    // }
    
    // Data model classes
    public static class AccountProperty {
        private final String property;
        private final String value;
        
        public AccountProperty(String property, String value) {
            this.property = property;
            this.value = value;
        }
        
        public String getProperty() { return property; }
        public String getValue() { return value; }
    }
    
    public static class CandleData {
        private final String date;
        private final double open;
        private final double high;
        private final double low;
        private final double close;
        
        public CandleData(String date, double open, double high, double low, double close) {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }
        
        public String getDate() { return date; }
        public double getOpen() { return open; }
        public double getHigh() { return high; }
        public double getLow() { return low; }
        public double getClose() { return close; }
    }
    
    public static class PositionData {
        private final String id;
        private final String instrument;
        private final double units;
        private final double price;
        private final double profit;
        private final String openTime;
        
        public PositionData(String id, String instrument, double units, double price, double profit, String openTime) {
            this.id = id;
            this.instrument = instrument;
            this.units = units;
            this.price = price;
            this.profit = profit;
            this.openTime = openTime;
        }
        
        public String getId() { return id; }
        public String getInstrument() { return instrument; }
        public double getUnits() { return units; }
        public double getPrice() { return price; }
        public double getProfit() { return profit; }
        public String getOpenTime() { return openTime; }
    }
}