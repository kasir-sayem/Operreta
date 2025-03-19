package main.java.com.operetta;

import java.io.File;
import java.io.IOException;

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
            File dataDir = new File("c:/data");
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            // Load the main FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Parent root = loader.load();
            
            // Set up the main scene
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Operetta Database Application");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}