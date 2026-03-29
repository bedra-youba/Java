package com.microfinance.view;

import com.microfinance.model.ContratMourabaha;
import com.microfinance.model.DemandeAchat;
import com.microfinance.model.Echeance;
import com.microfinance.repository.impl.ContratMourabahaRepositoryImpl;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;
import com.microfinance.repository.impl.EcheanceRepositoryImpl;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * HistoriqueView — Page Historique complet pour l'Agent.
 *
 * Sections :
 *  1. Historique des Demandes     (toutes, filtrées par nom client)
 *  2. Historique des Contrats     (tous, filtrés par nom client)
 *  3. Historique des Paiements    (toutes les échéances payées, filtrées par nom client)
 *  4. Historique des Connexions   (journal de session en mémoire)
 *
 * Filtre global par nom de client appliqué aux sections 1, 2 et 3.
 */
public class HistoriqueView {

    private BorderPane root;
    private Stage primaryStage;

    private final DemandeAchatRepositoryImpl      repoD = new DemandeAchatRepositoryImpl();
    private final ContratMourabahaRepositoryImpl  repoC = new ContratMourabahaRepositoryImpl();
    private final EcheanceRepositoryImpl          repoE = new EcheanceRepositoryImpl();

    // ── Tables & données ──
    private TableView<DemandeAchat>     tableDemandes;
    private TableView<ContratMourabaha> tableContrats;
    private TableView<Echeance>         tablePaiements;
    private TableView<String[]>         tableConnexions;

    private ObservableList<DemandeAchat>     dataDemandes    = FXCollections.observableArrayList();
    private ObservableList<ContratMourabaha> dataContrats    = FXCollections.observableArrayList();
    private ObservableList<Echeance>         dataPaiements   = FXCollections.observableArrayList();
    private ObservableList<String[]>         dataConnexions  = FXCollections.observableArrayList();

    // Données brutes (avant filtre)
    private List<DemandeAchat>     toutesLesDemandes  = new ArrayList<>();
    private List<ContratMourabaha> tousLesContrats    = new ArrayList<>();
    private List<Echeance>         tousLesPaiements   = new ArrayList<>();

    // Champ de filtre
    private TextField filtreClientField;
    private Label     lblTotalDemandes, lblTotalContrats, lblTotalPaiements, lblTotalConnexions;

    // Journal de connexions simulé (en mémoire – réel si vous avez une table connexions)
    private static final List<String[]> journalConnexions = new ArrayList<>();
    static {
        // Enregistrer la connexion actuelle au chargement de la classe
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        journalConnexions.add(new String[]{ts, UserSession.getInstance().getNom(), "Connexion", "Succès"});
    }

