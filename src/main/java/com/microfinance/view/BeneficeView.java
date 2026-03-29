package com.microfinance.view;

import com.microfinance.model.ContratMourabaha;
import com.microfinance.repository.impl.ContratMourabahaRepositoryImpl;
import com.microfinance.repository.impl.EcheanceRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Vue Bénéfices — Tableau de bord Directeur.
 *
 * Démontre (cours Java Avancé) :
 *  - Collections : List, ObservableList, stream/forEach
 *  - Agrégation SQL via JDBC (getBeneficeTotal)
 *  - Encapsulation : accès via getters
 *  - Exceptions : try-catch
 *  - Architecture en couches : Vue → Repository
 */
public class BeneficeView {

    private BorderPane root;
    private Stage primaryStage;

    private final ContratMourabahaRepositoryImpl contratRepo = new ContratMourabahaRepositoryImpl();
    private final EcheanceRepositoryImpl echeanceRepo = new EcheanceRepositoryImpl();

    private TableView<ContratMourabaha> table;
    private ObservableList<ContratMourabaha> data;

    // Labels des KPI
    private Label lblBeneficeTotal;
    private Label lblNbContrats;
    private Label lblNbRetard;

    public BeneficeView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
        chargerDonnees();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");
        root.setLeft(buildSidebar());

        VBox main = new VBox(0);
        main.getChildren().add(buildTopBar("Rapport des Bénéfices — Direction"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;");

        VBox body = new VBox(22);
        body.setPadding(new Insets(28));
        body.getChildren().addAll(
                buildKpiRow(),
                buildTableContrats(),
                buildPanneauRapport()
        );

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ─────────────────────────────────────────────────────
    // KPI — Cartes de statistiques
    // ─────────────────────────────────────────────────────
    private HBox buildKpiRow() {
        HBox row = new HBox(18);

        // Carte bénéfice total
        VBox cardBenef = buildKpiCard("💰", "Bénéfice total agence", "— MRU", "#27ae60", "#eafaf1");
        lblBeneficeTotal = (Label) ((VBox) cardBenef).getChildren().get(1);
        HBox.setHgrow(cardBenef, Priority.ALWAYS);

        // Carte nb contrats
        VBox cardNb = buildKpiCard("📋", "Contrats actifs", "—", "#2980b9", "#eaf4fb");
        lblNbContrats = (Label) ((VBox) cardNb).getChildren().get(1);
        HBox.setHgrow(cardNb, Priority.ALWAYS);

        // Carte retards
        VBox cardRetard = buildKpiCard("⚠️", "Échéances en retard", "—", "#e67e22", "#fef9e7");
        lblNbRetard = (Label) ((VBox) cardRetard).getChildren().get(1);
        HBox.setHgrow(cardRetard, Priority.ALWAYS);

        row.getChildren().addAll(cardBenef, cardNb, cardRetard);
        return row;
    }

    private VBox buildKpiCard(String icon, String label, String valeur, String color, String bg) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(22));
        card.setStyle(
                "-fx-background-color:" + bg + ";" +
                        "-fx-background-radius:14;" +
                        "-fx-border-color:" + color + ";" +
                        "-fx-border-width:0 0 0 4;" +
                        "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);"
        );
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size:26px;");
        Label valLbl  = new Label(valeur);
        valLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        valLbl.setStyle("-fx-text-fill:" + color + ";");
        Label lblLbl = new Label(label);
        lblLbl.setFont(Font.font("Georgia", 12));
        lblLbl.setStyle("-fx-text-fill:#666;");
        lblLbl.setWrapText(true);
        card.getChildren().addAll(iconLbl, valLbl, lblLbl);
        return card;
    }

    // ─────────────────────────────────────────────────────
    // TABLE CONTRATS AVEC BÉNÉFICE PAR CLIENT
    // ─────────────────────────────────────────────────────
    private VBox buildTableContrats() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");

        Label titre = new Label("📊  Bénéfice par Contrat / Client");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        titre.setStyle("-fx-text-fill:#1b5e20;");

