package com.microfinance.view;

import com.microfinance.model.Client;
import com.microfinance.model.ContratMourabaha;
import com.microfinance.repository.impl.ContratMourabahaRepositoryImpl;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;
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

/**
 * ContratView — Création et gestion des contrats Mourabaha.
 *
 * Trois constructeurs :
 *  new ContratView(stage)                         → usage normal
 *  new ContratView(stage, idClient, prix)          → depuis demande acceptée (ancien)
 *  new ContratView(stage, idClient, prix, idDemande) → depuis DemandeAgentView (nouveau)
 *    → après création du contrat, met le statut de la demande à CONTRAT_CREE
 */
public class ContratView {

    private BorderPane root;
    private Stage primaryStage;

    private final ContratMourabahaRepositoryImpl repo        = new ContratMourabahaRepositoryImpl();
    private final DemandeAchatRepositoryImpl     demandeRepo = new DemandeAchatRepositoryImpl();

    private TableView<ContratMourabaha>      table;
    private ObservableList<ContratMourabaha> data;

    private TextField   idClientField, prixAchatField, margeField, dureeField;
    private ComboBox<String> statutCombo;
    private Label       prixVenteLabel, mensualiteLabel;

    private ContratMourabaha contratSelectionne = null;

    // Pré-remplissage depuis une demande
    private final long    prefilledIdClient;
    private final double  prefilledPrix;
    private final long    idDemandeSource;   // -1 si pas de demande source
    private final boolean fromDemande;

    // ── Constructeur normal ──
    public ContratView(Stage primaryStage) {
        this(primaryStage, -1, 0, -1);
    }

    // ── Constructeur depuis demande (sans idDemande) ──
    public ContratView(Stage primaryStage, long idClient, double prixEstime) {
        this(primaryStage, idClient, prixEstime, -1);
    }

