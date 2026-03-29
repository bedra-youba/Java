package com.microfinance.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalView {
    private BorderPane root;
    private Stage primaryStage;

    public MenuPrincipalView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #ecf0f1;");

        // En-tête
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        header.setStyle("-fx-background-color: #2c3e50;");

        Label title = new Label("MICROFINANCE MRU");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: white;");

        Label subtitle = new Label("Personne 2 - Processus Mourabaha");
        subtitle.setFont(Font.font("Arial", 16));
        subtitle.setStyle("-fx-text-fill: #bdc3c7;");

        header.getChildren().addAll(title, subtitle);
        root.setTop(header);

        // Menu principal (grille de boutons)
        GridPane menuGrid = new GridPane();
        menuGrid.setAlignment(Pos.CENTER);
        menuGrid.setHgap(20);
        menuGrid.setVgap(20);
        menuGrid.setPadding(new Insets(40));

        // ── Bouton Demandes Clients (NOUVEAU) ──
        Button demandeBtn = createMenuButton("📨 Demandes Clients", "Accepter / Refuser les demandes", "#e67e22");
        demandeBtn.setOnAction(e -> {
            DemandeAgentView demandeView = new DemandeAgentView(primaryStage);
            primaryStage.getScene().setRoot(demandeView.getRoot());
            primaryStage.setTitle("MicroFinance MRU — Demandes Clients");
        });

        // Bouton Fournisseurs
        Button fournisseurBtn = createMenuButton("🏭 Fournisseurs", "Gérer les fournisseurs", "#27ae60");
        fournisseurBtn.setOnAction(e -> {
            FournisseurView fournisseurView = new FournisseurView(primaryStage);
            root.setCenter(fournisseurView.getRoot());
        });

        // Bouton Contrats
        Button contratBtn = createMenuButton("📝 Contrats Mourabaha", "Créer et gérer les contrats", "#2980b9");
        contratBtn.setOnAction(e -> {
            ContratView contratView = new ContratView(primaryStage);
            root.setCenter(contratView.getRoot());
        });

        // Bouton Échéances
        Button echeanceBtn = createMenuButton("💰 Échéances", "Gérer les paiements", "#e67e22");
        echeanceBtn.setOnAction(e -> {
            EcheanceView echeanceView = new EcheanceView(primaryStage);
            root.setCenter(echeanceView.getRoot());
        });



        // Disposition des boutons (3 colonnes × 2 lignes)
        menuGrid.add(demandeBtn,     0, 0);   // ← Nouveau en première position
        menuGrid.add(fournisseurBtn, 1, 0);
        menuGrid.add(contratBtn,     2, 0);
        menuGrid.add(echeanceBtn,    0, 1);

        root.setCenter(menuGrid);

        // Pied de page avec bouton déconnexion
        VBox footer = new VBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 20, 10, 20));
        footer.setStyle("-fx-background-color: #bdc3c7;");

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        logoutBtn.setOnAction(e -> {
            LoginView loginView = new LoginView(primaryStage);
            primaryStage.getScene().setRoot(loginView.getRoot());
            primaryStage.setTitle("MicroFinance MRU - Connexion");
            primaryStage.setWidth(400);
            primaryStage.setHeight(300);
        });

        footer.getChildren().add(logoutBtn);
        root.setBottom(footer);
    }

    private Button createMenuButton(String title, String subtitle, String color) {
        Button btn = new Button(title + "\n" + subtitle);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 16px; -fx-padding: 30 40; -fx-font-weight: bold;");
        btn.setPrefSize(250, 120);
        return btn;
    }

    public BorderPane getRoot() {
        return root;
    }
}