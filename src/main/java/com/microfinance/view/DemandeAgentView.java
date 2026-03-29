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
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DemandeAgentView — Gestion complète du flux Mourabaha côté agent.
 *
 * Flux des statuts :
 *  EN_ATTENTE → ACCEPTEE → COMMANDE_FOURNISSEUR → BIEN_RECU → CONTRAT_CREE
 *  EN_ATTENTE → REFUSEE  (flux alternatif)
 *
 * Actions par statut :
 *  EN_ATTENTE            → [✅ Accepter] [❌ Refuser]
 *  ACCEPTEE              → Choisir fournisseur + [📦 Commander]
 *  COMMANDE_FOURNISSEUR  → [✅ Marquer bien reçu]
 *  BIEN_RECU             → [📝 Créer le contrat Mourabaha]
 *  CONTRAT_CREE/REFUSEE  → Affichage final uniquement
 */
public class DemandeAgentView {

    private BorderPane root;
    private Stage primaryStage;

    private final DemandeAchatRepositoryImpl demandeRepo     = new DemandeAchatRepositoryImpl();
    private final FournisseurRepositoryImpl  fournisseurRepo  = new FournisseurRepositoryImpl();

    private final ObservableList<DemandeAchat> data       = FXCollections.observableArrayList();
    private       ObservableList<DemandeAchat> dataFiltree = FXCollections.observableArrayList();

    private TableView<DemandeAchat> table;
    private ComboBox<String>        filtreCombo;
    private VBox                    actionContainer;
    private List<Fournisseur>       listeFournisseurs;

    public DemandeAgentView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        chargerFournisseurs();
        createView();
        chargerDonnees();
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

