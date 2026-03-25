package com.microfinance;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger l'interface FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setScene(scene);
        stage.setTitle("Gestion Microfinance");
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // Démarre l'application JavaFX
    }
}
