package com.microfinance.view;

import com.microfinance.model.Echeance;
import com.microfinance.repository.impl.EcheanceRepositoryImpl;
import com.microfinance.Util.UserSession;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * EcheanceView — Tâches Agent Mourabaha :
 *
 *  ✅ 1. Gérer les échéances (voir toutes les échéances d'un contrat)
 *  ✅ 2. Enregistrer paiement d'une échéance
 *  ✅ 3. Recevoir alertes de risque (échéances en retard)
 *  ✅ 4. Vérifier statut de paiement
 *
 * Règle Mourabaha : pas de pénalité d'intérêt, seulement suivi du paiement.
 */
public class EcheanceView {

    private BorderPane root;
    private Stage primaryStage;

    private final EcheanceRepositoryImpl repo = new EcheanceRepositoryImpl();

    private TableView<Echeance> tableContrat;
    private TableView<Echeance> tableRetard;
    private ObservableList<Echeance> dataContrat = FXCollections.observableArrayList();
    private ObservableList<Echeance> dataRetard  = FXCollections.observableArrayList();

    private TextField idContratField;

    public EcheanceView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
        chargerRetards();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");
        root.setLeft(buildSidebar());

        VBox main = new VBox(0);
        main.getChildren().add(buildTopBar("Gestion des Échéances Mourabaha"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        body.getChildren().addAll(
                buildSectionRecherchePaiement(),
                buildSectionAlertes()
        );

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 1 — RECHERCHE + PAIEMENT D'ÉCHÉANCES
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionRecherchePaiement() {
        VBox card = sectionCard("✅  Échéances d'un Contrat — Enregistrer un Paiement", "#1b5e20");

        HBox searchRow = new HBox(12);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        idContratField = champ("Numéro du contrat Mourabaha");
        idContratField.setMaxWidth(260);

        Button btnRechercher = btn("🔍  Voir les échéances", "#2980b9", "#1a6090");
        Button btnTout       = btn("🔄  Tout réinitialiser",  "#7f8c8d", "#636e72");

        Label msgRecherche = new Label("");
        msgRecherche.setFont(Font.font("Georgia", 12));
        msgRecherche.setWrapText(true);

        btnRechercher.setOnAction(e -> {
            try {
                if (idContratField.getText().trim().isEmpty()) {
                    msgRecherche.setText("❌  Entrez l'ID du contrat.");
                    msgRecherche.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
                    return;
                }
                long idContrat = Long.parseLong(idContratField.getText().trim());
                List<Echeance> liste = repo.findByContratId(idContrat);
                dataContrat.setAll(liste);

                if (liste.isEmpty()) {
                    msgRecherche.setText("ℹ️  Aucune échéance pour le contrat #" + idContrat + ".");
                    msgRecherche.setStyle("-fx-text-fill:#2980b9;-fx-font-family:Georgia;");
                } else {
                    long payees   = liste.stream().filter(ec -> "PAYEE".equals(ec.getStatutPaiement())).count();
                    long impayees = liste.stream().filter(ec -> "IMPAYEE".equals(ec.getStatutPaiement())).count();
                    long retard   = liste.stream().filter(Echeance::estEnRetard).count();
                    double totalPaye = liste.stream()
                            .filter(ec -> "PAYEE".equals(ec.getStatutPaiement()))
                            .mapToDouble(Echeance::getMontant).sum();

                    msgRecherche.setText(String.format(
                            "Contrat #%d — Total : %d échéances | ✅ Payées : %d | 🕐 Impayées : %d | ⚠ En retard : %d | Total payé : %.2f MRU",
                            idContrat, liste.size(), payees, impayees, retard, totalPaye));
                    msgRecherche.setStyle("-fx-text-fill:#1b5e20;-fx-font-family:Georgia;-fx-font-weight:bold;");
                }
            } catch (NumberFormatException ex) {
                msgRecherche.setText("❌  L'ID du contrat doit être un nombre entier.");
                msgRecherche.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
            } catch (Exception ex) {
                msgRecherche.setText("❌  " + ex.getMessage());
                msgRecherche.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
            }
        });

        btnTout.setOnAction(e -> {
            dataContrat.clear();
            idContratField.clear();
            msgRecherche.setText("");
            chargerRetards();
        });

        searchRow.getChildren().addAll(idContratField, btnRechercher, btnTout);

        // Table échéances du contrat
        tableContrat = buildTableEcheances();
        tableContrat.setItems(dataContrat);
        tableContrat.setPrefHeight(260);

        // Bouton payer
        Label msgPayer = new Label("");
        msgPayer.setFont(Font.font("Georgia", 12));
        msgPayer.setWrapText(true);

        Button btnPayer = btn("💳  Enregistrer le paiement de l'échéance sélectionnée", "#2e7d32", "#1b5e20");
        btnPayer.setOnAction(e -> {
            Echeance sel = tableContrat.getSelectionModel().getSelectedItem();
            if (sel == null) {
                sel = tableRetard.getSelectionModel().getSelectedItem();
                if (sel == null) {
                    msgPayer.setText("❌  Sélectionnez une échéance à payer.");
                    msgPayer.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
                    return;
                }
            }
            if ("PAYEE".equals(sel.getStatutPaiement())) {
                msgPayer.setText("ℹ️  Cette échéance est déjà payée.");
                msgPayer.setStyle("-fx-text-fill:#2980b9;-fx-font-family:Georgia;");
                return;
            }

            Echeance finalSel = sel;
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    String.format("Confirmer le paiement de %.2f MRU pour l'échéance N°%d ?",
                            sel.getMontant(), sel.getNumeroEchange()),
                    ButtonType.YES, ButtonType.NO);
            conf.setTitle("Confirmation paiement");
            conf.setHeaderText(null);

            if (conf.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    repo.payerEcheance(finalSel.getIdEchange());

                    msgPayer.setText("✅  Paiement enregistré pour l'échéance N°" + finalSel.getNumeroEchange() +
                            " — Montant : " + String.format("%.2f MRU", finalSel.getMontant()) +
                            "\n     Si toutes les échéances sont payées, le contrat sera clôturé automatiquement.");
                    msgPayer.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;");

                    // Rafraîchir
                    if (!idContratField.getText().trim().isEmpty()) {
                        long idC = Long.parseLong(idContratField.getText().trim());
                        dataContrat.setAll(repo.findByContratId(idC));
                    }
                    chargerRetards();

                } catch (Exception ex) {
                    msgPayer.setText("❌  " + ex.getMessage());
                    msgPayer.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
                }
            }
        });

        card.getChildren().addAll(searchRow, msgRecherche, tableContrat, btnPayer, msgPayer);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 2 — ALERTES RETARD
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionAlertes() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color:#fef9e7;-fx-background-radius:16;" +
                "-fx-border-color:#f39c12;-fx-border-width:0 0 0 5;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        HBox hdr = new HBox(10);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label ico   = new Label("⚠️"); ico.setStyle("-fx-font-size:22px;");
        Label titre = new Label("Alertes de Risque — Échéances en Retard");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        titre.setStyle("-fx-text-fill:#e67e22;");
        hdr.getChildren().addAll(ico, titre);

        Label infoAlerte = new Label(
                "ℹ️  Ces échéances sont impayées et leur date d'échéance est dépassée.\n" +
                        "     Contactez le client pour régulariser la situation.");
        infoAlerte.setFont(Font.font("Georgia", 12));
        infoAlerte.setStyle("-fx-text-fill:#e67e22;");
        infoAlerte.setWrapText(true);

        tableRetard = buildTableEcheances();
        tableRetard.setItems(dataRetard);
        tableRetard.setPrefHeight(200);

        Button btnActu = btn("🔄  Actualiser les alertes", "#e67e22", "#d35400");
        btnActu.setOnAction(e -> chargerRetards());

        card.getChildren().addAll(hdr, infoAlerte, tableRetard, btnActu);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // TABLE RÉUTILISABLE
    // ═══════════════════════════════════════════════════════
    private TableView<Echeance> buildTableEcheances() {
        TableView<Echeance> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Aucune échéance à afficher."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<Echeance, Long>    cId   = new TableColumn<>("ID");
        TableColumn<Echeance, Integer> cNum  = new TableColumn<>("N°");
        TableColumn<Echeance, String>  cDate = new TableColumn<>("Date échéance");
        TableColumn<Echeance, Double>  cMont = new TableColumn<>("Montant (MRU)");
        TableColumn<Echeance, String>  cStat = new TableColumn<>("Statut");
        TableColumn<Echeance, Long>    cCont = new TableColumn<>("Contrat #");

        cId.setCellValueFactory(new PropertyValueFactory<>("idEchange"));
        cNum.setCellValueFactory(new PropertyValueFactory<>("numeroEchange"));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateEchange() != null
                        ? d.getValue().getDateEchange().format(fmt) : ""));
        cMont.setCellValueFactory(new PropertyValueFactory<>("montant"));
        cStat.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));
        cCont.setCellValueFactory(d -> new SimpleLongProperty(
                d.getValue().getContrat() != null
                        ? d.getValue().getContrat().getIdContrat() : 0L
        ).asObject());

        // Couleur du statut
        cStat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                Echeance row = getTableView().getItems().get(getIndex());
                if ("PAYEE".equals(item)) {
                    setText("✅  PAYÉE");
                    setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
                } else if (row != null && row.estEnRetard()) {
                    setText("⚠  EN RETARD");
                    setStyle("-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-font-family:Georgia;");
                } else {
                    setText("🕐  À PAYER");
                    setStyle("-fx-text-fill:#e67e22;-fx-font-family:Georgia;");
                }
            }
        });

        // Montant en gras
        cMont.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("%.2f", item));
                setStyle("-fx-font-family:Georgia;-fx-font-weight:bold;");
            }
        });

        t.getColumns().addAll(cId, cNum, cDate, cMont, cStat, cCont);
        return t;
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT
    // ═══════════════════════════════════════════════════════
    private void chargerRetards() {
        try {
            List<Echeance> retard = repo.findEcheancesEnRetard();
            dataRetard.setAll(retard);
        } catch (Exception e) {
            System.err.println("[EcheanceView] Retards : " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════
    private VBox sectionCard(String titre, String color) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:" + color +
                ";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        t.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().add(t);
        return card;
    }

    private TextField champ(String p) {
        TextField tf = new TextField();
        tf.setPromptText(p);
        tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;" +
                "-fx-background-radius:8;-fx-padding:9 12;-fx-font-family:Georgia;-fx-font-size:13px;");
        return tf;
    }

    private Button btn(String text, String bg, String hover) {
        Button b = new Button(text);
        String s  = "-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-family:Georgia;" +
                "-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:" + hover + ";-fx-text-fill:white;-fx-font-family:Georgia;" +
                "-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(sh));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }

    private void nav(javafx.scene.Parent r, String titre) {
        primaryStage.getScene().setRoot(r);
        primaryStage.setTitle("MicroFinance MRU — " + titre);
    }

    // ═══════════════════════════════════════════════════════
    // SIDEBAR — navigation complète avec tous les boutons
    // ═══════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(230);
        sb.setStyle("-fx-background-color:#1b5e20;");

        // ── Logo ──
        VBox logoBox = new VBox(8);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(28, 20, 28, 20));
        logoBox.setStyle("-fx-background-color:#145214;");
        Label appName = new Label("MicroFinance");
        appName.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        appName.setStyle("-fx-text-fill:white;");
        Label mru = new Label("MRU");
        mru.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        mru.setStyle("-fx-text-fill:#a5d6a7;");
        Label badge = new Label("👩‍💼  AGENT");
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;" +
                "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;" +
                "-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll( appName, mru, badge);

        // ── Menu ──
        VBox menu = new VBox(4);
        menu.setPadding(new Insets(18, 10, 18, 10));

        Button bA = sb2("🏠", "Accueil",             false);
        Button bD = sb2("📨", "Demandes Clients",    false);
        Button bF = sb2("🏭", "Fournisseurs & Biens",false);
        Button bC = sb2("📋", "Contrats Mourabaha",  false);
        Button bE = sb2("✅", "Paiements Échéances", true);   // ← actif
        Button bH = sb2("📊", "Historique",          false);

        bA.setOnAction(e -> nav(new AccueilView(primaryStage).getRoot(),      "Accueil"));
        bD.setOnAction(e -> nav(new DemandeAgentView(primaryStage).getRoot(), "Demandes Clients"));
        bF.setOnAction(e -> nav(new FournisseurView(primaryStage).getRoot(),  "Fournisseurs"));
        bC.setOnAction(e -> nav(new ContratView(primaryStage).getRoot(),      "Contrats"));
        bE.setOnAction(e -> nav(new EcheanceView(primaryStage).getRoot(),     "Paiements Échéances"));
        bH.setOnAction(e -> nav(new HistoriqueView(primaryStage).getRoot(),   "Historique"));

        menu.getChildren().addAll(bA, bD, bF, bC, bE, bH);

        // ── Déconnexion ──
        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(20));
        VBox.setVgrow(bottom, Priority.ALWAYS);
        bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout = new Button("⬅   Déconnexion");
        logout.setMaxWidth(Double.MAX_VALUE);
        String lo = "-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;" +
                "-fx-background-radius:10;-fx-cursor:hand;";
        String lh = "-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;" +
                "-fx-background-radius:10;-fx-cursor:hand;";
        logout.setStyle(lo);
        logout.setOnMouseEntered(e -> logout.setStyle(lh));
        logout.setOnMouseExited(e  -> logout.setStyle(lo));
        logout.setOnAction(e -> {
            UserSession.getInstance().logout();
            nav(new LoginView(primaryStage).getRoot(), "Connexion");
        });
        bottom.getChildren().add(logout);

        sb.getChildren().addAll(logoBox, menu, bottom);
        return sb;
    }

    private Button sb2(String icon, String text, boolean active) {
        Button btn = new Button(icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        String base  = active
                ? "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;" +
                "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;" +
                "-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;"
                : "-fx-background-color:transparent;-fx-text-fill:#a5d6a7;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;" +
                "-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover = "-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;" +
                "-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> { if (!active) btn.setStyle(hover); });
        btn.setOnMouseExited(e  -> { if (!active) btn.setStyle(base); });
        return btn;
    }

    private HBox buildTopBar(String titre) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label(titre);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label user = new Label("👩‍💼  " + UserSession.getInstance().getNom());
        user.setFont(Font.font("Georgia", 13));
        user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t, sp, user);
        return bar;
    }

    public BorderPane getRoot() { return root; }
}