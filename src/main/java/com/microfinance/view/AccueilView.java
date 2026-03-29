package com.microfinance.view;

import com.microfinance.Util.UserSession;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * Dashboard AGENT — Tableau de bord Mourabaha.
 * Accessible uniquement après connexion avec rôle AGENT.
 */
public class AccueilView {
    private BorderPane root;
    private Stage primaryStage;

    public AccueilView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f0;");
        root.setLeft(buildSidebar());
        root.setCenter(buildCentreAccueil());
    }

    // ═══════════════════════════════════════════════════════
    // CENTRE
    // ═══════════════════════════════════════════════════════
    private ScrollPane buildCentreAccueil() {
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox mainContent = new VBox(0);
        mainContent.setStyle("-fx-background-color:#f0f4f0;");

        // ── Hero Banner ──
        StackPane heroBanner = new StackPane();
        heroBanner.setMinHeight(210); heroBanner.setMaxHeight(210);
        try {
            InputStream is = getClass().getResourceAsStream("/images/bg_billets.jpeg");
            if (is != null) {
                Image bgImg = new Image(is);
                BackgroundImage bg = new BackgroundImage(bgImg,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, true));
                heroBanner.setBackground(new Background(bg));
            }
        } catch (Exception ignored) {}

        Region gradientOverlay = new Region();
        gradientOverlay.setMaxWidth(Double.MAX_VALUE);
        gradientOverlay.setMaxHeight(Double.MAX_VALUE);
        gradientOverlay.setStyle(
                "-fx-background-color:linear-gradient(to right," +
                        "rgba(20,82,20,0.92) 0%," +
                        "rgba(20,82,20,0.70) 45%," +
                        "rgba(20,82,20,0.10) 100%);");

        VBox bannerText = new VBox(8);
        bannerText.setAlignment(Pos.CENTER_LEFT);
        bannerText.setPadding(new Insets(0, 0, 0, 40));
        StackPane.setAlignment(bannerText, Pos.CENTER_LEFT);

        String nom = UserSession.getInstance().getNom();
        Label welcomeLbl = new Label("Bonjour, " + nom + " 👩‍💼");
        welcomeLbl.setFont(Font.font("Georgia", 14));
        welcomeLbl.setStyle("-fx-text-fill:#a5d6a7;");

        Label dashLbl = new Label("Espace Agent — Mourabaha");
        dashLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        dashLbl.setStyle("-fx-text-fill:white;");

        Label sloganLbl = new Label("Gérez les demandes, contrats, fournisseurs et paiements");
        sloganLbl.setFont(Font.font("Georgia", 13));
        sloganLbl.setStyle("-fx-text-fill:#c8e6c9;");

        HBox badge = new HBox(8);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(7, 14, 7, 14));
        badge.setStyle("-fx-background-color:rgba(241,196,15,0.22);-fx-background-radius:20;" +
                "-fx-border-color:#f1c40f;-fx-border-radius:20;");
        Label badgeLbl = new Label("✦  Jusqu'à 500 000 MRU disponibles");
        badgeLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        badgeLbl.setStyle("-fx-text-fill:#f1c40f;");
        badge.getChildren().add(badgeLbl);

        bannerText.getChildren().addAll(welcomeLbl, dashLbl, sloganLbl, badge);
        heroBanner.getChildren().addAll(gradientOverlay, bannerText);

        // ── Corps ──
        VBox body = new VBox(22);
        body.setPadding(new Insets(26, 28, 28, 28));

        // Alerte demandes en attente
        long nbEnAttente = 0;
        try {
            DemandeAchatRepositoryImpl dr = new DemandeAchatRepositoryImpl();
            nbEnAttente = dr.findAll().stream()
                    .filter(d -> "EN_ATTENTE".equals(d.getStatutDemande())).count();
        } catch (Exception ignored) {}

        if (nbEnAttente > 0) {
            HBox alerte = new HBox(14);
            alerte.setAlignment(Pos.CENTER_LEFT);
            alerte.setPadding(new Insets(14, 18, 14, 18));
            alerte.setStyle("-fx-background-color:#fff8e1;-fx-background-radius:12;" +
                    "-fx-border-color:#f1c40f;-fx-border-width:0 0 0 5;");
            Label alerteIcon = new Label("⏳");
            alerteIcon.setStyle("-fx-font-size:22px;");
            VBox alerteTxt = new VBox(2);
            Label alerteTitre = new Label(nbEnAttente + " demande(s) client en attente de votre réponse !");
            alerteTitre.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
            alerteTitre.setStyle("-fx-text-fill:#e67e22;");
            Label alerteDesc = new Label("Acceptez ou refusez les demandes pour débloquer la création de contrat.");
            alerteDesc.setFont(Font.font("Georgia", 12));
            alerteDesc.setStyle("-fx-text-fill:#7d6608;");
            alerteTxt.getChildren().addAll(alerteTitre, alerteDesc);
            Button alerteBtn = actionBtn("📨", "Voir les demandes");
            alerteBtn.setOnAction(e -> {
                primaryStage.getScene().setRoot(new DemandeAgentView(primaryStage).getRoot());
                primaryStage.setTitle("MicroFinance MRU — Demandes Clients");
            });
            alerte.getChildren().addAll(alerteIcon, alerteTxt, alerteBtn);
            HBox.setHgrow(alerteTxt, Priority.ALWAYS);
            body.getChildren().add(alerte);
        }

        // ── Stats ──
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                statCard("💰", "Solde agence",       "45 000 MRU", "#27ae60", "#eafaf1", "#27ae60"),
                statCard("📋", "Contrats actifs",    "3",          "#2980b9", "#eaf4fb", "#2980b9"),
                statCard("✅", "Paiements ce mois",  "12 500 MRU", "#e67e22", "#fef9e7", "#e67e22"),
                statCard("📊", "Taux remboursement", "98 %",       "#8e44ad", "#f5eef8", "#8e44ad")
        );
        for (var c : statsRow.getChildren()) HBox.setHgrow((Region) c, Priority.ALWAYS);

        // ── Actions rapides + mini carte ──
        HBox bottomRow = new HBox(18);
        bottomRow.setAlignment(Pos.TOP_LEFT);

        VBox actionsPanel = new VBox(10);
        actionsPanel.setPadding(new Insets(22));
        actionsPanel.setPrefWidth(300);
        actionsPanel.setStyle("-fx-background-color:white;-fx-background-radius:16;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");
        Label actTitle = new Label("Actions rapides");
        actTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        actTitle.setStyle("-fx-text-fill:#1b5e20;");

        Button btnD = actionBtn("📨", "Demandes Clients");
        Button btnF = actionBtn("🏭", "Gérer les fournisseurs");
        Button btnC = actionBtn("📋", "Contrats Mourabaha");
        Button btnP = actionBtn("✅", "Paiements échéances");
        Button btnH = actionBtn("📊", "Historique");
        Button btnB = actionBtn("💰", "Voir les bénéfices");

        btnD.setOnAction(e -> { primaryStage.getScene().setRoot(new DemandeAgentView(primaryStage).getRoot()); primaryStage.setTitle("MicroFinance MRU — Demandes"); });
        btnF.setOnAction(e -> { primaryStage.getScene().setRoot(new FournisseurView(primaryStage).getRoot());  primaryStage.setTitle("MicroFinance MRU — Fournisseurs"); });
        btnC.setOnAction(e -> { primaryStage.getScene().setRoot(new ContratView(primaryStage).getRoot());      primaryStage.setTitle("MicroFinance MRU — Contrats"); });
        btnP.setOnAction(e -> { primaryStage.getScene().setRoot(new EcheanceView(primaryStage).getRoot());     primaryStage.setTitle("MicroFinance MRU — Paiements"); });
        btnH.setOnAction(e -> { primaryStage.getScene().setRoot(new HistoriqueView(primaryStage).getRoot());   primaryStage.setTitle("MicroFinance MRU — Historique"); });
        btnB.setOnAction(e -> { primaryStage.getScene().setRoot(new BeneficeView(primaryStage).getRoot());     primaryStage.setTitle("MicroFinance MRU — Bénéfices"); });

        actionsPanel.getChildren().addAll(actTitle, btnD, btnF, btnC, btnP, btnH, btnB);

        // Mini carte visuelle
        StackPane miniCard = new StackPane();
        HBox.setHgrow(miniCard, Priority.ALWAYS);
        miniCard.setMinHeight(220);
        try {
            InputStream is2 = getClass().getResourceAsStream("/images/bg_billets.jpeg");
            if (is2 != null) {
                Image bgImg2 = new Image(is2);
                BackgroundImage bg2 = new BackgroundImage(bgImg2,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, true));
                miniCard.setBackground(new Background(bg2));
            }
        } catch (Exception ignored) {}

        Region miniOverlay = new Region();
        miniOverlay.setMaxWidth(Double.MAX_VALUE);
        miniOverlay.setMaxHeight(Double.MAX_VALUE);
        miniOverlay.setStyle(
                "-fx-background-color:linear-gradient(to top," +
                        "rgba(20,82,20,0.88) 0%," +
                        "rgba(20,82,20,0.30) 60%," +
                        "rgba(0,0,0,0.05) 100%);" +
                        "-fx-background-radius:16;");

        VBox miniText = new VBox(6);
        miniText.setAlignment(Pos.BOTTOM_LEFT);
        miniText.setPadding(new Insets(0, 0, 22, 22));
        StackPane.setAlignment(miniText, Pos.BOTTOM_LEFT);

        Label miniTitle = new Label("Mourabaha — Financement islamique");
        miniTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        miniTitle.setStyle("-fx-text-fill:white;");
        Label miniSub = new Label("Sans intérêt • Conforme à la charia");
        miniSub.setFont(Font.font("Georgia", 12));
        miniSub.setStyle("-fx-text-fill:#a5d6a7;");
        Button miniBtn = new Button("Voir les demandes  →");
        miniBtn.setStyle("-fx-background-color:#f1c40f;-fx-text-fill:#1b5e20;" +
                "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:12px;" +
                "-fx-padding:7 16;-fx-background-radius:18;-fx-cursor:hand;");
        miniBtn.setOnAction(e ->
                primaryStage.getScene().setRoot(new DemandeAgentView(primaryStage).getRoot()));
        miniText.getChildren().addAll(miniTitle, miniSub, miniBtn);
        miniCard.getChildren().addAll(miniOverlay, miniText);

        bottomRow.getChildren().addAll(actionsPanel, miniCard);
        body.getChildren().addAll(statsRow, bottomRow);
        mainContent.getChildren().addAll(heroBanner, body);
        scroll.setContent(mainContent);
        return scroll;
    }

    // ═══════════════════════════════════════════════════════
    // SIDEBAR — sans logo, juste nom + badge
    // ═══════════════════════════════════════════════════════
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.setPrefWidth(230); sb.setMinWidth(230);
        sb.setStyle("-fx-background-color:#1b5e20;");

        // ── En-tête logo box ──
        VBox logoBox = new VBox(8);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(28, 20, 28, 20));
        logoBox.setStyle("-fx-background-color:#145214;");

        Label appName = new Label("MicroFinance");
        appName.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        appName.setStyle("-fx-text-fill:white;");

        Label mru = new Label("MRU");
        mru.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        mru.setStyle("-fx-text-fill:#a5d6a7;");

        Label roleBadge = new Label("👩‍💼  AGENT");
        roleBadge.setStyle(
                "-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;" +
                        "-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;" +
                        "-fx-padding:4 10;-fx-background-radius:20;");

        logoBox.getChildren().addAll(appName, mru, roleBadge);

        // ── Menu ──
        VBox menu = new VBox(4);
        menu.setPadding(new Insets(18, 10, 18, 10));

        Button bA = sideBtn("🏠", "Accueil",             true);
        Button bD = sideBtn("📨", "Demandes Clients",    false);
        Button bF = sideBtn("🏭", "Fournisseurs",        false);
        Button bC = sideBtn("📋", "Contrats Mourabaha",  false);
        Button bE = sideBtn("✅", "Paiements Échéances", false);
        Button bH = sideBtn("📊", "Historique",          false);

        bA.setOnAction(e -> { primaryStage.getScene().setRoot(new AccueilView(primaryStage).getRoot());      primaryStage.setTitle("MicroFinance MRU — Accueil"); });
        bD.setOnAction(e -> { primaryStage.getScene().setRoot(new DemandeAgentView(primaryStage).getRoot()); primaryStage.setTitle("MicroFinance MRU — Demandes"); });
        bF.setOnAction(e -> { primaryStage.getScene().setRoot(new FournisseurView(primaryStage).getRoot());  primaryStage.setTitle("MicroFinance MRU — Fournisseurs"); });
        bC.setOnAction(e -> { primaryStage.getScene().setRoot(new ContratView(primaryStage).getRoot());      primaryStage.setTitle("MicroFinance MRU — Contrats"); });
        bE.setOnAction(e -> { primaryStage.getScene().setRoot(new EcheanceView(primaryStage).getRoot());     primaryStage.setTitle("MicroFinance MRU — Paiements"); });
        bH.setOnAction(e -> { primaryStage.getScene().setRoot(new HistoriqueView(primaryStage).getRoot());   primaryStage.setTitle("MicroFinance MRU — Historique"); });

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
            primaryStage.getScene().setRoot(new LoginView(primaryStage).getRoot());
            primaryStage.setTitle("MicroFinance MRU — Connexion");
        });
        bottom.getChildren().add(logout);

        sb.getChildren().addAll(logoBox, menu, bottom);
        return sb;
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════
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

    private VBox statCard(String icon, String label, String value,
                          String vc, String bg, String bc) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color:" + bg + ";-fx-background-radius:14;" +
                "-fx-border-color:" + bc + ";-fx-border-width:0 0 0 4;" +
                "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
        Label i = new Label(icon); i.setStyle("-fx-font-size:24px;");
        Label v = new Label(value);
        v.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        v.setStyle("-fx-text-fill:" + vc + ";");
        Label l = new Label(label);
        l.setFont(Font.font("Georgia", 12));
        l.setStyle("-fx-text-fill:#666;");
        l.setWrapText(true);
        card.getChildren().addAll(i, v, l);
        return card;
    }

    private Button actionBtn(String icon, String text) {
        Button btn = new Button(icon + "   " + text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        String b = "-fx-background-color:#f0f4f0;-fx-text-fill:#1b5e20;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;" +
                "-fx-padding:10 14;-fx-background-radius:10;" +
                "-fx-border-color:#c8e6c9;-fx-border-radius:10;-fx-cursor:hand;";
        String h = "-fx-background-color:#2e7d32;-fx-text-fill:white;" +
                "-fx-font-family:Georgia;-fx-font-size:13px;" +
                "-fx-padding:10 14;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(b);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e  -> btn.setStyle(b));
        return btn;
    }

    public BorderPane getRoot() { return root; }
}