        table = new TableView<>();
        table.setPrefHeight(300);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ContratMourabaha, Long>   colId     = new TableColumn<>("ID Contrat");
        TableColumn<ContratMourabaha, String> colClient = new TableColumn<>("Client");
        TableColumn<ContratMourabaha, Double> colAchat  = new TableColumn<>("Prix Achat (MRU)");
        TableColumn<ContratMourabaha, Double> colMarge  = new TableColumn<>("Marge %");
        TableColumn<ContratMourabaha, String> colBenef  = new TableColumn<>("Bénéfice (MRU)");
        TableColumn<ContratMourabaha, String> colStatut = new TableColumn<>("Statut");

        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idContrat"));
        colClient.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(
                        d.getValue().getClient() != null ? d.getValue().getClient().getNom() : ""));
        colAchat.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("prixAchatAgence"));
        colMarge.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("margeBeneficiaire"));

        // Bénéfice = prixAchat * marge / 100 (calculé en Java — encapsulation)
        colBenef.setCellValueFactory(d -> {
            ContratMourabaha c = d.getValue();
            double benef = c.getPrixAchatAgence() * c.getMargeBeneficiaire() / 100;
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.2f MRU", benef));
        });

        // Bénéfice coloré en vert
        colBenef.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        colStatut.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("statutContrat"));
        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                String color = "EN_COURS".equals(item) ? "#2980b9" : "CLOTURE".equals(item) ? "#27ae60" : "#e67e22";
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        table.getColumns().addAll(colId, colClient, colAchat, colMarge, colBenef, colStatut);
        data = FXCollections.observableArrayList();
        table.setItems(data);

        card.getChildren().addAll(titre, table);
        return card;
    }

    // ─────────────────────────────────────────────────────
    // PANNEAU RAPPORT TEXTE
    // ─────────────────────────────────────────────────────
    private VBox buildPanneauRapport() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(24));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");

        Label titre = new Label("📄  Rapport Synthèse Mourabaha");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        titre.setStyle("-fx-text-fill:#1b5e20;");

        Button btnRapport = buildBtn("📄  Générer le rapport", "#1b5e20", "#27ae60");
        TextArea rapportArea = new TextArea();
        rapportArea.setEditable(false);
        rapportArea.setPrefHeight(180);
        rapportArea.setStyle("-fx-font-family:Georgia;-fx-font-size:13px;-fx-background-radius:10;");

        btnRapport.setOnAction(e -> {
            try {
                // Génération du rapport en Java (Collections + agrégation)
                List<ContratMourabaha> contrats = contratRepo.findAll();
                double beneficeTotal = contratRepo.getBeneficeTotal();
                long nbEnCours = contrats.stream()
                        .filter(c -> "EN_COURS".equals(c.getStatutContrat()))
                        .count(); // Streams (Java moderne — cours)
                long nbClotures = contrats.stream()
                        .filter(c -> "CLOTURE".equals(c.getStatutContrat()))
                        .count();
                int nbRetard = echeanceRepo.findEcheancesEnRetard().size();

                StringBuilder sb = new StringBuilder();
                sb.append("═══════════════════════════════════════════════\n");
                sb.append("       RAPPORT MOURABAHA — MicroFinance MRU    \n");
                sb.append("═══════════════════════════════════════════════\n\n");
                sb.append(String.format("  📋  Total contrats       : %d\n", contrats.size()));
                sb.append(String.format("  🔵  Contrats en cours    : %d\n", nbEnCours));
                sb.append(String.format("  ✅  Contrats clôturés    : %d\n", nbClotures));
                sb.append(String.format("  ⚠️   Échéances en retard  : %d\n", nbRetard));
                sb.append(String.format("\n  💰  BÉNÉFICE TOTAL       : %.2f MRU\n\n", beneficeTotal));
                sb.append("  Détail par contrat :\n");
                sb.append("  ─────────────────────────────────────────\n");

                // Itération sur la collection (forEach — cours Collections)
                for (ContratMourabaha c : contrats) {
                    double benef = c.getPrixAchatAgence() * c.getMargeBeneficiaire() / 100;
                    String client = c.getClient() != null ? c.getClient().getNom() : "Inconnu";
                    sb.append(String.format("  Contrat #%-4d | %-15s | +%.2f MRU\n",
                            c.getIdContrat(), client, benef));
                }
                sb.append("═══════════════════════════════════════════════\n");
                rapportArea.setText(sb.toString());

            } catch (Exception ex) {
                rapportArea.setText("Erreur lors de la génération du rapport : " + ex.getMessage());
            }
        });

        card.getChildren().addAll(titre, btnRapport, rapportArea);
        return card;
    }

    // ─────────────────────────────────────────────────────
    // CHARGEMENT DES DONNÉES
    // ─────────────────────────────────────────────────────
    private void chargerDonnees() {
        try {
            List<ContratMourabaha> contrats = contratRepo.findAll();
            data.setAll(contrats);

            // Mettre à jour les KPI
            double benefice = contratRepo.getBeneficeTotal();
            lblBeneficeTotal.setText(String.format("%.2f MRU", benefice));
            lblNbContrats.setText(String.valueOf(contrats.size()));

            int nbRetard = echeanceRepo.findEcheancesEnRetard().size();
            lblNbRetard.setText(String.valueOf(nbRetard));
            if (nbRetard > 0) {
                lblNbRetard.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:22px;");
            }

        } catch (Exception e) {
            System.err.println("[BeneficeView] " + e.getMessage());
        }
    }

    // ─── Helpers ───
    private Button buildBtn(String text, String bg, String hover) {
        Button btn = new Button(text);
        String s  = "-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(s); btn.setOnMouseEntered(e->btn.setStyle(sh)); btn.setOnMouseExited(e->btn.setStyle(s));
        return btn;
    }

    private VBox buildSidebar() {
        VBox sidebar = new VBox(0); sidebar.setPrefWidth(230); sidebar.setStyle("-fx-background-color:#1b5e20;");
        VBox logoBox = new VBox(8); logoBox.setAlignment(Pos.CENTER); logoBox.setPadding(new Insets(28,20,28,20)); logoBox.setStyle("-fx-background-color:#145214;");

        Label appName = new Label("MicroFinance"); appName.setFont(Font.font("Georgia",FontWeight.BOLD,16)); appName.setStyle("-fx-text-fill:white;");
        Label mru = new Label("MRU"); mru.setFont(Font.font("Georgia",FontWeight.BOLD,13)); mru.setStyle("-fx-text-fill:#a5d6a7;");
        logoBox.getChildren().addAll(appName,mru);
        VBox menu = new VBox(4); menu.setPadding(new Insets(18,10,18,10));
        Button btnAccueil=sideBtn("🏠","Accueil",false); Button btnFournisseur=sideBtn("🏭","Fournisseurs",false);
        Button btnContrat=sideBtn("📋","Contrats Mourabaha",false); Button btnEcheance=sideBtn("✅","Paiements Échéances",false);
        Button btnHistorique=sideBtn("📊","Historique",false); Button btnBenefice=sideBtn("💰","Bénéfices",true);
        btnAccueil.setOnAction(e->{AccueilView v=new AccueilView(primaryStage);primaryStage.getScene().setRoot(v.getRoot());primaryStage.setTitle("MicroFinance MRU — Accueil");});
        btnFournisseur.setOnAction(e->{FournisseurView v=new FournisseurView(primaryStage);primaryStage.getScene().setRoot(v.getRoot());primaryStage.setTitle("MicroFinance MRU — Fournisseurs");});
        btnContrat.setOnAction(e->{ContratView v=new ContratView(primaryStage);primaryStage.getScene().setRoot(v.getRoot());primaryStage.setTitle("MicroFinance MRU — Contrats");});
        btnEcheance.setOnAction(e->{EcheanceView v=new EcheanceView(primaryStage);primaryStage.getScene().setRoot(v.getRoot());primaryStage.setTitle("MicroFinance MRU — Paiements");});
        btnBenefice.setOnAction(e->{BeneficeView v=new BeneficeView(primaryStage);primaryStage.getScene().setRoot(v.getRoot());primaryStage.setTitle("MicroFinance MRU — Bénéfices");});
        menu.getChildren().addAll(btnAccueil,btnFournisseur,btnContrat,btnEcheance,btnHistorique,btnBenefice);
        VBox bottom = new VBox(8); bottom.setPadding(new Insets(20)); VBox.setVgrow(bottom,Priority.ALWAYS); bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout = new Button("⬅   Déconnexion"); logout.setMaxWidth(Double.MAX_VALUE);
        String lo="-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh="-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        logout.setStyle(lo); logout.setOnMouseEntered(e->logout.setStyle(lh)); logout.setOnMouseExited(e->logout.setStyle(lo));
        logout.setOnAction(e->{LoginView lv=new LoginView(primaryStage);primaryStage.getScene().setRoot(lv.getRoot());primaryStage.setTitle("MicroFinance MRU — Connexion");});
        bottom.getChildren().add(logout);
        sidebar.getChildren().addAll(logoBox,menu,bottom);
        return sidebar;
    }
    private Button sideBtn(String icon, String text, boolean active) {
        Button btn = new Button(icon+"   "+text); btn.setMaxWidth(Double.MAX_VALUE); btn.setAlignment(Pos.CENTER_LEFT);
        String base = active ? "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;"
                : "-fx-baczkground-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover = "-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base); btn.setOnMouseEntered(e->{if(!active)btn.setStyle(hover);}); btn.setOnMouseExited(e->{if(!active)btn.setStyle(base);});
        return btn;
    }
    private HBox buildTopBar(String titre) {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(16,30,16,30));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia",FontWeight.BOLD,20)); t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp = new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
        Label user = new Label("👨‍💼  Directeur"); user.setFont(Font.font("Georgia",13)); user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t,sp,user);
        return bar;
    }
    public BorderPane getRoot() { return root; }
}