package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Ładowanie pliku FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/StartMenu.fxml"));

        // Tworzenie sceny
        Scene scene = new Scene(loader.load(), 450, 700); // Wymiary okna

        primaryStage.setTitle("Evolution Generator - Konfiguracja");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}