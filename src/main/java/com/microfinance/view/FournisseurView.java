package com.microfinance.view;

import com.microfinance.model.DemandeAchat;
import com.microfinance.model.Fournisseur;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;
import com.microfinance.repository.impl.FournisseurRepositoryImpl;
import com.microfinance.Util.UserSession;
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
 * FournisseurView — Rôle dans le flux Mourabaha :
 *
 *  ✅ Section 1 : CRUD Fournisseurs (ajouter, modifier, supprimer)
 *  ✅ Section 2 : Commandes en cours — Demandes au statut COMMANDE_FOURNISSEUR
 *                 → Bouton "📦 Marquer comme reçu" → passe à BIEN_RECU en base
 *  ✅ Section 3 : Biens reçus — Demandes au statut BIEN_RECU (prêtes pour le contrat)
 *
 * Connexion avec le flux :
 *  DemandeAgentView choisit le fournisseur → COMMANDE_FOURNISSEUR
 *  FournisseurView valide la réception    → BIEN_RECU
 *  DemandeAgentView crée le contrat       → CONTRAT_CREE
 */
public class FournisseurView {

    private BorderPane root;
    private Stage primaryStage;

    private final FournisseurRepositoryImpl  repoF    = new FournisseurRepositoryImpl();
    private final DemandeAchatRepositoryImpl repoD    = new DemandeAchatRepositoryImpl();

    // Section 1 — Fournisseurs
    private TableView<Fournisseur>      tableFournisseurs;
    private ObservableList<Fournisseur> dataFournisseurs = FXCollections.observableArrayList();
    private TextField nomField, telField, emailField, adresseField;
    private Fournisseur fournisseurSelectionne = null;

    // Section 2 — Commandes en cours (COMMANDE_FOURNISSEUR)
    private TableView<DemandeAchat>      tableCommandes;
    private ObservableList<DemandeAchat> dataCommandes = FXCollections.observableArrayList();

    // Section 3 — Biens reçus (BIEN_RECU)
    private TableView<DemandeAchat>      tableBiensRecus;
    private ObservableList<DemandeAchat> dataBiensRecus = FXCollections.observableArrayList();