    // ── Constructeur depuis DemandeAgentView (avec idDemande) ──
    public ContratView(Stage primaryStage, long idClient, double prixEstime, long idDemande) {
        this.primaryStage      = primaryStage;
        this.prefilledIdClient = idClient;
        this.prefilledPrix     = prixEstime;
        this.idDemandeSource   = idDemande;
        this.fromDemande       = (idClient > 0);
        createView();
        chargerDonnees();
        if (fromDemande) preRemplir();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");
        root.setLeft(buildSidebar());

        VBox main = new VBox(0);
        main.getChildren().add(buildTopBar("Contrats Mourabaha"));

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));

        if (fromDemande) body.getChildren().add(buildBannerDemande());
        body.getChildren().addAll(buildInfoProcessus(), buildFormContrat(), buildTableContrats());

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ── Bannière "venu d'une demande acceptée" ──
    private VBox buildBannerDemande() {
        VBox card = new VBox(8); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:#fff8e1;-fx-background-radius:12;-fx-border-color:#f1c40f;-fx-border-width:0 0 0 5;");
        Label titre = new Label("🎉  Demande acceptée — Formulaire pré-rempli !");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); titre.setStyle("-fx-text-fill:#e67e22;");
        Label desc = new Label(
                "   Client #" + prefilledIdClient + " pré-rempli automatiquement.\n" +
                        "   Vérifiez le prix, saisissez la marge et la durée, puis créez le contrat.\n" +
                        "   ✅  La demande sera automatiquement marquée CONTRAT_CREE après création.");
        desc.setFont(Font.font("Georgia", 13)); desc.setStyle("-fx-text-fill:#7d6608;"); desc.setWrapText(true);
        card.getChildren().addAll(titre, desc); return card;
    }

    private void preRemplir() {
        if (idClientField  != null) idClientField.setText(String.valueOf(prefilledIdClient));
        if (prixAchatField != null && prefilledPrix > 0) prixAchatField.setText(String.valueOf((int) prefilledPrix));
    }

    // ── Info processus ──
    private VBox buildInfoProcessus() {
        VBox card = new VBox(10); card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:14;-fx-border-color:#27ae60;-fx-border-width:0 0 0 5;");
        Label titre = new Label("📋  Processus Mourabaha — Vous êtes à l'étape finale");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); titre.setStyle("-fx-text-fill:#1b5e20;");
        Label etapes = new Label(
                "✅  Étape 1 : Client a soumis sa demande\n" +
                        "✅  Étape 2 : Agent a accepté la demande\n" +
                        "✅  Étape 3 : Agent a commandé au fournisseur\n" +
                        "✅  Étape 4 : Agent a validé la réception du bien\n" +
                        "📝  Étape 5 : Créer le contrat Mourabaha ← Vous êtes ici\n" +
                        "     Les échéances seront générées automatiquement."
        );
        etapes.setFont(Font.font("Georgia", 13)); etapes.setStyle("-fx-text-fill:#2e7d32;"); etapes.setWrapText(true);
        card.getChildren().addAll(titre, etapes); return card;
    }

    // ── Formulaire ──
    private VBox buildFormContrat() {
        VBox card = sectionCard("📝  Créer un Contrat Mourabaha", "#1b5e20");

        Label infoMarge = new Label(
                "ℹ️  Marge = pourcentage fixe du prix d'achat (PAS un intérêt).\n" +
                        "     Prix de vente = Prix d'achat + (Prix d'achat × Marge / 100)");
        infoMarge.setFont(Font.font("Georgia", 12));
        infoMarge.setStyle("-fx-text-fill:#555;-fx-background-color:#eafaf1;-fx-padding:8 12;-fx-background-radius:8;");
        infoMarge.setWrapText(true);

        GridPane grid = new GridPane(); grid.setHgap(16); grid.setVgap(12);
        idClientField  = champ("ID du client");
        prixAchatField = champ("Ex: 50000");
        margeField     = champ("Ex: 15 (pour 15%)");
        dureeField     = champ("Ex: 12 (mois)");

        statutCombo = new ComboBox<>(FXCollections.observableArrayList("EN_COURS", "CLOTURE", "SUSPENDU"));
        statutCombo.setValue("EN_COURS"); statutCombo.setMaxWidth(Double.MAX_VALUE);
        statutCombo.setStyle("-fx-font-family:Georgia;-fx-font-size:13px;");

        prixVenteLabel  = new Label("— MRU"); prixVenteLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 18)); prixVenteLabel.setStyle("-fx-text-fill:#27ae60;");
        mensualiteLabel = new Label("— MRU / mois"); mensualiteLabel.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); mensualiteLabel.setStyle("-fx-text-fill:#2980b9;");

        prixAchatField.textProperty().addListener((o, ov, nv) -> calculer());
        margeField.textProperty().addListener((o, ov, nv) -> calculer());
        dureeField.textProperty().addListener((o, ov, nv) -> calculer());

        ColumnConstraints lbl = new ColumnConstraints(160);
        ColumnConstraints fld = new ColumnConstraints(); fld.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(lbl, fld, lbl, fld);

        grid.add(label("ID Client"),               0, 0); grid.add(idClientField,  1, 0);
        grid.add(label("Statut"),                  2, 0); grid.add(statutCombo,    3, 0);
        grid.add(label("Prix d'achat (MRU)"),      0, 1); grid.add(prixAchatField, 1, 1);
        grid.add(label("Marge bénéficiaire (%)"),  2, 1); grid.add(margeField,     3, 1);
        grid.add(label("Durée (mois)"),            0, 2); grid.add(dureeField,     1, 2);

        VBox calcBox = new VBox(6); calcBox.setPadding(new Insets(12)); calcBox.setStyle("-fx-background-color:#f0f4f0;-fx-background-radius:10;");
        Label calcTitre = new Label("Calcul automatique :"); calcTitre.setFont(Font.font("Georgia", FontWeight.BOLD, 12)); calcTitre.setStyle("-fx-text-fill:#555;");
        HBox row1 = new HBox(10, new Label("💰  Prix de vente total :"), prixVenteLabel); row1.setAlignment(Pos.CENTER_LEFT);
        HBox row2 = new HBox(10, new Label("📅  Mensualité :"), mensualiteLabel); row2.setAlignment(Pos.CENTER_LEFT);
        calcBox.getChildren().addAll(calcTitre, row1, row2);

        Label msgLbl = new Label(""); msgLbl.setFont(Font.font("Georgia", 12)); msgLbl.setWrapText(true);

        Button btnSave = btn("💾  Créer le contrat", "#2e7d32", "#1b5e20");
        Button btnMod  = btn("✏️  Modifier",          "#2980b9", "#1a6090");
        Button btnDel  = btn("🗑️  Supprimer",         "#c0392b", "#922b21");
        Button btnClr  = btn("🔄  Effacer",           "#7f8c8d", "#636e72");

        btnSave.setOnAction(e -> {
            try {
                valider();
                double prixAchat = Double.parseDouble(prixAchatField.getText().trim());
                double marge     = Double.parseDouble(margeField.getText().trim());
                int    duree     = Integer.parseInt(dureeField.getText().trim());
                long   idClient  = Long.parseLong(idClientField.getText().trim());

                Client client = new Client(); client.setIdClient(idClient);

                if (contratSelectionne == null) {
                    ContratMourabaha c = new ContratMourabaha(prixAchat, marge, duree, client, null);
                    c.setStatutContrat(statutCombo.getValue());
                    repo.save(c);

                    // ── Lier la demande source si elle existe ──
                    if (idDemandeSource > 0) {
                        try { demandeRepo.updateStatut(idDemandeSource, "CONTRAT_CREE"); }
                        catch (Exception ex) { System.err.println("[ContratView] Lien demande : " + ex.getMessage()); }
                    }

                    double mensualite = c.getPrixVenteClient() / duree;
                    msgLbl.setText("✅  Contrat créé ! " + duree + " échéances de " +
                            String.format("%.2f MRU", mensualite) + " générées automatiquement." +
                            (idDemandeSource > 0 ? "\n✅  La demande a été marquée CONTRAT_CREE." : ""));
                    msgLbl.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;");
                } else {
                    contratSelectionne.setPrixAchatAgence(prixAchat);
                    contratSelectionne.setMargeBeneficiaire(marge);
                    contratSelectionne.setDureeMois(duree);
                    contratSelectionne.setStatutContrat(statutCombo.getValue());
                    repo.update(contratSelectionne);
                    msgLbl.setText("✅  Contrat modifié avec succès.");
                    msgLbl.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;");
                }
                clearForm(); chargerDonnees();

            } catch (NumberFormatException ex) {
                msgLbl.setText("❌  Saisissez des valeurs numériques valides."); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
            } catch (IllegalArgumentException ex) {
                msgLbl.setText("❌  " + ex.getMessage()); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
            } catch (Exception ex) {
                msgLbl.setText("❌  " + ex.getMessage()); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;");
            }
        });

        btnMod.setOnAction(e -> {
            ContratMourabaha sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgLbl.setText("❌  Sélectionnez un contrat."); msgLbl.setStyle("-fx-text-fill:#c0392b;"); return; }
            contratSelectionne = sel;
            idClientField.setText(String.valueOf(sel.getClient().getIdClient()));
            prixAchatField.setText(String.valueOf(sel.getPrixAchatAgence()));
            margeField.setText(String.valueOf(sel.getMargeBeneficiaire()));
            dureeField.setText(String.valueOf(sel.getDureeMois()));
            statutCombo.setValue(sel.getStatutContrat());
        });

        btnDel.setOnAction(e -> {
            ContratMourabaha sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { msgLbl.setText("❌  Sélectionnez un contrat."); msgLbl.setStyle("-fx-text-fill:#c0392b;"); return; }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le contrat #" + sel.getIdContrat() + " et ses échéances ?", ButtonType.YES, ButtonType.NO);
            conf.setHeaderText(null);
            if (conf.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try { repo.delete(sel.getIdContrat()); chargerDonnees(); msgLbl.setText("✅  Contrat supprimé."); msgLbl.setStyle("-fx-text-fill:#27ae60;"); }
                catch (Exception ex) { msgLbl.setText("❌  " + ex.getMessage()); msgLbl.setStyle("-fx-text-fill:#c0392b;"); }
            }
        });

        btnClr.setOnAction(e -> { clearForm(); msgLbl.setText(""); });
        HBox btns = new HBox(12, btnSave, btnMod, btnDel, btnClr);
        card.getChildren().addAll(infoMarge, grid, calcBox, btns, msgLbl);
        return card;
    }

    // ── Table des contrats ──
    private VBox buildTableContrats() {
        VBox card = sectionCard("📋  Liste des Contrats Mourabaha", "#1b5e20");

        double benef = 0;
        try { benef = repo.getBeneficeTotal(); } catch (Exception ignored) {}
        Label benefLbl = new Label(String.format("💰  Bénéfice total agence : %.2f MRU", benef));
        benefLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        benefLbl.setStyle("-fx-text-fill:#27ae60;-fx-background-color:#eafaf1;-fx-padding:8 14;-fx-background-radius:8;");

        table = new TableView<>();
        table.setPrefHeight(300); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<ContratMourabaha, Long>    cId     = new TableColumn<>("ID");
        TableColumn<ContratMourabaha, String>  cDate   = new TableColumn<>("Date");
        TableColumn<ContratMourabaha, String>  cClient = new TableColumn<>("Client");
        TableColumn<ContratMourabaha, Double>  cAchat  = new TableColumn<>("Prix achat");
        TableColumn<ContratMourabaha, Double>  cMarge  = new TableColumn<>("Marge %");
        TableColumn<ContratMourabaha, String>  cVente  = new TableColumn<>("Prix vente");
        TableColumn<ContratMourabaha, String>  cMens   = new TableColumn<>("Mensualité");
        TableColumn<ContratMourabaha, Integer> cDuree  = new TableColumn<>("Durée");
        TableColumn<ContratMourabaha, String>  cStat   = new TableColumn<>("Statut");

        cId.setCellValueFactory(new PropertyValueFactory<>("idContrat"));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateContrat() != null ? d.getValue().getDateContrat().format(fmt) : ""));
        cClient.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getClient() != null ? d.getValue().getClient().getNom() : ""));
        cAchat.setCellValueFactory(new PropertyValueFactory<>("prixAchatAgence"));
        cMarge.setCellValueFactory(new PropertyValueFactory<>("margeBeneficiaire"));
        cVente.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getPrixVenteClient())));
        cMens.setCellValueFactory(d -> {
            ContratMourabaha c = d.getValue();
            return new SimpleStringProperty(c.getDureeMois() != null && c.getDureeMois() > 0
                    ? String.format("%.2f", c.getPrixVenteClient() / c.getDureeMois()) : "—");
        });
        cDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        cStat.setCellValueFactory(new PropertyValueFactory<>("statutContrat"));
        cVente.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item+" MRU"); setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });
        cStat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item);
                String c = "EN_COURS".equals(item)?"#2980b9":"CLOTURE".equals(item)?"#27ae60":"#e67e22";
                setStyle("-fx-text-fill:"+c+";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });
        table.getColumns().addAll(cId, cDate, cClient, cAchat, cMarge, cVente, cMens, cDuree, cStat);
        data = FXCollections.observableArrayList(); table.setItems(data);
        card.getChildren().addAll(benefLbl, table); return card;
    }

    // ── Helpers ──
    private void calculer() {
        try {
            double prix  = Double.parseDouble(prixAchatField.getText().trim());
            double marge = Double.parseDouble(margeField.getText().trim());
            double vente = prix + (prix * marge / 100);
            prixVenteLabel.setText(String.format("%.2f MRU", vente));
            prixVenteLabel.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:18px;");
            try {
                int duree = Integer.parseInt(dureeField.getText().trim());
                if (duree > 0) mensualiteLabel.setText(String.format("%.2f MRU / mois", vente / duree));
            } catch (NumberFormatException ignored) {}
        } catch (NumberFormatException ignored) { prixVenteLabel.setText("— MRU"); mensualiteLabel.setText("— MRU / mois"); }
    }

    private void valider() {
        if (idClientField.getText().trim().isEmpty())  throw new IllegalArgumentException("L'ID client est requis.");
        if (prixAchatField.getText().trim().isEmpty()) throw new IllegalArgumentException("Le prix d'achat est requis.");
        if (margeField.getText().trim().isEmpty())     throw new IllegalArgumentException("La marge est requise.");
        if (dureeField.getText().trim().isEmpty())     throw new IllegalArgumentException("La durée est requise.");
        double prix = Double.parseDouble(prixAchatField.getText().trim());
        double marge = Double.parseDouble(margeField.getText().trim());
        int duree = Integer.parseInt(dureeField.getText().trim());
        if (prix <= 0)                throw new IllegalArgumentException("Le prix doit être positif.");
        if (marge < 0 || marge > 100) throw new IllegalArgumentException("La marge doit être entre 0 et 100%.");
        if (duree <= 0)               throw new IllegalArgumentException("La durée doit être positive.");
    }

    private void chargerDonnees() {
        try { data.setAll(repo.findAll()); }
        catch (Exception e) { System.err.println("[ContratView] " + e.getMessage()); }
    }

    private void clearForm() {
        contratSelectionne = null;
        idClientField.clear(); prixAchatField.clear(); margeField.clear(); dureeField.clear();
        statutCombo.setValue("EN_COURS");
        prixVenteLabel.setText("— MRU"); mensualiteLabel.setText("— MRU / mois");
        table.getSelectionModel().clearSelection();
    }

    private VBox sectionCard(String titre, String color) {
        VBox c = new VBox(14); c.setPadding(new Insets(22));
        c.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:"+color+";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 15)); t.setStyle("-fx-text-fill:"+color+";");
        c.getChildren().add(t); return c;
    }
    private TextField champ(String p) {
        TextField tf = new TextField(); tf.setPromptText(p); tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-family:Georgia;-fx-font-size:13px;");
        return tf;
    }
    private Label label(String t) { Label l = new Label(t); l.setFont(Font.font("Georgia", FontWeight.BOLD, 12)); l.setStyle("-fx-text-fill:#333;"); return l; }
    private Button btn(String text, String bg, String hover) {
        Button b = new Button(text);
        String s  = "-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        b.setStyle(s); b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s)); return b;
    }

    private VBox buildSidebar() {
        VBox sb = new VBox(0); sb.setPrefWidth(230); sb.setStyle("-fx-background-color:#1b5e20;");
        VBox logoBox = new VBox(8); logoBox.setAlignment(Pos.CENTER); logoBox.setPadding(new Insets(28, 20, 28, 20)); logoBox.setStyle("-fx-background-color:#145214;");

        Label appName = new Label("MicroFinance"); appName.setFont(Font.font("Georgia", FontWeight.BOLD, 16)); appName.setStyle("-fx-text-fill:white;");
        Label mru = new Label("MRU"); mru.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); mru.setStyle("-fx-text-fill:#a5d6a7;");
        Label badge = new Label("👩‍💼  AGENT");
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll( appName, mru, badge);
        VBox menu = new VBox(4); menu.setPadding(new Insets(18, 10, 18, 10));
        Button bA = sb2("🏠","Accueil",false), bD = sb2("📨","Demandes Clients",false),
                bF = sb2("🏭","Fournisseurs & Biens",false), bC = sb2("📋","Contrats Mourabaha",true),
                bE = sb2("✅","Paiements Échéances",false);
        Button bH = sb2("📊", "Historique", false);
        bH.setOnAction(e -> nav(new HistoriqueView(primaryStage).getRoot(), "Historique"));
        bA.setOnAction(e -> nav(new AccueilView(primaryStage).getRoot(),      "Accueil"));
        bD.setOnAction(e -> nav(new DemandeAgentView(primaryStage).getRoot(), "Demandes Clients"));
        bF.setOnAction(e -> nav(new FournisseurView(primaryStage).getRoot(),  "Fournisseurs"));
        bC.setOnAction(e -> nav(new ContratView(primaryStage).getRoot(),      "Contrats"));
        bE.setOnAction(e -> nav(new EcheanceView(primaryStage).getRoot(),     "Paiements"));
        menu.getChildren().addAll(bA, bD, bF, bC, bE,bH);
        VBox bottom = new VBox(8); bottom.setPadding(new Insets(20)); VBox.setVgrow(bottom, Priority.ALWAYS); bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout = new Button("⬅   Déconnexion"); logout.setMaxWidth(Double.MAX_VALUE);
        String lo = "-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh = "-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        logout.setStyle(lo); logout.setOnMouseEntered(e -> logout.setStyle(lh)); logout.setOnMouseExited(e -> logout.setStyle(lo));
        logout.setOnAction(e -> { UserSession.getInstance().logout(); nav(new LoginView(primaryStage).getRoot(), "Connexion"); });
        bottom.getChildren().add(logout);
        sb.getChildren().addAll(logoBox, menu, bottom); return sb;
    }
    private Button sb2(String icon, String text, boolean active) {
        Button btn = new Button(icon + "   " + text); btn.setMaxWidth(Double.MAX_VALUE); btn.setAlignment(Pos.CENTER_LEFT);
        String base  = active ? "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;"
                : "-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover = "-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base); btn.setOnMouseEntered(e -> { if (!active) btn.setStyle(hover); }); btn.setOnMouseExited(e -> { if (!active) btn.setStyle(base); });
        return btn;
    }
    private HBox buildTopBar(String titre) {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 18)); t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label user = new Label("👩‍💼  " + UserSession.getInstance().getNom()); user.setFont(Font.font("Georgia", 13)); user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t, sp, user); return bar;
    }
    private void nav(javafx.scene.Parent root, String titre) { primaryStage.getScene().setRoot(root); primaryStage.setTitle("MicroFinance MRU — " + titre); }
    public BorderPane getRoot() { return root; }
}