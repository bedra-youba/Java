package com.microfinance.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Toujours démarrer sur la page de connexion
        LoginView loginView = new LoginView(primaryStage);
        Scene scene = new Scene(loginView.getRoot(), 1200, 720);

        primaryStage.setTitle("MicroFinance MRU — Connexion");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}