    public FournisseurView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
        chargerTout();
    }

    // ═══════════════════════════════════════════════════
    // CONSTRUCTION
    // ═══════════════════════════════════════════════════
    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");
        root.setLeft(buildSidebar());

        VBox main = new VBox(0);
        main.getChildren().add(buildTopBar());

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(22);
        body.setPadding(new Insets(24));
        body.getChildren().addAll(
                buildInfoFlux(),
                buildSectionFournisseurs(),
                buildSectionCommandes(),
                buildSectionBiensRecus()
        );

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ── Bannière flux ──
    private VBox buildInfoFlux() {
        VBox card = new VBox(10); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:12;-fx-border-color:#27ae60;-fx-border-width:0 0 0 5;");
        Label titre = new Label("🏭  Rôle de cette page dans le flux Mourabaha");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); titre.setStyle("-fx-text-fill:#1b5e20;");
        Label desc = new Label(
                "1️⃣  Gérez vos fournisseurs (ajout, modification, suppression)\n" +
                        "2️⃣  Suivez les biens commandés aux fournisseurs (statut : COMMANDE_FOURNISSEUR)\n" +
                        "3️⃣  Validez la réception des biens → statut passe à BIEN_RECU\n" +
                        "4️⃣  L'agent peut ensuite créer le contrat depuis « Demandes Clients »"
        );
        desc.setFont(Font.font("Georgia", 13)); desc.setStyle("-fx-text-fill:#2e7d32;"); desc.setWrapText(true);
        card.getChildren().addAll(titre, desc);
        return card;
    }

    // ═══════════════════════════════════════════════════
    // SECTION 1 — CRUD FOURNISSEURS
    // ═══════════════════════════════════════════════════
    private VBox buildSectionFournisseurs() {
        VBox card = sectionCard("🏭  Gestion des Fournisseurs", "#1b5e20");

        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(10);
        nomField     = champ("Nom de l'entreprise *");
        telField     = champ("Téléphone");
        emailField   = champ("Email");
        adresseField = champ("Adresse");

        ColumnConstraints lbl = new ColumnConstraints(110);
        ColumnConstraints fld = new ColumnConstraints(); fld.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(lbl, fld, lbl, fld);
        grid.add(label("Entreprise *"), 0, 0); grid.add(nomField,     1, 0);
        grid.add(label("Téléphone"),    2, 0); grid.add(telField,     3, 0);
        grid.add(label("Email"),        0, 1); grid.add(emailField,   1, 1);
        grid.add(label("Adresse"),      2, 1); grid.add(adresseField, 3, 1);

        Label msgF = msgLabel();

        Button btnSave = btn("💾  Enregistrer", "#2e7d32", "#1b5e20");
        Button btnMod  = btn("✏️  Modifier",     "#2980b9", "#1a6090");
        Button btnDel  = btn("🗑️  Supprimer",    "#c0392b", "#922b21");
        Button btnClr  = btn("🔄  Effacer",      "#7f8c8d", "#636e72");

        btnSave.setOnAction(e -> {
            if (nomField.getText().trim().isEmpty()) { erreur(msgF, "Le nom de l'entreprise est obligatoire."); return; }
            try {
                if (fournisseurSelectionne == null) {
                    Fournisseur f = new Fournisseur(nomField.getText().trim(), telField.getText().trim(), emailField.getText().trim(), adresseField.getText().trim());
                    repoF.save(f);
                    succes(msgF, "✅  Fournisseur « " + f.getNomEntreprise() + " » ajouté.");
                } else {
                    fournisseurSelectionne.setNomEntreprise(nomField.getText().trim());
                    fournisseurSelectionne.setTelephone(telField.getText().trim());
                    fournisseurSelectionne.setEmail(emailField.getText().trim());
                    fournisseurSelectionne.setAdresse(adresseField.getText().trim());
                    repoF.update(fournisseurSelectionne);
                    succes(msgF, "✅  Fournisseur modifié.");
                }
                clearFormF(); chargerFournisseurs();
            } catch (Exception ex) { erreur(msgF, ex.getMessage()); }
        });

        btnMod.setOnAction(e -> {
            Fournisseur sel = tableFournisseurs.getSelectionModel().getSelectedItem();
            if (sel == null) { erreur(msgF, "Sélectionnez un fournisseur."); return; }
            fournisseurSelectionne = sel;
            nomField.setText(sel.getNomEntreprise()); telField.setText(sel.getTelephone());
            emailField.setText(sel.getEmail()); adresseField.setText(sel.getAdresse());
        });

        btnDel.setOnAction(e -> {
            Fournisseur sel = tableFournisseurs.getSelectionModel().getSelectedItem();
            if (sel == null) { erreur(msgF, "Sélectionnez un fournisseur."); return; }
            if (!confirmer("Supprimer « " + sel.getNomEntreprise() + " » ?\nAttention : les demandes liées perdront leur fournisseur.")) return;
            try { repoF.delete(sel.getIdFournisseur()); chargerFournisseurs(); succes(msgF, "✅  Fournisseur supprimé."); }
            catch (Exception ex) { erreur(msgF, ex.getMessage()); }
        });

        btnClr.setOnAction(e -> { clearFormF(); msgF.setText(""); });
        HBox btns = new HBox(10, btnSave, btnMod, btnDel, btnClr);

        // Table fournisseurs
        tableFournisseurs = new TableView<>();
        tableFournisseurs.setPrefHeight(200);
        tableFournisseurs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableFournisseurs.setPlaceholder(new Label("Aucun fournisseur enregistré."));

        TableColumn<Fournisseur, Long>   cId   = new TableColumn<>("ID");
        TableColumn<Fournisseur, String> cNom  = new TableColumn<>("Entreprise");
        TableColumn<Fournisseur, String> cTel  = new TableColumn<>("Téléphone");
        TableColumn<Fournisseur, String> cMail = new TableColumn<>("Email");
        TableColumn<Fournisseur, String> cAdr  = new TableColumn<>("Adresse");

        cId.setCellValueFactory(new PropertyValueFactory<>("idFournisseur"));
        cNom.setCellValueFactory(new PropertyValueFactory<>("nomEntreprise"));
        cTel.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        cMail.setCellValueFactory(new PropertyValueFactory<>("email"));
        cAdr.setCellValueFactory(new PropertyValueFactory<>("adresse"));

        tableFournisseurs.getColumns().addAll(cId, cNom, cTel, cMail, cAdr);
        tableFournisseurs.setItems(dataFournisseurs);

        card.getChildren().addAll(grid, btns, msgF, tableFournisseurs);
        return card;
    }

    // ═══════════════════════════════════════════════════
    // SECTION 2 — COMMANDES EN COURS (COMMANDE_FOURNISSEUR)
    // ═══════════════════════════════════════════════════
    private VBox buildSectionCommandes() {
        VBox card = sectionCard("🚚  Biens Commandés — En attente de livraison", "#8e44ad");

        Label info = new Label(
                "ℹ️  Ces biens ont été commandés auprès d'un fournisseur depuis « Demandes Clients ».\n" +
                        "     Lorsque le fournisseur livre, cliquez sur « ✅ Marquer comme reçu ».\n" +
                        "     Le statut passera à BIEN_RECU et l'agent pourra créer le contrat.");
        info.setFont(Font.font("Georgia", 12));
        info.setStyle("-fx-text-fill:#555;-fx-background-color:#f3e5f5;-fx-padding:10 14;-fx-background-radius:8;");
        info.setWrapText(true);

        tableCommandes = new TableView<>();
        tableCommandes.setPrefHeight(220);
        tableCommandes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableCommandes.setPlaceholder(new Label("Aucune commande en cours — passez des commandes depuis « Demandes Clients »."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        TableColumn<DemandeAchat, String> cDate   = new TableColumn<>("Date demande");
        TableColumn<DemandeAchat, String> cClient = new TableColumn<>("Client");
        TableColumn<DemandeAchat, String> cBien   = new TableColumn<>("Bien commandé");
        TableColumn<DemandeAchat, String> cPrix   = new TableColumn<>("Prix estimé");
        TableColumn<DemandeAchat, String> cFourn  = new TableColumn<>("Fournisseur");
        TableColumn<DemandeAchat, String> cAction = new TableColumn<>("Action");

        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDemande() != null ? d.getValue().getDateDemande().format(fmt) : "—"));
        cClient.setCellValueFactory(d -> new SimpleStringProperty(nomClient(d.getValue())));
        cBien.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescriptionBien()));
        cPrix.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPrixEstime() != null ? String.format("%.0f MRU", d.getValue().getPrixEstime()) : "—"));
        cFourn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomFournisseur() != null ? "🏭  " + d.getValue().getNomFournisseur() : "—"));
        cFourn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item); setStyle("-fx-text-fill:#8e44ad;-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        // Bouton "Marquer comme reçu" dans la colonne Action
        Label msgCommandes = msgLabel();
        cAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRecu = new Button("📦  Reçu");
            {
                String s  = "-fx-background-color:#16a085;-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:8;-fx-cursor:hand;";
                String sh = "-fx-background-color:#0e6655;-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:8;-fx-cursor:hand;";
                btnRecu.setStyle(s);
                btnRecu.setOnMouseEntered(e -> btnRecu.setStyle(sh));
                btnRecu.setOnMouseExited(e  -> btnRecu.setStyle(s));
                btnRecu.setOnAction(e -> {
                    DemandeAchat dem = getTableView().getItems().get(getIndex());
                    validerReception(dem, msgCommandes);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRecu);
            }
        });

        tableCommandes.getColumns().addAll(cDate, cClient, cBien, cPrix, cFourn, cAction);
        tableCommandes.setItems(dataCommandes);

        // Compteur
        HBox compteurBox = new HBox(12);
        VBox kpi = kpi("🚚", String.valueOf(dataCommandes.size()), "Commandes en cours", "#8e44ad", "#f3e5f5");
        HBox.setHgrow(kpi, Priority.ALWAYS);
        compteurBox.getChildren().add(kpi);

        Button btnActu = btn("🔄  Actualiser", "#8e44ad", "#6a1b9a");
        btnActu.setOnAction(e -> { chargerTout(); succes(msgCommandes, "✅  Liste actualisée."); });

        card.getChildren().addAll(info, compteurBox, tableCommandes, btnActu, msgCommandes);
        return card;
    }

    // ═══════════════════════════════════════════════════
    // SECTION 3 — BIENS REÇUS (BIEN_RECU)
    // ═══════════════════════════════════════════════════
    private VBox buildSectionBiensRecus() {
        VBox card = sectionCard("📦  Biens Reçus — Prêts pour la création du contrat", "#16a085");

        Label info = new Label(
                "ℹ️  Ces biens ont été réceptionnés. L'agent peut maintenant créer le contrat Mourabaha.\n" +
                        "     Allez dans « Demandes Clients », sélectionnez la demande, puis cliquez « Créer le contrat ».");
        info.setFont(Font.font("Georgia", 12));
        info.setStyle("-fx-text-fill:#555;-fx-background-color:#e8f8f5;-fx-padding:10 14;-fx-background-radius:8;");
        info.setWrapText(true);

        tableBiensRecus = new TableView<>();
        tableBiensRecus.setPrefHeight(180);
        tableBiensRecus.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableBiensRecus.setPlaceholder(new Label("Aucun bien reçu pour le moment."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<DemandeAchat, String> cDate2   = new TableColumn<>("Date");
        TableColumn<DemandeAchat, String> cClient2 = new TableColumn<>("Client");
        TableColumn<DemandeAchat, String> cBien2   = new TableColumn<>("Bien");
        TableColumn<DemandeAchat, String> cPrix2   = new TableColumn<>("Prix estimé");
        TableColumn<DemandeAchat, String> cFourn2  = new TableColumn<>("Fournisseur");
        TableColumn<DemandeAchat, String> cStat2   = new TableColumn<>("Statut");

        cDate2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDemande() != null ? d.getValue().getDateDemande().format(fmt) : "—"));
        cClient2.setCellValueFactory(d -> new SimpleStringProperty(nomClient(d.getValue())));
        cBien2.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescriptionBien()));
        cPrix2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPrixEstime() != null ? String.format("%.0f MRU", d.getValue().getPrixEstime()) : "—"));
        cFourn2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomFournisseur() != null ? d.getValue().getNomFournisseur() : "—"));
        cStat2.setCellValueFactory(d -> new SimpleStringProperty("📦  BIEN REÇU"));
        cStat2.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item); setStyle("-fx-text-fill:#16a085;-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        tableBiensRecus.getColumns().addAll(cDate2, cClient2, cBien2, cPrix2, cFourn2, cStat2);
        tableBiensRecus.setItems(dataBiensRecus);

        // Bouton raccourci vers Demandes Clients
        Button btnDemandes = btn("📨  Aller à « Demandes Clients » pour créer le contrat →", "#27ae60", "#1e8449");
        btnDemandes.setOnAction(e -> {
            DemandeAgentView v = new DemandeAgentView(primaryStage);
            primaryStage.getScene().setRoot(v.getRoot());
            primaryStage.setTitle("MicroFinance MRU — Demandes Clients");
        });

        card.getChildren().addAll(info, tableBiensRecus, btnDemandes);
        return card;
    }

    // ═══════════════════════════════════════════════════
    // LOGIQUE PRINCIPALE : Valider réception d'un bien
    // ═══════════════════════════════════════════════════
    private void validerReception(DemandeAchat dem, Label msgLbl) {
        if (!confirmer(
                "Confirmer la réception du bien :\n\n" +
                        "   📦  " + dem.getDescriptionBien() + "\n" +
                        "   🏭  Fournisseur : " + (dem.getNomFournisseur() != null ? dem.getNomFournisseur() : "—") + "\n" +
                        "   👤  Client : " + nomClient(dem) + "\n\n" +
                        "→ Le statut passera à BIEN_RECU.")) return;
        try {
            repoD.updateStatut(dem.getIdDemande(), "BIEN_RECU");
            succes(msgLbl, "✅  Réception confirmée pour « " + dem.getDescriptionBien() + " ».\n" +
                    "     L'agent peut maintenant créer le contrat depuis « Demandes Clients ».");
            chargerTout();
        } catch (Exception ex) {
            erreur(msgLbl, ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════
    // CHARGEMENT DEPUIS LA BASE DE DONNÉES
    // ═══════════════════════════════════════════════════
    private void chargerTout() {
        chargerFournisseurs();
        chargerDemandes();
    }

    private void chargerFournisseurs() {
        try { dataFournisseurs.setAll(repoF.findAll()); }
        catch (Exception e) { System.err.println("[FournisseurView] Fournisseurs : " + e.getMessage()); }
    }

    private void chargerDemandes() {
        try {
            List<DemandeAchat> toutes = repoD.findAll();

            // Commandes en cours
            ObservableList<DemandeAchat> commandes = FXCollections.observableArrayList();
            toutes.stream().filter(d -> "COMMANDE_FOURNISSEUR".equals(d.getStatutDemande())).forEach(commandes::add);
            dataCommandes.setAll(commandes);

            // Biens reçus (prêts pour contrat)
            ObservableList<DemandeAchat> recus = FXCollections.observableArrayList();
            toutes.stream().filter(d -> "BIEN_RECU".equals(d.getStatutDemande())).forEach(recus::add);
            dataBiensRecus.setAll(recus);

        } catch (Exception e) { System.err.println("[FournisseurView] Demandes : " + e.getMessage()); }
    }

    private void clearFormF() {
        fournisseurSelectionne = null;
        nomField.clear(); telField.clear(); emailField.clear(); adresseField.clear();
        tableFournisseurs.getSelectionModel().clearSelection();
    }

    // ═══════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════
    private VBox sectionCard(String titre, String color) {
        VBox card = new VBox(14); card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:" + color +
                ";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 15)); t.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().add(t); return card;
    }

    private VBox kpi(String icon, String val, String label, String color, String bg) {
        VBox c = new VBox(4); c.setPadding(new Insets(12)); c.setAlignment(Pos.CENTER_LEFT);
        c.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:10;-fx-border-color:" + color + ";-fx-border-width:0 0 0 4;");
        Label v = new Label(icon + "  " + val); v.setFont(Font.font("Georgia", FontWeight.BOLD, 16)); v.setStyle("-fx-text-fill:" + color + ";");
        Label l = new Label(label); l.setFont(Font.font("Georgia", 11)); l.setStyle("-fx-text-fill:#666;");
        c.getChildren().addAll(v, l); return c;
    }

    private TextField champ(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;" +
                "-fx-background-radius:8;-fx-padding:9 12;-fx-font-family:Georgia;-fx-font-size:13px;");
        return tf;
    }

    private Label label(String t) {
        Label l = new Label(t); l.setFont(Font.font("Georgia", FontWeight.BOLD, 12)); l.setStyle("-fx-text-fill:#333;"); return l;
    }

    private Label msgLabel() {
        Label l = new Label(""); l.setFont(Font.font("Georgia", 13)); l.setWrapText(true); return l;
    }

    private Button btn(String text, String bg, String hover) {
        Button b = new Button(text);
        String s  = "-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        b.setStyle(s); b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s)); return b;
    }

    private boolean confirmer(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null); return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void succes(Label l, String msg) { l.setText(msg); l.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;"); }
    private void erreur(Label l, String msg)  { l.setText("❌  " + msg); l.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;"); }

    private String nomClient(DemandeAchat dem) {
        if (dem.getIdClient() == null) return "—";
        return dem.getIdClient().getNom() != null ? dem.getIdClient().getNom()
                : "Client #" + dem.getIdClient().getIdClient();
    }

    // ── TopBar + Sidebar ──
    private HBox buildTopBar() {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label("🏭  Fournisseurs & Suivi des Livraisons");
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 18)); t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label user = new Label("👩‍💼  " + UserSession.getInstance().getNom()); user.setFont(Font.font("Georgia", 13)); user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t, sp, user); return bar;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(0); sb.setPrefWidth(230); sb.setStyle("-fx-background-color:#1b5e20;");
        VBox logoBox = new VBox(8); logoBox.setAlignment(Pos.CENTER); logoBox.setPadding(new Insets(28, 20, 28, 20)); logoBox.setStyle("-fx-background-color:#145214;");

        Label appName = new Label("MicroFinance"); appName.setFont(Font.font("Georgia", FontWeight.BOLD, 16)); appName.setStyle("-fx-text-fill:white;");
        Label mru = new Label("MRU"); mru.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); mru.setStyle("-fx-text-fill:#a5d6a7;");
        Label badge = new Label("👩‍💼  AGENT");
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;" +
                "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll( appName, mru, badge);

        VBox menu = new VBox(4); menu.setPadding(new Insets(18, 10, 18, 10));
        Button bA = sb2("🏠", "Accueil",              false);
        Button bD = sb2("📨", "Demandes Clients",     false);
        Button bF = sb2("🏭", "Fournisseurs & Biens", true);   // ← actif
        Button bC = sb2("📋", "Contrats Mourabaha",   false);
        Button bE = sb2("✅", "Paiements Échéances",  false);
        Button bH = sb2("📊", "Historique", false);
        bH.setOnAction(e -> nav(new HistoriqueView(primaryStage).getRoot(), "Historique"));

        bA.setOnAction(e -> nav(new AccueilView(primaryStage).getRoot(),       "Accueil"));
        bD.setOnAction(e -> nav(new DemandeAgentView(primaryStage).getRoot(),  "Demandes Clients"));
        bF.setOnAction(e -> nav(new FournisseurView(primaryStage).getRoot(),   "Fournisseurs"));
        bC.setOnAction(e -> nav(new ContratView(primaryStage).getRoot(),       "Contrats"));
        bE.setOnAction(e -> nav(new EcheanceView(primaryStage).getRoot(),      "Paiements"));

        menu.getChildren().addAll(bA, bD, bF, bC, bE,bH);

        VBox bottom = new VBox(8); bottom.setPadding(new Insets(20)); VBox.setVgrow(bottom, Priority.ALWAYS); bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout = new Button("⬅   Déconnexion"); logout.setMaxWidth(Double.MAX_VALUE);
        String lo = "-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh = "-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        logout.setStyle(lo); logout.setOnMouseEntered(e -> logout.setStyle(lh)); logout.setOnMouseExited(e -> logout.setStyle(lo));
        logout.setOnAction(e -> { UserSession.getInstance().logout(); nav(new LoginView(primaryStage).getRoot(), "Connexion"); });
        bottom.getChildren().add(logout);
        sb.getChildren().addAll(logoBox, menu, bottom);
        return sb;
    }

    private Button sb2(String icon, String text, boolean active) {
        Button btn = new Button(icon + "   " + text); btn.setMaxWidth(Double.MAX_VALUE); btn.setAlignment(Pos.CENTER_LEFT);
        String base  = active ? "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;"
                : "-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover = "-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base); btn.setOnMouseEntered(e -> { if (!active) btn.setStyle(hover); }); btn.setOnMouseExited(e -> { if (!active) btn.setStyle(base); });
        return btn;
    }

    private void nav(javafx.scene.Parent r, String titre) {
        primaryStage.getScene().setRoot(r);
        primaryStage.setTitle("MicroFinance MRU — " + titre);
    }

    public BorderPane getRoot() { return root; }
}