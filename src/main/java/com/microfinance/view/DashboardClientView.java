package com.microfinance.view;

import com.microfinance.model.Client;
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
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * DashboardClientView — Espace client Mourabaha.
 *
 * Nouveautés :
 *  ✅ Barre de progression (5 étapes) sur chaque demande
 *  ✅ Bouton "💳 Payer" sur chaque échéance dans l'échéancier
 *  ✅ Statuts complets : EN_ATTENTE / ACCEPTEE / COMMANDE_FOURNISSEUR / BIEN_RECU / CONTRAT_CREE / REFUSEE
 */
public class DashboardClientView {

    private BorderPane root;
    private Stage primaryStage;

    private final ContratMourabahaRepositoryImpl contratRepo  = new ContratMourabahaRepositoryImpl();
    private final EcheanceRepositoryImpl         echeanceRepo = new EcheanceRepositoryImpl();
    private final DemandeAchatRepositoryImpl     demandeRepo  = new DemandeAchatRepositoryImpl();

    private final ObservableList<ContratMourabaha> dataContrats  = FXCollections.observableArrayList();
    private final ObservableList<Echeance>         dataEcheances = FXCollections.observableArrayList();
    private final ObservableList<DemandeAchat>     dataDemandes  = FXCollections.observableArrayList();

    private Button btnAccueil, btnDemander, btnContrats, btnEcheances;