        VBox body = new VBox(20);
        body.setPadding(new Insets(24));
        body.getChildren().addAll(
                buildBanniereProcessus(),
                buildStatistiques(),
                buildTableSection(),
                buildActionContainer()
        );

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ── Bannière processus ──
    private VBox buildBanniereProcessus() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:14;-fx-border-color:#27ae60;-fx-border-width:0 0 0 5;");
        Label titre = new Label("📋  Flux Mourabaha — Actions de l'Agent");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        titre.setStyle("-fx-text-fill:#1b5e20;");
        HBox etapes = new HBox(6);
        etapes.setAlignment(Pos.CENTER_LEFT);
        etapes.getChildren().addAll(
                etape("1️⃣", "Client\ndemande",              "#e67e22"),
                fleche(),
                etape("2️⃣", "Accepter\nou refuser",         "#2980b9"),
                fleche(),
                etape("3️⃣", "Commander\nfournisseur",        "#8e44ad"),
                fleche(),
                etape("4️⃣", "Valider\nlivraison",           "#16a085"),
                fleche(),
                etape("5️⃣", "Créer\nle contrat",            "#27ae60")
        );
        card.getChildren().addAll(titre, etapes);
        return card;
    }

    // ── Statistiques ──
    private HBox buildStatistiques() {
        long enAttente  = data.stream().filter(d -> "EN_ATTENTE".equals(d.getStatutDemande())).count();
        long acceptees  = data.stream().filter(d -> "ACCEPTEE".equals(d.getStatutDemande())).count();
        long commandees = data.stream().filter(d -> "COMMANDE_FOURNISSEUR".equals(d.getStatutDemande())).count();
        long recues     = data.stream().filter(d -> "BIEN_RECU".equals(d.getStatutDemande())).count();
        long terminees  = data.stream().filter(d -> "CONTRAT_CREE".equals(d.getStatutDemande())).count();
        HBox box = new HBox(12);
        VBox[] kpis = {
                kpi("⏳", String.valueOf(enAttente),  "En attente",  "#e67e22", "#fef9e7"),
                kpi("✅", String.valueOf(acceptees),  "Acceptées",   "#2980b9", "#eaf4fb"),
                kpi("🚚", String.valueOf(commandees), "Commandées",  "#8e44ad", "#f5eef8"),
                kpi("📦", String.valueOf(recues),     "Reçues",      "#16a085", "#e8f8f5"),
                kpi("📝", String.valueOf(terminees),  "Contrat créé","#27ae60", "#eafaf1")
        };
        for (VBox k : kpis) { HBox.setHgrow(k, Priority.ALWAYS); box.getChildren().add(k); }
        return box;
    }

    // ── Table des demandes ──
    private VBox buildTableSection() {
        VBox card = sectionCard("📨  Toutes les Demandes Clients", "#2980b9");

        HBox filtreRow = new HBox(12);
        filtreRow.setAlignment(Pos.CENTER_LEFT);
        filtreCombo = new ComboBox<>(FXCollections.observableArrayList(
                "TOUS","EN_ATTENTE","ACCEPTEE","COMMANDE_FOURNISSEUR","BIEN_RECU","CONTRAT_CREE","REFUSEE"));
        filtreCombo.setValue("TOUS");
        filtreCombo.setStyle("-fx-font-family:Georgia;-fx-font-size:13px;");
        filtreCombo.setOnAction(e -> appliquerFiltre());
        Button btnActu = btn("🔄  Actualiser", "#2980b9", "#1a6090");
        btnActu.setOnAction(e -> chargerDonnees());
        filtreRow.getChildren().addAll(label("Filtrer :"), filtreCombo, btnActu);

        table = new TableView<>();
        table.setPrefHeight(280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Aucune demande."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        TableColumn<DemandeAchat, String> cDate   = new TableColumn<>("Date");
        TableColumn<DemandeAchat, String> cClient = new TableColumn<>("Client");
        TableColumn<DemandeAchat, String> cDesc   = new TableColumn<>("Bien demandé");
        TableColumn<DemandeAchat, String> cPrix   = new TableColumn<>("Prix estimé");
        TableColumn<DemandeAchat, String> cFourn  = new TableColumn<>("Fournisseur");
        TableColumn<DemandeAchat, String> cStatut = new TableColumn<>("Statut");

        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDemande() != null ? d.getValue().getDateDemande().format(fmt) : "—"));
        cClient.setCellValueFactory(d -> new SimpleStringProperty(nomClient(d.getValue())));
        cDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescriptionBien()));
        cPrix.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPrixEstime() != null ? String.format("%.0f MRU", d.getValue().getPrixEstime()) : "—"));
        cFourn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFournisseur() != null
                        ? d.getValue().getFournisseur().getNomEntreprise()
                        : "—"));
        cStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutDemande()));
        cStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                String[] info = statutInfo(item);
                setText(info[0]);
                setStyle("-fx-text-fill:" + info[1] + ";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        table.getColumns().addAll(cDate, cClient, cDesc, cPrix, cFourn, cStatut);
        table.setItems(dataFiltree);

        // Sélection → met à jour le panneau d'action
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> mettreAJourActionPanel(sel));

        Label hint = new Label("💡  Cliquez sur une ligne pour voir et effectuer les actions disponibles.");
        hint.setFont(Font.font("Georgia", 12)); hint.setStyle("-fx-text-fill:#888;");

        card.getChildren().addAll(filtreRow, table, hint);
        return card;
    }

    private VBox buildActionContainer() {
        actionContainer = new VBox(0);
        return actionContainer;
    }

    // ═══════════════════════════════════════════════════
    // PANNEAUX D'ACTION DYNAMIQUES
    // ═══════════════════════════════════════════════════
    private void mettreAJourActionPanel(DemandeAchat dem) {
        actionContainer.getChildren().clear();
        if (dem == null) return;
        switch (dem.getStatutDemande()) {
            case "EN_ATTENTE"           -> actionContainer.getChildren().add(panelEnAttente(dem));
            case "ACCEPTEE"             -> actionContainer.getChildren().add(panelAcceptee(dem));
            case "COMMANDE_FOURNISSEUR" -> actionContainer.getChildren().add(panelCommandee(dem));
            case "BIEN_RECU"            -> actionContainer.getChildren().add(panelBienRecu(dem));
            case "CONTRAT_CREE"         -> actionContainer.getChildren().add(panelTerminal("✅  Contrat Mourabaha créé — flux terminé.", "#27ae60", "#eafaf1"));
            case "REFUSEE"              -> actionContainer.getChildren().add(panelTerminal("❌  Demande refusée.", "#c0392b", "#fdecea"));
        }
    }

    // Étape 2 : EN_ATTENTE → accepter ou refuser
    private VBox panelEnAttente(DemandeAchat dem) {
        VBox panel = actionPanel("⏳  Étape 1 — Examiner la demande", "#e67e22");
        Label info = infoLabel("Le client attend votre décision. Acceptez pour continuer le processus Mourabaha, ou refusez pour clore la demande.");
        Label msgLbl = msgLabel();
        Button btnAcc = btn("✅  Accepter la demande", "#27ae60", "#1e8449");
        Button btnRef = btn("❌  Refuser la demande",  "#c0392b", "#922b21");

        btnAcc.setOnAction(e -> {
            if (!confirmer("Accepter la demande de " + nomClient(dem) + " pour « " + dem.getDescriptionBien() + " » ?")) return;
            try {
                demandeRepo.updateStatut(dem.getIdDemande(), "ACCEPTEE");
                succes(msgLbl, "✅  Demande acceptée ! Sélectionnez maintenant un fournisseur.");
                chargerDonnees(); reselecter(dem.getIdDemande());
            } catch (Exception ex) { erreur(msgLbl, ex.getMessage()); }
        });
        btnRef.setOnAction(e -> {
            if (!confirmer("Refuser la demande de " + nomClient(dem) + " ?")) return;
            try {
                demandeRepo.updateStatut(dem.getIdDemande(), "REFUSEE");
                succes(msgLbl, "Demande refusée.");
                chargerDonnees(); actionContainer.getChildren().clear();
            } catch (Exception ex) { erreur(msgLbl, ex.getMessage()); }
        });
        panel.getChildren().addAll(info, new HBox(14, btnAcc, btnRef), msgLbl);
        return panel;
    }

    // Étape 3 : ACCEPTEE → choisir fournisseur et commander
    private VBox panelAcceptee(DemandeAchat dem) {
        VBox panel = actionPanel("🏭  Étape 2 — Commander au Fournisseur", "#2980b9");
        Label info = infoLabel("Choisissez le fournisseur qui livrera « " + dem.getDescriptionBien() + " ». La commande sera enregistrée en base de données.");

        ComboBox<String> comboF = new ComboBox<>();
        comboF.setPromptText(listeFournisseurs.isEmpty() ? "⚠  Aucun fournisseur — créez-en un d'abord" : "Sélectionner un fournisseur...");
        comboF.setDisable(listeFournisseurs.isEmpty());
        comboF.setStyle("-fx-font-family:Georgia;-fx-font-size:13px;-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;");
        comboF.setPrefWidth(300);
        listeFournisseurs.forEach(f -> comboF.getItems().add(f.getNomEntreprise()));

        Label msgLbl = msgLabel();
        Button btnCommander = btn("📦  Passer la commande", "#2980b9", "#1a6090");
        btnCommander.setOnAction(e -> {
            if (comboF.getValue() == null) { erreur(msgLbl, "Choisissez un fournisseur."); return; }
            Fournisseur fChoisi = listeFournisseurs.stream()
                    .filter(f -> f.getNomEntreprise().equals(comboF.getValue())).findFirst().orElse(null);
            if (fChoisi == null) { erreur(msgLbl, "Fournisseur introuvable."); return; }
            if (!confirmer("Commander « " + dem.getDescriptionBien() + " » auprès de « " + fChoisi.getNomEntreprise() + " » ?")) return;
            try {
                demandeRepo.updateFournisseurEtStatut(dem.getIdDemande(), fChoisi.getIdFournisseur(), "COMMANDE_FOURNISSEUR");
                succes(msgLbl, "📦  Commande passée à « " + fChoisi.getNomEntreprise() + " ». Attendez la livraison.");
                chargerDonnees(); reselecter(dem.getIdDemande());
            } catch (Exception ex) { erreur(msgLbl, ex.getMessage()); }
        });

        HBox row = new HBox(14, label("Fournisseur :"), comboF, btnCommander);
        row.setAlignment(Pos.CENTER_LEFT);
        panel.getChildren().addAll(info, row, msgLbl);
        return panel;
    }

    // Étape 4 : COMMANDE_FOURNISSEUR → valider réception
    private VBox panelCommandee(DemandeAchat dem) {
        VBox panel = actionPanel("🚚  Étape 3 — Valider la Réception du Bien", "#8e44ad");
        String nomF = dem.getFournisseur() != null
                ? dem.getFournisseur().getNomEntreprise()
                : "le fournisseur";
        Label info  = infoLabel("« " + dem.getDescriptionBien() + " » a été commandé auprès de « " + nomF + " ».\n" +
                "Lorsque le fournisseur effectue la livraison, cliquez sur le bouton ci-dessous.");
        Label msgLbl = msgLabel();
        Button btnRecu = btn("📦  Confirmer la réception du bien", "#8e44ad", "#6a1b9a");
        btnRecu.setOnAction(e -> {
            if (!confirmer("Confirmer la réception de « " + dem.getDescriptionBien() + " » ?")) return;
            try {
                demandeRepo.updateStatut(dem.getIdDemande(), "BIEN_RECU");
                succes(msgLbl, "📦  Réception confirmée ! Vous pouvez créer le contrat Mourabaha.");
                chargerDonnees(); reselecter(dem.getIdDemande());
            } catch (Exception ex) { erreur(msgLbl, ex.getMessage()); }
        });
        panel.getChildren().addAll(info, btnRecu, msgLbl);
        return panel;
    }

    // Étape 5 : BIEN_RECU → créer le contrat
    private VBox panelBienRecu(DemandeAchat dem) {
        VBox panel = actionPanel("📝  Étape 4 — Créer le Contrat Mourabaha", "#27ae60");
        Label info = infoLabel("Le bien est en votre possession. Créez maintenant le contrat Mourabaha.\n" +
                "Le formulaire sera automatiquement pré-rempli avec le client et le prix d'achat.");

        // Résumé de la demande
        VBox resume = new VBox(8);
        resume.setPadding(new Insets(14));
        resume.setStyle("-fx-background-color:#eafaf1;-fx-background-radius:10;-fx-border-color:#a9dfbf;-fx-border-radius:10;");
        resume.getChildren().addAll(
                resumeLigne("👤  Client :",      nomClient(dem)),
                resumeLigne("📦  Bien :",         dem.getDescriptionBien()),
                resumeLigne("🏭  Fournisseur :",
                        dem.getFournisseur() != null
                                ? dem.getFournisseur().getNomEntreprise()
                                : "—"),
                resumeLigne("💰  Prix estimé :",  dem.getPrixEstime() != null ? String.format("%.0f MRU", dem.getPrixEstime()) : "Non précisé")
        );

        Label msgLbl = msgLabel();
        Button btnContrat = btn("📝  Créer le Contrat Mourabaha →", "#27ae60", "#1e8449");
        btnContrat.setStyle(btnContrat.getStyle().replace("13px", "14px").replace("10 16", "12 22"));

        btnContrat.setOnAction(e -> {
            try {
                long   idClient  = dem.getIdClient().getIdClient();
                double prix      = dem.getPrixEstime() != null ? dem.getPrixEstime() : 0;
                long   idDemande = dem.getIdDemande();
                ContratView cv = new ContratView(primaryStage, idClient, prix, idDemande);
                primaryStage.getScene().setRoot(cv.getRoot());
                primaryStage.setTitle("MicroFinance MRU — Créer Contrat");
            } catch (Exception ex) { erreur(msgLbl, ex.getMessage()); }
        });

        panel.getChildren().addAll(info, resume, btnContrat, msgLbl);
        return panel;
    }

    // Panel terminal (CONTRAT_CREE ou REFUSEE)
    private VBox panelTerminal(String message, String color, String bg) {
        VBox panel = new VBox(10); panel.setPadding(new Insets(18));
        panel.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;-fx-border-color:" + color + ";-fx-border-width:0 0 0 5;");
        Label lbl = new Label(message); lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); lbl.setStyle("-fx-text-fill:" + color + ";");
        panel.getChildren().add(lbl); return panel;
    }

    // ═══════════════════════════════════════════════════
    // CHARGEMENT ET FILTRAGE
    // ═══════════════════════════════════════════════════
    private void chargerDonnees() {
        try { data.setAll(demandeRepo.findAll()); appliquerFiltre(); }
        catch (Exception e) { System.err.println("[DemandeAgentView] " + e.getMessage()); }
    }

    private void chargerFournisseurs() {
        try { listeFournisseurs = fournisseurRepo.findAll(); }
        catch (Exception e) { listeFournisseurs = new java.util.ArrayList<>(); }
    }

    private void appliquerFiltre() {
        String filtre = filtreCombo != null ? filtreCombo.getValue() : "TOUS";
        ObservableList<DemandeAchat> f = FXCollections.observableArrayList();
        if ("TOUS".equals(filtre)) f.addAll(data);
        else data.stream().filter(d -> filtre.equals(d.getStatutDemande())).forEach(f::add);
        dataFiltree = f;
        if (table != null) table.setItems(dataFiltree);
    }

    private void reselecter(Long idDemande) {
        for (int i = 0; i < dataFiltree.size(); i++) {
            if (dataFiltree.get(i).getIdDemande().equals(idDemande)) {
                table.getSelectionModel().select(i);
                mettreAJourActionPanel(dataFiltree.get(i));
                return;
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════
    private VBox actionPanel(String titre, String color) {
        VBox p = new VBox(14); p.setPadding(new Insets(22));
        p.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:" + color +
                ";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 15)); t.setStyle("-fx-text-fill:" + color + ";");
        p.getChildren().add(t); return p;
    }

    private Label infoLabel(String txt) {
        Label l = new Label(txt); l.setFont(Font.font("Georgia", 13));
        l.setStyle("-fx-text-fill:#555;-fx-background-color:#f8f9fa;-fx-padding:10 14;-fx-background-radius:8;");
        l.setWrapText(true); return l;
    }

    private Label msgLabel() { Label l = new Label(""); l.setFont(Font.font("Georgia", 13)); l.setWrapText(true); return l; }

    private HBox resumeLigne(String cle, String val) {
        Label k = new Label(cle); k.setFont(Font.font("Georgia", FontWeight.BOLD, 12)); k.setMinWidth(140); k.setStyle("-fx-text-fill:#555;");
        Label v = new Label(val); v.setFont(Font.font("Georgia", 12)); v.setStyle("-fx-text-fill:#1b5e20;-fx-font-weight:bold;");
        HBox row = new HBox(8, k, v); row.setAlignment(Pos.CENTER_LEFT); return row;
    }

    private VBox kpi(String icon, String val, String label, String color, String bg) {
        VBox c = new VBox(4); c.setPadding(new Insets(14)); c.setAlignment(Pos.CENTER_LEFT);
        c.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:10;-fx-border-color:" + color + ";-fx-border-width:0 0 0 4;");
        Label v = new Label(icon + "  " + val); v.setFont(Font.font("Georgia", FontWeight.BOLD, 18)); v.setStyle("-fx-text-fill:" + color + ";");
        Label l = new Label(label); l.setFont(Font.font("Georgia", 11)); l.setStyle("-fx-text-fill:#666;");
        c.getChildren().addAll(v, l); return c;
    }

    private VBox sectionCard(String titre, String color) {
        VBox c = new VBox(14); c.setPadding(new Insets(22));
        c.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:" + color + ";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 14)); t.setStyle("-fx-text-fill:" + color + ";");
        c.getChildren().add(t); return c;
    }

    private Label label(String t) { Label l = new Label(t); l.setFont(Font.font("Georgia", FontWeight.BOLD, 12)); l.setStyle("-fx-text-fill:#333;"); return l; }

    private Button btn(String text, String bg, String hover) {
        Button b = new Button(text);
        String s  = "-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:" + hover + ";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        b.setStyle(s); b.setOnMouseEntered(e -> b.setStyle(sh)); b.setOnMouseExited(e -> b.setStyle(s)); return b;
    }

    private VBox etape(String num, String txt, String color) {
        VBox b = new VBox(3); b.setAlignment(Pos.CENTER); b.setPadding(new Insets(8, 12, 8, 12));
        b.setStyle("-fx-background-color:rgba(0,0,0,0.06);-fx-background-radius:8;");
        Label n = new Label(num); n.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); n.setStyle("-fx-text-fill:" + color + ";");
        Label t = new Label(txt); t.setFont(Font.font("Georgia", 10)); t.setStyle("-fx-text-fill:#444;-fx-text-alignment:center;"); t.setWrapText(true); t.setMaxWidth(80);
        b.getChildren().addAll(n, t); HBox.setHgrow(b, Priority.ALWAYS); return b;
    }

    private Label fleche() { Label l = new Label("→"); l.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;"); return l; }

    private String nomClient(DemandeAchat dem) {
        if (dem.getIdClient() == null) return "—";
        return dem.getIdClient().getNom() != null ? dem.getIdClient().getNom()
                : "Client #" + dem.getIdClient().getIdClient();
    }

    private String[] statutInfo(String statut) {
        return switch (statut) {
            case "EN_ATTENTE"           -> new String[]{"⏳  EN ATTENTE",   "#e67e22"};
            case "ACCEPTEE"             -> new String[]{"✅  ACCEPTÉE",     "#2980b9"};
            case "COMMANDE_FOURNISSEUR" -> new String[]{"🚚  COMMANDÉE",    "#8e44ad"};
            case "BIEN_RECU"            -> new String[]{"📦  BIEN REÇU",    "#16a085"};
            case "CONTRAT_CREE"         -> new String[]{"📝  CONTRAT CRÉÉ", "#27ae60"};
            case "REFUSEE"              -> new String[]{"❌  REFUSÉE",      "#c0392b"};
            default                     -> new String[]{statut,             "#555"};
        };
    }

    private boolean confirmer(String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        a.setHeaderText(null); return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void succes(Label l, String msg) { l.setText(msg); l.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;"); }
    private void erreur(Label l, String msg)  { l.setText("❌  " + msg); l.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;"); }

    // ── TopBar + Sidebar ──
    private HBox buildTopBar() {
        HBox bar = new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label("📨  Demandes Clients — Flux Mourabaha");
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
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll( appName, mru, badge);

        VBox menu = new VBox(4); menu.setPadding(new Insets(18, 10, 18, 10));
        Button bA = sb2("🏠", "Accueil",              false);
        Button bD = sb2("📨", "Demandes Clients",      true);
        Button bF = sb2("🏭", "Fournisseurs & Biens",  false);
        Button bC = sb2("📋", "Contrats Mourabaha",    false);
        Button bE = sb2("✅", "Paiements Échéances",   false);
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

    private void nav(javafx.scene.Parent root, String titre) {
        primaryStage.getScene().setRoot(root);
        primaryStage.setTitle("MicroFinance MRU — " + titre);
    }

    public BorderPane getRoot() { return root; }
}