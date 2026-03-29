package com.microfinance.view;

import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class SidebarComponent {

    public static VBox build(Stage primaryStage, String active) {

        VBox menu = new VBox();

        Button bH = new Button("📊 Historique");

        bH.setOnAction(e -> {
            HistoriqueView v = new HistoriqueView(primaryStage);
            primaryStage.getScene().setRoot(v.getRoot());
            primaryStage.setTitle("MicroFinance MRU — Historique");
        });

        menu.getChildren().add(bH);

        return menu;
    }
}