    public HistoriqueView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
        chargerTout();
    }

    // ═══════════════════════════════════════════════════════
    // CONSTRUCTION DE LA VUE
    // ═══════════════════════════════════════════════════════
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
        body.setPadding(new Insets(26, 28, 28, 28));

        body.getChildren().addAll(
                buildBanniereInfo(),
                buildBarreFiltre(),
                buildKpiRow(),
                buildSectionDemandes(),
                buildSectionContrats(),
                buildSectionPaiements(),
                buildSectionConnexions()
        );

        scroll.setContent(body);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        main.getChildren().add(scroll);
        root.setCenter(main);
    }

    // ── Bannière d'info ──
    private VBox buildBanniereInfo() {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:14;" +
                "-fx-border-color:#27ae60;-fx-border-width:0 0 0 5;");
        Label titre = new Label("📊  Historique complet — Espace Agent");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        titre.setStyle("-fx-text-fill:#1b5e20;");
        Label desc = new Label(
                "Consultez l'intégralité des demandes, contrats, paiements et connexions.\n" +
                        "Utilisez le champ « Rechercher par client » pour filtrer les résultats.");
        desc.setFont(Font.font("Georgia", 13));
        desc.setStyle("-fx-text-fill:#2e7d32;");
        desc.setWrapText(true);
        card.getChildren().addAll(titre, desc);
        return card;
    }

    // ── Barre de filtre par client ──
    private HBox buildBarreFiltre() {
        HBox bar = new HBox(14);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(16));
        bar.setStyle("-fx-background-color:white;-fx-background-radius:14;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");

        Label icone = new Label("🔍");
        icone.setStyle("-fx-font-size:20px;");

        filtreClientField = new TextField();
        filtreClientField.setPromptText("Rechercher par nom du client…");
        filtreClientField.setPrefWidth(320);
        filtreClientField.setStyle(
                "-fx-background-color:#f8f9fa;-fx-border-color:#c8e6c9;-fx-border-radius:10;" +
                        "-fx-background-radius:10;-fx-padding:10 14;-fx-font-family:Georgia;-fx-font-size:13px;");

        Button btnFiltrer  = actionBtn("🔍", "Filtrer");
        Button btnReinit   = actionBtn("🔄", "Tout afficher");

        btnFiltrer.setStyle(
                "-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;");
        btnFiltrer.setOnMouseEntered(e -> btnFiltrer.setStyle(
                "-fx-background-color:#1b5e20;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;"));
        btnFiltrer.setOnMouseExited(e -> btnFiltrer.setStyle(
                "-fx-background-color:#2e7d32;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;"));

        btnReinit.setStyle(
                "-fx-background-color:#7f8c8d;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;");
        btnReinit.setOnMouseEntered(e -> btnReinit.setStyle(
                "-fx-background-color:#636e72;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;"));
        btnReinit.setOnMouseExited(e -> btnReinit.setStyle(
                "-fx-background-color:#7f8c8d;-fx-text-fill:white;-fx-font-family:Georgia;" +
                        "-fx-font-size:13px;-fx-padding:10 18;-fx-background-radius:10;-fx-cursor:hand;"));

        btnFiltrer.setOnAction(e -> appliquerFiltre());
        btnReinit.setOnAction(e  -> { filtreClientField.clear(); appliquerFiltre(); });

        // Filtre en temps réel
        filtreClientField.textProperty().addListener((obs, ov, nv) -> appliquerFiltre());

        Label hint = new Label("Le filtre s'applique aux demandes, contrats et paiements.");
        hint.setFont(Font.font("Georgia", 11));
        hint.setStyle("-fx-text-fill:#aaa;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(icone, filtreClientField, btnFiltrer, btnReinit, spacer, hint);
        return bar;
    }

    // ── KPI Row ──
    private HBox buildKpiRow() {
        HBox row = new HBox(16);

        VBox k1 = kpiCard("📨", "0", "Demandes totales",  "#e67e22", "#fef9e7", "#e67e22");
        VBox k2 = kpiCard("📋", "0", "Contrats totaux",   "#2980b9", "#eaf4fb", "#2980b9");
        VBox k3 = kpiCard("✅", "0", "Paiements reçus",   "#27ae60", "#eafaf1", "#27ae60");
        VBox k4 = kpiCard("🔐", "0", "Connexions agent",  "#8e44ad", "#f5eef8", "#8e44ad");

        lblTotalDemandes   = (Label) k1.getChildren().get(1);
        lblTotalContrats   = (Label) k2.getChildren().get(1);
        lblTotalPaiements  = (Label) k3.getChildren().get(1);
        lblTotalConnexions = (Label) k4.getChildren().get(1);

        for (VBox k : List.of(k1, k2, k3, k4)) HBox.setHgrow(k, Priority.ALWAYS);
        row.getChildren().addAll(k1, k2, k3, k4);
        return row;
    }

    private VBox kpiCard(String icon, String val, String label, String vc, String bg, String bc) {
        VBox c = new VBox(8);
        c.setPadding(new Insets(18));
        c.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;" +
                "-fx-border-color:" + bc + ";-fx-border-width:0 0 0 4;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
        Label i = new Label(icon); i.setStyle("-fx-font-size:24px;");
        Label v = new Label(val);
        v.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        v.setStyle("-fx-text-fill:" + vc + ";");
        Label l = new Label(label);
        l.setFont(Font.font("Georgia", 12));
        l.setStyle("-fx-text-fill:#666;");
        l.setWrapText(true);
        c.getChildren().addAll(i, v, l);
        return c;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 1 — HISTORIQUE DES DEMANDES
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionDemandes() {
        VBox card = sectionCard("📨  Historique des Demandes Clients", "#e67e22");

        tableDemandes = new TableView<>();
        tableDemandes.setPrefHeight(260);
        tableDemandes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableDemandes.setPlaceholder(new Label("Aucune demande trouvée."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        TableColumn<DemandeAchat, String> cDate    = new TableColumn<>("Date");
        TableColumn<DemandeAchat, String> cClient  = new TableColumn<>("Client");
        TableColumn<DemandeAchat, String> cBien    = new TableColumn<>("Bien demandé");
        TableColumn<DemandeAchat, String> cPrix    = new TableColumn<>("Prix estimé (MRU)");
        TableColumn<DemandeAchat, String> cFourn   = new TableColumn<>("Fournisseur");
        TableColumn<DemandeAchat, String> cStatut  = new TableColumn<>("Statut");

        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateDemande() != null
                        ? d.getValue().getDateDemande().format(fmt) : "—"));
        cClient.setCellValueFactory(d -> new SimpleStringProperty(nomClient(d.getValue())));
        cBien.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescriptionBien()));
        cPrix.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getPrixEstime() != null
                        ? String.format("%.0f", d.getValue().getPrixEstime()) : "—"));
        cFourn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNomFournisseur() != null ? d.getValue().getNomFournisseur() : "—"));
        cStatut.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatutDemande()));

        // Couleur statut demandes
        cStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item) {
                    case "EN_ATTENTE"           -> "#e67e22";
                    case "ACCEPTEE"             -> "#2980b9";
                    case "REFUSEE"              -> "#c0392b";
                    case "COMMANDE_FOURNISSEUR" -> "#8e44ad";
                    case "BIEN_RECU"            -> "#16a085";
                    case "CONTRAT_CREE"         -> "#27ae60";
                    default                     -> "#555";
                };
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        tableDemandes.getColumns().addAll(cDate, cClient, cBien, cPrix, cFourn, cStatut);
        tableDemandes.setItems(dataDemandes);
        card.getChildren().add(tableDemandes);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 2 — HISTORIQUE DES CONTRATS
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionContrats() {
        VBox card = sectionCard("📋  Historique des Contrats Mourabaha", "#2980b9");

        tableContrats = new TableView<>();
        tableContrats.setPrefHeight(240);
        tableContrats.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableContrats.setPlaceholder(new Label("Aucun contrat trouvé."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<ContratMourabaha, Long>   cId     = new TableColumn<>("ID");
        TableColumn<ContratMourabaha, String> cDate   = new TableColumn<>("Date création");
        TableColumn<ContratMourabaha, String> cClient = new TableColumn<>("Client");
        TableColumn<ContratMourabaha, Double> cAchat  = new TableColumn<>("Prix achat (MRU)");
        TableColumn<ContratMourabaha, String> cVente  = new TableColumn<>("Prix vente (MRU)");
        TableColumn<ContratMourabaha, String> cMens   = new TableColumn<>("Mensualité");
        TableColumn<ContratMourabaha, String> cBenef  = new TableColumn<>("Bénéfice (MRU)");
        TableColumn<ContratMourabaha, String> cStatut = new TableColumn<>("Statut");

        cId.setCellValueFactory(new PropertyValueFactory<>("idContrat"));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateContrat() != null ? d.getValue().getDateContrat().format(fmt) : "—"));
        cClient.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getClient() != null ? d.getValue().getClient().getNom() : "—"));
        cAchat.setCellValueFactory(new PropertyValueFactory<>("prixAchatAgence"));
        cVente.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.2f", d.getValue().getPrixVenteClient())));
        cMens.setCellValueFactory(d -> {
            ContratMourabaha c = d.getValue();
            return new SimpleStringProperty(c.getDureeMois() != null && c.getDureeMois() > 0
                    ? String.format("%.2f", c.getPrixVenteClient() / c.getDureeMois()) : "—");
        });
        cBenef.setCellValueFactory(d -> {
            ContratMourabaha c = d.getValue();
            double b = c.getPrixAchatAgence() * c.getMargeBeneficiaire() / 100;
            return new SimpleStringProperty(String.format("%.2f", b));
        });
        cStatut.setCellValueFactory(new PropertyValueFactory<>("statutContrat"));

        // Couleur bénéfice
        cBenef.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item + " MRU");
                setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        // Couleur statut contrat
        cStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                String color = switch (item) {
                    case "EN_COURS"  -> "#2980b9";
                    case "CLOTURE"   -> "#27ae60";
                    case "SUSPENDU"  -> "#e67e22";
                    default          -> "#555";
                };
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        tableContrats.getColumns().addAll(cId, cDate, cClient, cAchat, cVente, cMens, cBenef, cStatut);
        tableContrats.setItems(dataContrats);
        card.getChildren().add(tableContrats);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 3 — HISTORIQUE DES PAIEMENTS
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionPaiements() {
        VBox card = sectionCard("✅  Historique des Paiements (Échéances Payées)", "#27ae60");

        tablePaiements = new TableView<>();
        tablePaiements.setPrefHeight(240);
        tablePaiements.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablePaiements.setPlaceholder(new Label("Aucun paiement enregistré."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        TableColumn<Echeance, Long>    cId      = new TableColumn<>("ID Échéance");
        TableColumn<Echeance, Integer> cNum     = new TableColumn<>("N°");
        TableColumn<Echeance, String>  cClient  = new TableColumn<>("Client");
        TableColumn<Echeance, String>  cContrat = new TableColumn<>("Contrat #");
        TableColumn<Echeance, String>  cDate    = new TableColumn<>("Date échéance");
        TableColumn<Echeance, Double>  cMont    = new TableColumn<>("Montant (MRU)");
        TableColumn<Echeance, String>  cStat    = new TableColumn<>("Statut");

        cId.setCellValueFactory(new PropertyValueFactory<>("idEchange"));
        cNum.setCellValueFactory(new PropertyValueFactory<>("numeroEchange"));
        cClient.setCellValueFactory(d -> {
            Echeance ec = d.getValue();
            if (ec.getContrat() != null && ec.getContrat().getClient() != null)
                return new SimpleStringProperty(ec.getContrat().getClient().getNom());
            return new SimpleStringProperty("—");
        });
        cContrat.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getContrat() != null
                        ? "#" + d.getValue().getContrat().getIdContrat() : "—"));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateEchange() != null
                        ? d.getValue().getDateEchange().format(fmt) : "—"));
        cMont.setCellValueFactory(new PropertyValueFactory<>("montant"));

        cStat.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));
        cStat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                if ("PAYEE".equals(item)) {
                    setText("✅  PAYÉE");
                    setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
                } else {
                    setText("🕐  À PAYER");
                    setStyle("-fx-text-fill:#e67e22;-fx-font-family:Georgia;");
                }
            }
        });

        cMont.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(String.format("%.2f MRU", item));
                setStyle("-fx-font-family:Georgia;-fx-font-weight:bold;-fx-text-fill:#1b5e20;");
            }
        });

        tablePaiements.getColumns().addAll(cId, cNum, cClient, cContrat, cDate, cMont, cStat);
        tablePaiements.setItems(dataPaiements);
        card.getChildren().add(tablePaiements);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 4 — HISTORIQUE DES CONNEXIONS
    // ═══════════════════════════════════════════════════════
    private VBox buildSectionConnexions() {
        VBox card = sectionCard("🔐  Historique des Connexions Agent", "#8e44ad");

        Label info = new Label(
                "ℹ️  Journal des sessions enregistrées pour l'agent connecté.\n" +
                        "     Les connexions de la session actuelle sont affichées en premier.");
        info.setFont(Font.font("Georgia", 12));
        info.setStyle("-fx-text-fill:#555;-fx-background-color:#f5eef8;" +
                "-fx-padding:10 14;-fx-background-radius:8;");
        info.setWrapText(true);

        tableConnexions = new TableView<>();
        tableConnexions.setPrefHeight(200);
        tableConnexions.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableConnexions.setPlaceholder(new Label("Aucune connexion enregistrée."));

        TableColumn<String[], String> cDate    = new TableColumn<>("Date & Heure");
        TableColumn<String[], String> cAgent   = new TableColumn<>("Agent");
        TableColumn<String[], String> cAction  = new TableColumn<>("Action");
        TableColumn<String[], String> cResultat= new TableColumn<>("Résultat");

        cDate.setCellValueFactory(d     -> new SimpleStringProperty(d.getValue()[0]));
        cAgent.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue()[1]));
        cAction.setCellValueFactory(d   -> new SimpleStringProperty(d.getValue()[2]));
        cResultat.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[3]));

        // Couleur résultat
        cResultat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                String color = "Succès".equals(item) ? "#27ae60" : "#c0392b";
                setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });

        // Couleur action
        cAction.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item);
                String color = "Connexion".equals(item) ? "#2980b9" : "#e67e22";
                setStyle("-fx-text-fill:" + color + ";-fx-font-family:Georgia;-fx-font-weight:bold;");
            }
        });

        tableConnexions.getColumns().addAll(cDate, cAgent, cAction, cResultat);

        // Remplir avec le journal statique
        dataConnexions.setAll(journalConnexions);
        tableConnexions.setItems(dataConnexions);

        // Bouton pour simuler une nouvelle entrée de journal
        Button btnNouveau = btn("➕  Ajouter entrée journal", "#8e44ad", "#6a1b9a");
        Label msgConn = new Label(""); msgConn.setFont(Font.font("Georgia", 12));
        btnNouveau.setOnAction(e -> {
            String ts2 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            String[] entree = {ts2, UserSession.getInstance().getNom(), "Navigation", "Succès"};
            journalConnexions.add(0, entree);
            dataConnexions.setAll(journalConnexions);
            lblTotalConnexions.setText(String.valueOf(dataConnexions.size()));
            msgConn.setText("✅  Entrée ajoutée au journal.");
            msgConn.setStyle("-fx-text-fill:#8e44ad;-fx-font-family:Georgia;");
        });

        card.getChildren().addAll(info, tableConnexions, btnNouveau, msgConn);
        return card;
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT & FILTRE
    // ═══════════════════════════════════════════════════════
    private void chargerTout() {
        try {
            toutesLesDemandes = repoD.findAll();
        } catch (Exception e) {
            System.err.println("[HistoriqueView] Demandes : " + e.getMessage());
            toutesLesDemandes = new ArrayList<>();
        }
        try {
            tousLesContrats = repoC.findAll();
        } catch (Exception e) {
            System.err.println("[HistoriqueView] Contrats : " + e.getMessage());
            tousLesContrats = new ArrayList<>();
        }
        try {
            // Toutes les échéances récupérées contrat par contrat
            tousLesPaiements = new ArrayList<>();
            for (ContratMourabaha c : tousLesContrats) {
                List<Echeance> echs = repoE.findByContratId(c.getIdContrat());
                // Rattacher le contrat complet (avec client) à chaque échéance
                for (Echeance ec : echs) {
                    ec.setContrat(c);
                }
                tousLesPaiements.addAll(echs);
            }
        } catch (Exception e) {
            System.err.println("[HistoriqueView] Paiements : " + e.getMessage());
            tousLesPaiements = new ArrayList<>();
        }

        // Connexions
        dataConnexions.setAll(journalConnexions);

        appliquerFiltre();
    }

    private void appliquerFiltre() {
        String filtre = filtreClientField != null
                ? filtreClientField.getText().trim().toLowerCase()
                : "";

        // ── Demandes ──
        List<DemandeAchat> demandesFiltrees = filtre.isEmpty()
                ? toutesLesDemandes
                : toutesLesDemandes.stream()
                .filter(d -> nomClient(d).toLowerCase().contains(filtre))
                .collect(Collectors.toList());
        dataDemandes.setAll(demandesFiltrees);

        // ── Contrats ──
        List<ContratMourabaha> contratsFiltres = filtre.isEmpty()
                ? tousLesContrats
                : tousLesContrats.stream()
                .filter(c -> c.getClient() != null &&
                        c.getClient().getNom() != null &&
                        c.getClient().getNom().toLowerCase().contains(filtre))
                .collect(Collectors.toList());
        dataContrats.setAll(contratsFiltres);

        // ── Paiements ──
        List<Echeance> paiementsFiltres = filtre.isEmpty()
                ? tousLesPaiements
                : tousLesPaiements.stream()
                .filter(ec -> {
                    if (ec.getContrat() == null || ec.getContrat().getClient() == null) return false;
                    String nom = ec.getContrat().getClient().getNom();
                    return nom != null && nom.toLowerCase().contains(filtre);
                })
                .collect(Collectors.toList());
        dataPaiements.setAll(paiementsFiltres);

        // ── Mise à jour KPI ──
        if (lblTotalDemandes  != null) lblTotalDemandes.setText(String.valueOf(dataDemandes.size()));
        if (lblTotalContrats  != null) lblTotalContrats.setText(String.valueOf(dataContrats.size()));
        if (lblTotalPaiements != null) lblTotalPaiements.setText(String.valueOf(dataPaiements.size()));
        if (lblTotalConnexions!= null) lblTotalConnexions.setText(String.valueOf(dataConnexions.size()));
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════
    private String nomClient(DemandeAchat d) {
        if (d.getIdClient() == null) return "—";
        return d.getIdClient().getNom() != null
                ? d.getIdClient().getNom()
                : "Client #" + d.getIdClient().getIdClient();
    }

    private VBox sectionCard(String titre, String color) {
        VBox card = new VBox(14);
        card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;" +
                "-fx-border-color:" + color + ";-fx-border-width:0 0 0 5;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t = new Label(titre);
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        t.setStyle("-fx-text-fill:" + color + ";");
        card.getChildren().add(t);
        return card;
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

    private Button actionBtn(String icon, String text) {
        Button btn = new Button(icon + "  " + text);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    // ═══════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t = new Label("📊  Historique — Espace Agent");
        t.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label user = new Label("👩‍💼  " + UserSession.getInstance().getNom());
        user.setFont(Font.font("Georgia", 13));
        user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t, sp, user);
        return bar;
    }

    // ═══════════════════════════════════════════════════════
    // SIDEBAR
    // ═══════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(230); sb.setMinWidth(230);
        sb.setStyle("-fx-background-color:#1b5e20;");

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
        Label roleBadge = new Label("👩‍💼  AGENT");
        roleBadge.setStyle(
                "-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;" +
                        "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;" +
                        "-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll( appName, mru, roleBadge);

        VBox menu = new VBox(4);
        menu.setPadding(new Insets(18, 10, 18, 10));

        Button bA = sideBtn("🏠", "Accueil",              false);
        Button bD = sideBtn("📨", "Demandes Clients",     false);
        Button bF = sideBtn("🏭", "Fournisseurs",         false);
        Button bC = sideBtn("📋", "Contrats Mourabaha",   false);
        Button bE = sideBtn("✅", "Paiements Échéances",  false);
        Button bH = sideBtn("📊", "Historique",           true);   // ← actif


        bA.setOnAction(e -> nav(new AccueilView(primaryStage).getRoot(),       "Accueil"));
        bD.setOnAction(e -> nav(new DemandeAgentView(primaryStage).getRoot(),  "Demandes Clients"));
        bF.setOnAction(e -> nav(new FournisseurView(primaryStage).getRoot(),   "Fournisseurs"));
        bC.setOnAction(e -> nav(new ContratView(primaryStage).getRoot(),       "Contrats"));
        bE.setOnAction(e -> nav(new EcheanceView(primaryStage).getRoot(),      "Paiements"));
        bH.setOnAction(e -> nav(new HistoriqueView(primaryStage).getRoot(),    "Historique"));

        menu.getChildren().addAll(bA, bD, bF, bC, bE, bH);

        VBox bottom = new VBox(8);
        bottom.setPadding(new Insets(20));
        VBox.setVgrow(bottom, Priority.ALWAYS);
        bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout = new Button("⬅   Déconnexion");
        logout.setMaxWidth(Double.MAX_VALUE);
        String lo = "-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh = "-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
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

    private Button sideBtn(String icon, String text, boolean active) {
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

    private void nav(javafx.scene.Parent r, String titre) {
        primaryStage.getScene().setRoot(r);
        primaryStage.setTitle("MicroFinance MRU — " + titre);
    }

    public BorderPane getRoot() { return root; }
}