    public DashboardClientView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
        chargerDepuisDB();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");
        root.setLeft(buildSidebar());
        root.setTop(buildTopBar());
        afficherAccueil();
    }

    private void chargerDepuisDB() {
        Long idClient = UserSession.getInstance().getIdUtilisateur();
        try { dataContrats.setAll(contratRepo.findByClientId(idClient)); }
        catch (Exception e) { System.err.println("[Client] Contrats : " + e.getMessage()); }
        try { dataDemandes.setAll(demandeRepo.findByClientId(idClient)); }
        catch (Exception e) { System.err.println("[Client] Demandes : " + e.getMessage()); }
    }

    private void chargerEcheances(Long idContrat) {
        try { dataEcheances.setAll(echeanceRepo.findByContratId(idContrat)); }
        catch (Exception e) { System.err.println("[Client] Échéances : " + e.getMessage()); }
    }

    // ═══════════════════════════════════════════════════
    // PAGE ACCUEIL
    // ═══════════════════════════════════════════════════
    private void afficherAccueil() {
        updateActif(btnAccueil);
        VBox body = new VBox(22);
        body.setPadding(new Insets(28));
        body.getChildren().add(buildHeroBanner());

        HBox cartes = new HBox(20);
        cartes.setAlignment(Pos.CENTER);
        VBox c1 = carteAction("🛒","Demander un bien","Soumettez votre demande à l'agence","#1b5e20","#eafaf1");
        VBox c2 = carteAction("📋","Mes contrats","Prix du bien, marge et total","#2980b9","#eaf4fb");
        VBox c3 = carteAction("📅","Mon échéancier","Calendrier de remboursement","#e67e22","#fef9e7");
        ((Button) c1.getChildren().get(3)).setOnAction(e -> afficherDemandeAchat());
        ((Button) c2.getChildren().get(3)).setOnAction(e -> afficherMesContrats());
        ((Button) c3.getChildren().get(3)).setOnAction(e -> afficherMonEcheancier());
        HBox.setHgrow(c1,Priority.ALWAYS); HBox.setHgrow(c2,Priority.ALWAYS); HBox.setHgrow(c3,Priority.ALWAYS);
        cartes.getChildren().addAll(c1,c2,c3);
        body.getChildren().addAll(cartes, buildProcessusBanner());
        setContent(body);
    }

    // ═══════════════════════════════════════════════════
    // PAGE DEMANDE D'ACHAT — avec barre de progression
    // ═══════════════════════════════════════════════════
    private void afficherDemandeAchat() {
        updateActif(btnDemander);
        VBox body = new VBox(20);
        body.setPadding(new Insets(28));
        body.getChildren().add(pageTitre("🛒  Demander un Bien — Mourabaha", "#1b5e20"));

        Label info = new Label(
                "ℹ️  Votre demande est sauvegardée en base de données.\n" +
                        "     L'agent Mourabaha la traitera en 5 étapes — suivez la progression ci-dessous.");
        info.setFont(Font.font("Georgia",13));
        info.setStyle("-fx-text-fill:#2e7d32;-fx-background-color:#eafaf1;-fx-padding:12 16;-fx-background-radius:10;");
        info.setWrapText(true);

        // ── Formulaire nouvelle demande ──
        VBox formCard = sectionCard("📝  Nouvelle demande", "#1b5e20");
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(14);
        TextField descField = champ("Ex: Voiture Toyota, Terrain 500m², Équipement agricole...");
        TextField prixField = champ("Montant estimé en MRU — chiffres uniquement (optionnel)");
        prixField.textProperty().addListener((obs, old, nv) -> { if (!nv.matches("[0-9]*\\.?[0-9]*")) prixField.setText(old); });
        ColumnConstraints lbl = new ColumnConstraints(160);
        ColumnConstraints fld = new ColumnConstraints(); fld.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(lbl,fld);
        grid.add(fieldLabel("Description du bien *"), 0, 0); grid.add(descField, 1, 0);
        grid.add(fieldLabel("Prix estimé (MRU)"),     0, 1); grid.add(prixField, 1, 1);

        Label msgLbl = new Label(""); msgLbl.setFont(Font.font("Georgia",13)); msgLbl.setWrapText(true);
        Button btnSoumettre = btn("💾  Soumettre la demande", "#1b5e20", "#27ae60");
        btnSoumettre.setOnAction(e -> {
            String desc = descField.getText().trim();
            if (desc.isEmpty()) { msgLbl.setText("❌  La description est obligatoire."); msgLbl.setStyle("-fx-text-fill:#c0392b;"); return; }
            try {
                Client client = new Client(); client.setIdClient(UserSession.getInstance().getIdUtilisateur());
                Double prixEstime = null;
                if (!prixField.getText().trim().isEmpty()) {
                    prixEstime = Double.parseDouble(prixField.getText().trim());
                    if (prixEstime <= 0) { msgLbl.setText("❌  Le prix doit être positif."); msgLbl.setStyle("-fx-text-fill:#c0392b;"); return; }
                }
                DemandeAchat demande = new DemandeAchat(desc, prixEstime, client);
                demandeRepo.save(demande);
                dataDemandes.setAll(demandeRepo.findByClientId(client.getIdClient()));
                msgLbl.setText("✅  Demande soumise ! Suivez la progression dans le tableau ci-dessous.");
                msgLbl.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;");
                descField.clear(); prixField.clear();
                // Rafraîchir la section historique
                afficherDemandeAchat();
            } catch (Exception ex) { msgLbl.setText("❌  " + ex.getMessage()); msgLbl.setStyle("-fx-text-fill:#c0392b;"); }
        });
        formCard.getChildren().addAll(grid, btnSoumettre, msgLbl);

        // ── Historique avec barre de progression ──
        VBox histCard = sectionCard("📋  Mes demandes — Suivi en temps réel", "#555");

        // Recharger les demandes
        try { dataDemandes.setAll(demandeRepo.findByClientId(UserSession.getInstance().getIdUtilisateur())); }
        catch (Exception ignored) {}

        if (dataDemandes.isEmpty()) {
            Label vide = new Label("Aucune demande — soumettez votre première demande ci-dessus.");
            vide.setFont(Font.font("Georgia", 13)); vide.setStyle("-fx-text-fill:#888;");
            histCard.getChildren().add(vide);
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (DemandeAchat dem : dataDemandes) {
                histCard.getChildren().add(buildCarteDemandeClient(dem, fmt));
            }
        }

        body.getChildren().addAll(info, formCard, histCard);
        setContent(body);
    }

    // ── Carte individuelle pour une demande avec progression ──
    private VBox buildCarteDemandeClient(DemandeAchat dem, DateTimeFormatter fmt) {
        VBox carte = new VBox(10); carte.setPadding(new Insets(16));
        String[] si = statutClientInfo(dem.getStatutDemande());
        carte.setStyle("-fx-background-color:#fafafa;-fx-background-radius:12;-fx-border-color:" + si[1] +
                ";-fx-border-width:0 0 0 4;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.05),6,0,0,2);");

        // En-tête de la carte
        HBox header = new HBox(14); header.setAlignment(Pos.CENTER_LEFT);
        Label dateL  = new Label(dem.getDateDemande() != null ? dem.getDateDemande().format(fmt) : "—");
        dateL.setFont(Font.font("Georgia", 11)); dateL.setStyle("-fx-text-fill:#888;");
        Label descL  = new Label("📦  " + dem.getDescriptionBien());
        descL.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); descL.setStyle("-fx-text-fill:#333;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label statL  = new Label(si[0]);
        statL.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        statL.setStyle("-fx-text-fill:white;-fx-background-color:" + si[1] + ";-fx-padding:4 10;-fx-background-radius:20;");
        header.getChildren().addAll(dateL, descL, sp, statL);

        // Infos supplémentaires
        HBox infos = new HBox(20); infos.setAlignment(Pos.CENTER_LEFT);
        if (dem.getPrixEstime() != null) {
            Label prixL = new Label("💰  " + String.format("%.0f MRU", dem.getPrixEstime()));
            prixL.setFont(Font.font("Georgia", 12)); prixL.setStyle("-fx-text-fill:#2980b9;");
            infos.getChildren().add(prixL);
        }
        if (dem.getNomFournisseur() != null) {
            Label fournL = new Label("🏭  " + dem.getNomFournisseur());
            fournL.setFont(Font.font("Georgia", 12)); fournL.setStyle("-fx-text-fill:#8e44ad;");
            infos.getChildren().add(fournL);
        }

        // Barre de progression
        HBox progression = buildBarreProgression(dem.getStatutDemande());

        carte.getChildren().addAll(header, infos, progression);
        return carte;
    }

    // ── Barre de progression en 5 étapes ──
    private HBox buildBarreProgression(String statut) {
        int etapeActuelle = etapeNumero(statut);
        String[] etapeLabels = {"Soumise", "Acceptée", "Commandée", "Reçue", "Contrat"};
        String[] etapeIcons  = {"📝", "✅", "🚚", "📦", "📋"};
        String[] etapeColors = {"#e67e22", "#2980b9", "#8e44ad", "#16a085", "#27ae60"};

        HBox barre = new HBox(0); barre.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < 5; i++) {
            boolean done   = i < etapeActuelle;
            boolean active = i == etapeActuelle - 1 && !"REFUSEE".equals(statut);
            boolean refused = "REFUSEE".equals(statut) && i == 0;

            VBox etapeBox = new VBox(4); etapeBox.setAlignment(Pos.CENTER); etapeBox.setPadding(new Insets(6, 10, 6, 10));
            HBox.setHgrow(etapeBox, Priority.ALWAYS);

            String bgColor;
            String txtColor;
            if (refused && i == 0) {
                bgColor = "#c0392b"; txtColor = "white";
            } else if (done || active) {
                bgColor = etapeColors[i]; txtColor = "white";
            } else {
                bgColor = "#e0e0e0"; txtColor = "#999";
            }

            String icon = refused && i == 0 ? "❌" : (done || active ? etapeIcons[i] : "○");
            Label iconL = new Label(icon); iconL.setFont(Font.font("Georgia", 13)); iconL.setStyle("-fx-text-fill:" + txtColor + ";");
            Label lblL  = new Label(etapeLabels[i]); lblL.setFont(Font.font("Georgia", 9)); lblL.setStyle("-fx-text-fill:" + txtColor + ";-fx-text-alignment:center;"); lblL.setWrapText(true);
            etapeBox.setStyle("-fx-background-color:" + bgColor + ";" + (i == 0 ? "-fx-background-radius:8 0 0 8;" : i == 4 ? "-fx-background-radius:0 8 8 0;" : ""));
            etapeBox.getChildren().addAll(iconL, lblL);

            barre.getChildren().add(etapeBox);

            // Flèche entre étapes
            if (i < 4) {
                Label fl = new Label("›"); fl.setStyle("-fx-text-fill:#ccc;-fx-font-size:16px;-fx-padding:0;"); barre.getChildren().add(fl);
            }
        }

        // Message si refusée
        if ("REFUSEE".equals(statut)) {
            Label refMsg = new Label("  ❌  Demande refusée — vous pouvez faire une nouvelle demande.");
            refMsg.setFont(Font.font("Georgia", 11)); refMsg.setStyle("-fx-text-fill:#c0392b;");
        }

        return barre;
    }

    private int etapeNumero(String statut) {
        return switch (statut) {
            case "EN_ATTENTE"           -> 1;
            case "ACCEPTEE"             -> 2;
            case "COMMANDE_FOURNISSEUR" -> 3;
            case "BIEN_RECU"            -> 4;
            case "CONTRAT_CREE"         -> 5;
            case "REFUSEE"              -> 1;
            default                     -> 0;
        };
    }

    private String[] statutClientInfo(String statut) {
        return switch (statut) {
            case "EN_ATTENTE"           -> new String[]{"⏳ En attente",         "#e67e22"};
            case "ACCEPTEE"             -> new String[]{"✅ Acceptée",           "#2980b9"};
            case "COMMANDE_FOURNISSEUR" -> new String[]{"🚚 En commande",        "#8e44ad"};
            case "BIEN_RECU"            -> new String[]{"📦 Bien reçu",          "#16a085"};
            case "CONTRAT_CREE"         -> new String[]{"📋 Contrat actif",      "#27ae60"};
            case "REFUSEE"              -> new String[]{"❌ Refusée",            "#c0392b"};
            default                     -> new String[]{statut,                  "#555"};
        };
    }

    // ═══════════════════════════════════════════════════
    // PAGE MES CONTRATS
    // ═══════════════════════════════════════════════════
    private void afficherMesContrats() {
        updateActif(btnContrats);
        try { dataContrats.setAll(contratRepo.findByClientId(UserSession.getInstance().getIdUtilisateur())); }
        catch (Exception e) { System.err.println("[Client] " + e.getMessage()); }

        VBox body = new VBox(20); body.setPadding(new Insets(28));
        body.getChildren().add(pageTitre("📋  Mes Contrats Mourabaha", "#2980b9"));

        Label info = new Label("ℹ️  Prix du bien, marge bénéficiaire et total à rembourser.\n     Cliquez sur un contrat pour charger son échéancier.");
        info.setFont(Font.font("Georgia",13));
        info.setStyle("-fx-text-fill:#1565c0;-fx-background-color:#e3f2fd;-fx-padding:12 16;-fx-background-radius:10;");
        info.setWrapText(true);

        if (!dataContrats.isEmpty()) {
            double totalDu = dataContrats.stream().mapToDouble(ContratMourabaha::getPrixVenteClient).sum();
            long   enCours = dataContrats.stream().filter(c -> "EN_COURS".equals(c.getStatutContrat())).count();
            HBox kpis = new HBox(16);
            kpis.getChildren().addAll(
                    kpiMini("📋", String.valueOf(dataContrats.size()), "Contrats",  "#2980b9","#eaf4fb"),
                    kpiMini("🔵", String.valueOf(enCours),            "En cours",  "#27ae60","#eafaf1"),
                    kpiMini("💰", String.format("%.0f MRU", totalDu), "Total dû",  "#e67e22","#fef9e7")
            );
            for (var k : kpis.getChildren()) HBox.setHgrow((Region)k, Priority.ALWAYS);
            body.getChildren().add(kpis);
        }

        VBox card = sectionCard("📋  Liste de mes contrats", "#2980b9");
        TableView<ContratMourabaha> table = new TableView<>();
        table.setPrefHeight(280); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(dataContrats);
        table.setPlaceholder(new Label("Aucun contrat — votre demande est en cours de traitement."));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TableColumn<ContratMourabaha, String>  cDate  = new TableColumn<>("Date");
        TableColumn<ContratMourabaha, String>  cAchat = new TableColumn<>("Prix du bien");
        TableColumn<ContratMourabaha, String>  cMarge = new TableColumn<>("Marge %");
        TableColumn<ContratMourabaha, String>  cVente = new TableColumn<>("Total à payer");
        TableColumn<ContratMourabaha, String>  cMens  = new TableColumn<>("Mensualité");
        TableColumn<ContratMourabaha, Integer> cDuree = new TableColumn<>("Durée");
        TableColumn<ContratMourabaha, String>  cStat  = new TableColumn<>("Statut");

        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateContrat() != null ? d.getValue().getDateContrat().format(fmt) : ""));
        cAchat.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f MRU", d.getValue().getPrixAchatAgence())));
        cMarge.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.1f %%", d.getValue().getMargeBeneficiaire())));
        cVente.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f MRU", d.getValue().getPrixVenteClient())));
        cMens.setCellValueFactory(d -> {
            ContratMourabaha c = d.getValue();
            if (c.getDureeMois() != null && c.getDureeMois() > 0)
                return new SimpleStringProperty(String.format("%.2f MRU", c.getPrixVenteClient()/c.getDureeMois()));
            return new SimpleStringProperty("—");
        });
        cDuree.setCellValueFactory(new PropertyValueFactory<>("dureeMois"));
        cStat.setCellValueFactory(new PropertyValueFactory<>("statutContrat"));

        cVente.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item); setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");
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
        table.getColumns().addAll(cDate, cAchat, cMarge, cVente, cMens, cDuree, cStat);
        table.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel != null) chargerEcheances(sel.getIdContrat());
        });

        Label hint = new Label("💡  Cliquez sur un contrat, puis allez dans « Mon échéancier ».");
        hint.setFont(Font.font("Georgia",12)); hint.setStyle("-fx-text-fill:#888;");
        Button btnEch = btn("📅  Voir mon échéancier →", "#e67e22", "#d35400");
        btnEch.setOnAction(e -> afficherMonEcheancier());

        card.getChildren().addAll(table, hint, btnEch);
        body.getChildren().addAll(info, card);
        setContent(body);
    }

    // ═══════════════════════════════════════════════════
    // PAGE ÉCHÉANCIER — avec bouton Payer
    // ═══════════════════════════════════════════════════
    private void afficherMonEcheancier() {
        updateActif(btnEcheances);
        VBox body = new VBox(20); body.setPadding(new Insets(28));
        body.getChildren().add(pageTitre("📅  Mon Échéancier de Paiement", "#e67e22"));

        if (dataEcheances.isEmpty() && !dataContrats.isEmpty()) {
            Label hint = new Label("💡  Allez dans « Mes contrats », cliquez sur un contrat,\n     puis revenez ici pour voir son échéancier.");
            hint.setFont(Font.font("Georgia",13));
            hint.setStyle("-fx-text-fill:#d35400;-fx-background-color:#fef9e7;-fx-padding:14 18;-fx-background-radius:10;");
            hint.setWrapText(true);
            Button btnGo = btn("📋  Aller à Mes contrats", "#2980b9","#1a6090");
            btnGo.setOnAction(e -> afficherMesContrats());
            body.getChildren().addAll(hint, btnGo);
            setContent(body); return;
        }

        if (dataEcheances.isEmpty()) {
            Label noData = new Label("Aucun contrat trouvé. Votre contrat sera créé après acceptation de votre demande.");
            noData.setFont(Font.font("Georgia", 13)); noData.setStyle("-fx-text-fill:#888;"); noData.setWrapText(true);
            body.getChildren().add(noData);
            setContent(body); return;
        }

        Label info = new Label("ℹ️  Montant fixe chaque mois — aucun intérêt ne s'ajoute (principe Mourabaha).");
        info.setFont(Font.font("Georgia",13));
        info.setStyle("-fx-text-fill:#d35400;-fx-background-color:#fef9e7;-fx-padding:12 16;-fx-background-radius:10;");

        long payees  = dataEcheances.stream().filter(e -> "PAYEE".equals(e.getStatutPaiement())).count();
        long restant = dataEcheances.size() - payees;
        double montantRestant = dataEcheances.stream().filter(e -> !"PAYEE".equals(e.getStatutPaiement())).mapToDouble(Echeance::getMontant).sum();
        HBox kpis = new HBox(16);
        kpis.getChildren().addAll(
                kpiMini("✅", String.valueOf(payees),  "Payées",   "#27ae60","#eafaf1"),
                kpiMini("🕐", String.valueOf(restant), "Restantes","#e67e22","#fef9e7"),
                kpiMini("💰", String.format("%.0f MRU", montantRestant),"Restant","#c0392b","#fdecea")
        );
        for (var k : kpis.getChildren()) HBox.setHgrow((Region)k, Priority.ALWAYS);
        body.getChildren().add(kpis);

        VBox card = sectionCard("📅  Calendrier de remboursement", "#e67e22");

        Label msgPayer = new Label(""); msgPayer.setFont(Font.font("Georgia", 13)); msgPayer.setWrapText(true);

        TableView<Echeance> table = new TableView<>();
        table.setPrefHeight(340); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(dataEcheances);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TableColumn<Echeance, Integer> cNum   = new TableColumn<>("N°");
        TableColumn<Echeance, String>  cDate  = new TableColumn<>("Date paiement");
        TableColumn<Echeance, String>  cMont  = new TableColumn<>("Montant (MRU)");
        TableColumn<Echeance, String>  cStat  = new TableColumn<>("Statut");
        TableColumn<Echeance, String>  cAction= new TableColumn<>("Action");

        cNum.setCellValueFactory(new PropertyValueFactory<>("numeroEchange"));
        cDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDateEchange() != null ? d.getValue().getDateEchange().format(fmt) : ""));
        cMont.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.2f", d.getValue().getMontant())));
        cStat.setCellValueFactory(new PropertyValueFactory<>("statutPaiement"));

        cMont.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); if (empty||item==null){setText(null);return;}
                setText(item+" MRU"); setStyle("-fx-font-weight:bold;-fx-font-family:Georgia;");
            }
        });
        cStat.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty||item==null){setText(null);setStyle("");return;}
                Echeance row = getTableView().getItems().get(getIndex());
                if ("PAYEE".equals(item)) { setText("✅  PAYÉE"); setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;"); }
                else if (row!=null && row.estEnRetard()) { setText("⚠  EN RETARD"); setStyle("-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-font-family:Georgia;"); }
                else { setText("🕐  À PAYER"); setStyle("-fx-text-fill:#e67e22;-fx-font-family:Georgia;"); }
            }
        });

        // ── Colonne Action avec bouton Payer ──
        cAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnPayer = new Button("💳 Payer");
            {
                String s  = "-fx-background-color:#27ae60;-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:8;-fx-cursor:hand;";
                String sh = "-fx-background-color:#1e8449;-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:11px;-fx-padding:5 10;-fx-background-radius:8;-fx-cursor:hand;";
                btnPayer.setStyle(s);
                btnPayer.setOnMouseEntered(e -> btnPayer.setStyle(sh));
                btnPayer.setOnMouseExited(e  -> btnPayer.setStyle(s));
                btnPayer.setOnAction(e -> {
                    Echeance ech = getTableView().getItems().get(getIndex());
                    payerEcheance(ech, msgPayer);
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Echeance ech = getTableView().getItems().get(getIndex());
                btnPayer.setDisable("PAYEE".equals(ech.getStatutPaiement()));
                setGraphic(btnPayer);
            }
        });

        table.getColumns().addAll(cNum, cDate, cMont, cStat, cAction);
        card.getChildren().addAll(table, msgPayer);
        body.getChildren().addAll(info, card);
        setContent(body);
    }

    // ── Payer une échéance depuis le dashboard client ──
    private void payerEcheance(Echeance ech, Label msgPayer) {
        if ("PAYEE".equals(ech.getStatutPaiement())) {
            msgPayer.setText("ℹ️  Cette échéance est déjà payée."); msgPayer.setStyle("-fx-text-fill:#2980b9;"); return;
        }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                String.format("Confirmer le paiement de %.2f MRU pour l'échéance N°%d ?", ech.getMontant(), ech.getNumeroEchange()),
                ButtonType.YES, ButtonType.NO);
        conf.setHeaderText("Confirmation de paiement"); conf.setTitle("Paiement");
        if (conf.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                echeanceRepo.payerEcheance(ech.getIdEchange());
                msgPayer.setText("✅  Paiement de " + String.format("%.2f MRU", ech.getMontant()) + " enregistré pour l'échéance N°" + ech.getNumeroEchange() + " !");
                msgPayer.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-weight:bold;");
                // Rafraîchir l'échéancier
                if (!dataContrats.isEmpty()) {
                    ContratMourabaha selC = null;
                    for (ContratMourabaha c : dataContrats) {
                        if (ech.getContrat() != null && c.getIdContrat().equals(ech.getContrat().getIdContrat())) { selC = c; break; }
                    }
                    if (selC != null) chargerEcheances(selC.getIdContrat());
                    else chargerEcheances(dataContrats.get(0).getIdContrat());
                }
                afficherMonEcheancier();
            } catch (Exception ex) {
                msgPayer.setText("❌  Erreur : " + ex.getMessage()); msgPayer.setStyle("-fx-text-fill:#c0392b;");
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // HELPERS UI (inchangés)
    // ═══════════════════════════════════════════════════
    private void setContent(VBox body) {
        ScrollPane scroll = new ScrollPane(body); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");
        root.setCenter(scroll);
    }

    private void updateActif(Button actif) {
        String base     = "-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String actStyle = "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        for (Button b : new Button[]{btnAccueil,btnDemander,btnContrats,btnEcheances})
            if (b != null) b.setStyle(b == actif ? actStyle : base);
    }

    private StackPane buildHeroBanner() {
        StackPane banner = new StackPane(); banner.setMinHeight(130); banner.setMaxHeight(130);
        try {
            InputStream is = getClass().getResourceAsStream("/images/bg_billets.jpeg");
            if (is != null) {
                Image img = new Image(is);
                BackgroundImage bg = new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100,100,true,true,false,true));
                banner.setBackground(new Background(bg));
            }
        } catch (Exception ignored) {}
        Region ov = new Region(); ov.setMaxWidth(Double.MAX_VALUE); ov.setMaxHeight(Double.MAX_VALUE);
        ov.setStyle("-fx-background-color:linear-gradient(to right,rgba(20,82,20,0.90) 0%,rgba(20,82,20,0.55) 60%,rgba(0,0,0,0.05) 100%);-fx-background-radius:14;");
        VBox txt = new VBox(4); txt.setAlignment(Pos.CENTER_LEFT); txt.setPadding(new Insets(0,0,0,28)); StackPane.setAlignment(txt,Pos.CENTER_LEFT);
        Label w=new Label("Bonjour, "+UserSession.getInstance().getNom()+" 👋"); w.setFont(Font.font("Georgia",13)); w.setStyle("-fx-text-fill:#a5d6a7;");
        Label t=new Label("Mon Espace Client — Mourabaha"); t.setFont(Font.font("Georgia",FontWeight.BOLD,22)); t.setStyle("-fx-text-fill:white;");
        Label s=new Label("Demandez un bien · Suivez votre demande étape par étape · Payez vos échéances"); s.setFont(Font.font("Georgia",12)); s.setStyle("-fx-text-fill:#c8e6c9;");
        txt.getChildren().addAll(w,t,s); banner.getChildren().addAll(ov,txt); return banner;
    }

    private VBox buildProcessusBanner() {
        VBox card = new VBox(10); card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:12;-fx-border-color:#27ae60;-fx-border-width:0 0 0 5;");
        Label titre = new Label("📋  Le processus Mourabaha"); titre.setFont(Font.font("Georgia",FontWeight.BOLD,13)); titre.setStyle("-fx-text-fill:#1b5e20;");
        HBox etapes = new HBox(4); etapes.setAlignment(Pos.CENTER_LEFT);
        etapes.getChildren().addAll(
                ep("1️⃣","Vous\ndemandez","#27ae60",true), fl(),
                ep("2️⃣","Agence\nexamine","#2980b9",false), fl(),
                ep("3️⃣","Commande\nfournisseur","#8e44ad",false), fl(),
                ep("4️⃣","Livraison\ndu bien","#16a085",false), fl(),
                ep("5️⃣","Contrat\ncréé","#c0392b",false)
        );
        card.getChildren().addAll(titre, etapes); return card;
    }

    private VBox ep(String num, String txt, String color, boolean actif) {
        VBox b=new VBox(3); b.setAlignment(Pos.CENTER); b.setPadding(new Insets(7,10,7,10));
        b.setStyle(actif?"-fx-background-color:"+color+";-fx-background-radius:8;":"-fx-background-color:rgba(0,0,0,0.05);-fx-background-radius:8;");
        Label n=new Label(num); n.setFont(Font.font("Georgia",FontWeight.BOLD,13)); n.setStyle("-fx-text-fill:"+(actif?"white":color)+";");
        Label t=new Label(txt); t.setFont(Font.font("Georgia",10)); t.setStyle("-fx-text-fill:"+(actif?"white":"#555")+";-fx-text-alignment:center;"); t.setWrapText(true); t.setMaxWidth(75);
        b.getChildren().addAll(n,t); HBox.setHgrow(b,Priority.ALWAYS); return b;
    }
    private Label fl() { Label l=new Label("→"); l.setStyle("-fx-text-fill:#aaa;-fx-font-size:14px;"); l.setPadding(new Insets(0,2,0,2)); return l; }

    private VBox carteAction(String icon, String titre, String desc, String color, String bg) {
        VBox card=new VBox(12); card.setPadding(new Insets(22)); card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:"+color+";-fx-border-width:0 0 0 4;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label ico=new Label(icon); ico.setStyle("-fx-font-size:34px;");
        Label tit=new Label(titre); tit.setFont(Font.font("Georgia",FontWeight.BOLD,14)); tit.setStyle("-fx-text-fill:"+color+";");
        Label dsc=new Label(desc); dsc.setFont(Font.font("Georgia",11)); dsc.setStyle("-fx-text-fill:#666;-fx-text-alignment:center;"); dsc.setWrapText(true);
        Button b=btn("Accéder →",color,color); b.setMaxWidth(Double.MAX_VALUE);
        card.getChildren().addAll(ico,tit,dsc,b); return card;
    }

    private VBox kpiMini(String icon, String val, String label, String color, String bg) {
        VBox card=new VBox(4); card.setPadding(new Insets(12)); card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color:"+bg+";-fx-background-radius:10;-fx-border-color:"+color+";-fx-border-width:0 0 0 4;");
        Label v=new Label(icon+" "+val); v.setFont(Font.font("Georgia",FontWeight.BOLD,15)); v.setStyle("-fx-text-fill:"+color+";");
        Label l=new Label(label); l.setFont(Font.font("Georgia",11)); l.setStyle("-fx-text-fill:#666;");
        card.getChildren().addAll(v,l); return card;
    }

    private Label pageTitre(String t, String c) { Label l=new Label(t); l.setFont(Font.font("Georgia",FontWeight.BOLD,20)); l.setStyle("-fx-text-fill:"+c+";"); return l; }

    private VBox sectionCard(String titre, String color) {
        VBox card=new VBox(14); card.setPadding(new Insets(22));
        card.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:"+color+";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label t=new Label(titre); t.setFont(Font.font("Georgia",FontWeight.BOLD,14)); t.setStyle("-fx-text-fill:"+color+";");
        card.getChildren().add(t); return card;
    }

    private TextField champ(String p) {
        TextField tf=new TextField(); tf.setPromptText(p); tf.setMaxWidth(Double.MAX_VALUE);
        tf.setStyle("-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 12;-fx-font-family:Georgia;-fx-font-size:13px;");
        return tf;
    }

    private Label fieldLabel(String t) { Label l=new Label(t); l.setFont(Font.font("Georgia",FontWeight.BOLD,12)); l.setStyle("-fx-text-fill:#333;"); return l; }

    private Button btn(String text, String bg, String hover) {
        Button b=new Button(text);
        String s="-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String sh="-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        b.setStyle(s); b.setOnMouseEntered(e->b.setStyle(sh)); b.setOnMouseExited(e->b.setStyle(s)); return b;
    }

    private VBox buildSidebar() {
        VBox sb=new VBox(0); sb.setPrefWidth(220); sb.setStyle("-fx-background-color:#1b5e20;");
        VBox logoBox=new VBox(8); logoBox.setAlignment(Pos.CENTER); logoBox.setPadding(new Insets(28,20,28,20)); logoBox.setStyle("-fx-background-color:#145214;");

        Label appName=new Label("MicroFinance"); appName.setFont(Font.font("Georgia",FontWeight.BOLD,15)); appName.setStyle("-fx-text-fill:white;");
        Label mru=new Label("MRU"); mru.setFont(Font.font("Georgia",FontWeight.BOLD,12)); mru.setStyle("-fx-text-fill:#a5d6a7;");
        Label badge=new Label("👤  CLIENT");
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:4 10;-fx-background-radius:20;");
        logoBox.getChildren().addAll(appName,mru,badge);

        btnAccueil  = sideBtn("🏠","Accueil");
        btnDemander = sideBtn("🛒","Demander un bien");
        btnContrats = sideBtn("📋","Mes contrats");
        btnEcheances= sideBtn("📅","Mon échéancier");

        btnAccueil.setOnAction(e   -> afficherAccueil());
        btnDemander.setOnAction(e  -> afficherDemandeAchat());
        btnContrats.setOnAction(e  -> afficherMesContrats());
        btnEcheances.setOnAction(e -> afficherMonEcheancier());

        VBox menu=new VBox(4); menu.setPadding(new Insets(18,10,18,10));
        menu.getChildren().addAll(btnAccueil,btnDemander,btnContrats,btnEcheances);

        VBox bottom=new VBox(8); bottom.setPadding(new Insets(20)); VBox.setVgrow(bottom,Priority.ALWAYS); bottom.setAlignment(Pos.BOTTOM_CENTER);
        Button logout=new Button("⬅   Déconnexion"); logout.setMaxWidth(Double.MAX_VALUE);
        String lo="-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh="-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        logout.setStyle(lo); logout.setOnMouseEntered(e->logout.setStyle(lh)); logout.setOnMouseExited(e->logout.setStyle(lo));
        logout.setOnAction(e->{ UserSession.getInstance().logout(); LoginView lv=new LoginView(primaryStage); primaryStage.getScene().setRoot(lv.getRoot()); primaryStage.setTitle("MicroFinance MRU — Connexion"); });
        bottom.getChildren().add(logout);
        sb.getChildren().addAll(logoBox,menu,bottom);
        return sb;
    }

    private Button sideBtn(String icon, String text) {
        Button btn=new Button(icon+"   "+text); btn.setMaxWidth(Double.MAX_VALUE); btn.setAlignment(Pos.CENTER_LEFT);
        String base="-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover="-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e->{ if(!btn.getStyle().contains("0.18")) btn.setStyle(hover); });
        btn.setOnMouseExited(e ->{ if(!btn.getStyle().contains("0.18")) btn.setStyle(base); });
        return btn;
    }

    private HBox buildTopBar() {
        HBox bar=new HBox(); bar.setAlignment(Pos.CENTER_LEFT); bar.setPadding(new Insets(14,28,14,28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t=new Label("Espace Client — Mourabaha"); t.setFont(Font.font("Georgia",FontWeight.BOLD,18)); t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp=new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
        Label user=new Label("👤  "+UserSession.getInstance().getNom()); user.setFont(Font.font("Georgia",13)); user.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t,sp,user); return bar;
    }

    public BorderPane getRoot() { return root; }
}