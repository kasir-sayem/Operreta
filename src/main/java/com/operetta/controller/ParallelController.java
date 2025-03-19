package com.operetta.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the parallel programming demonstration
 */
public class ParallelController implements Initializable {

    @FXML
    private Label label1;
    
    @FXML
    private Label label2;
    
    @FXML
    private Button startButton;
    
    @FXML
    private ProgressBar progressBar1;
    
    @FXML
    private ProgressBar progressBar2;
    
    private ExecutorService executorService;
    private Task<Void> task1;
    private Task<Void> task2;
    private boolean running = false;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newFixedThreadPool(2);
        
        // Initialize UI elements
        label1.setText("Thread 1 (1 second)");
        label2.setText("Thread 2 (2 seconds)");
        progressBar1.setProgress(0);
        progressBar2.setProgress(0);
    }
    
    /**
     * Start or stop the parallel threads
     */
    @FXML
    private void toggleThreads() {
        if (!running) {
            startThreads();
            startButton.setText("Stop");
        } else {
            stopThreads();
            startButton.setText("Start");
        }
        running = !running;
    }
    
    /**
     * Start the parallel threads
     */
    private void startThreads() {
        // Task for thread 1 - updates every 1 second
        task1 = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                while (!isCancelled()) {
                    final int currentCount = count;
                    Platform.runLater(() -> {
                        label1.setText("Thread 1: Count " + currentCount);
                        progressBar1.setProgress((currentCount % 10) / 10.0);
                    });
                    count++;
                    Thread.sleep(1000); // 1 second interval
                }
                return null;
            }
        };
        
        // Task for thread 2 - updates every 2 seconds
        task2 = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int count = 0;
                while (!isCancelled()) {
                    final int currentCount = count;
                    Platform.runLater(() -> {
                        label2.setText("Thread 2: Count " + currentCount);
                        progressBar2.setProgress((currentCount % 5) / 5.0);
                    });
                    count++;
                    Thread.sleep(2000); // 2 second interval
                }
                return null;
            }
        };
        
        // Start both tasks in the executor service
        executorService.submit(task1);
        executorService.submit(task2);
    }
    
    /**
     * Stop the parallel threads
     */
    private void stopThreads() {
        if (task1 != null) {
            task1.cancel();
        }
        if (task2 != null) {
            task2.cancel();
        }
        
        // Reset UI
        label1.setText("Thread 1 (1 second)");
        label2.setText("Thread 2 (2 seconds)");
        progressBar1.setProgress(0);
        progressBar2.setProgress(0);
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