package com.microfinance.view;

import com.microfinance.model.Utilisateur;
import com.microfinance.repository.impl.UtilisateurRepository;
import com.microfinance.Util.UserSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.InputStream;
import java.util.Optional;

/**
 * Page de Connexion avec RBAC (Role-Based Access Control).
 *
 * Règles de validation :
 *  - Email : doit contenir @ et finir par gmail.com, icloud.com, hotmail.com, yahoo.com, outlook.com
 *  - Mot de passe : max 8 caractères, min 4 caractères
 *  - Nom : obligatoire
 *  - Rôle : CLIENT, AGENT ou DIRECTEUR
 *
 * Après connexion :
 *  - CLIENT    → DashboardClientView
 *  - AGENT     → AccueilView (dashboard Agent / Mourabaha)
 *  - DIRECTEUR → DashboardDirecteurView
 */
public class LoginView {

    private HBox root;
    private Stage primaryStage;
    private final UtilisateurRepository userRepo = new UtilisateurRepository();

    // Onglet actif : "connexion" ou "inscription"
    private boolean modeInscription = false;

    // Panneaux du formulaire
    private VBox panneauConnexion;
    private VBox panneauInscription;
    private VBox leftPanel;

    public LoginView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        createView();
    }

    private void createView() {
        root = new HBox(0);

        // ─── PANNEAU GAUCHE : formulaires ───
        leftPanel = new VBox(0);
        leftPanel.setPrefWidth(500);
        leftPanel.setMinWidth(440);
        leftPanel.setStyle("-fx-background-color: white;");
        leftPanel.setAlignment(Pos.TOP_CENTER);

        // En-tête du panneau gauche
        VBox header = buildHeader();
        // Onglets Connexion / Inscription
        HBox tabs = buildTabs();

        panneauConnexion   = buildFormConnexion();
        panneauInscription = buildFormInscription();
        panneauInscription.setVisible(false);
        panneauInscription.setManaged(false);

        leftPanel.getChildren().addAll(header, tabs, panneauConnexion, panneauInscription);

        // ScrollPane : permet de defiler quand le formulaire inscription est trop long
        ScrollPane leftScroll = new ScrollPane(leftPanel);
        leftScroll.setFitToWidth(true);
        leftScroll.setPrefWidth(500);
        leftScroll.setMinWidth(440);
        leftScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        leftScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        leftScroll.setStyle("-fx-background-color:white;-fx-background:white;-fx-border-color:transparent;");

        // PANNEAU DROIT : image billets
        StackPane rightPanel = buildRightPanel();
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        root.getChildren().addAll(leftScroll, rightPanel);
    }

    // ─────────────────────────────────────────────────────
    // EN-TÊTE
    // ─────────────────────────────────────────────────────
    private VBox buildHeader() {
        VBox header = new VBox(6);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(22, 40, 12, 40));

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER);
        ImageView logo = new ImageView(
                new Image(getClass().getResourceAsStream("/images/M.png"))
        );
        logo.setFitWidth(60);
        logo.setFitHeight(60);
        VBox logoText = new VBox(2);
        Label appName = new Label("MicroFinance");
        appName.setFont(Font.font("Georgia", FontWeight.BOLD, 20));
        appName.setStyle("-fx-text-fill:#1b5e20;");
        Label mruLbl = new Label("MRU — Mauritanie");
        mruLbl.setFont(Font.font("Georgia", 12));
        mruLbl.setStyle("-fx-text-fill:#888;");
        logoText.getChildren().addAll(appName, mruLbl);
        logoRow.getChildren().addAll(logo, logoText);

        Region bar = new Region();
        bar.setPrefHeight(3); bar.setMaxWidth(50);
        bar.setStyle("-fx-background-color:#27ae60;-fx-background-radius:2;");

        header.getChildren().addAll(logoRow, bar);
        return header;
    }

    // ─────────────────────────────────────────────────────
    // ONGLETS
    // ─────────────────────────────────────────────────────
    private HBox buildTabs() {
        HBox tabs = new HBox(0);
        tabs.setAlignment(Pos.CENTER);
        tabs.setPadding(new Insets(0, 40, 0, 40));

        Button btnCnx = tabBtn("Connexion", true);
        Button btnIns = tabBtn("Créer un compte", false);

        btnCnx.setOnAction(e -> {
            modeInscription = false;
            panneauConnexion.setVisible(true);   panneauConnexion.setManaged(true);
            panneauInscription.setVisible(false); panneauInscription.setManaged(false);
            styleTabActive(btnCnx); styleTabInactive(btnIns);
        });

        btnIns.setOnAction(e -> {
            modeInscription = true;
            panneauConnexion.setVisible(false);   panneauConnexion.setManaged(false);
            panneauInscription.setVisible(true);  panneauInscription.setManaged(true);
            styleTabActive(btnIns); styleTabInactive(btnCnx);
        });

        tabs.getChildren().addAll(btnCnx, btnIns);
        return tabs;
    }

    // ─────────────────────────────────────────────────────
    // FORMULAIRE DE CONNEXION
    // ─────────────────────────────────────────────────────
    private VBox buildFormConnexion() {
        VBox form = new VBox(14);
        form.setPadding(new Insets(28, 40, 40, 40));

        Label titleLbl = new Label("Connexion");
        titleLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleLbl.setStyle("-fx-text-fill:#1a1a1a;");

        Label subLbl = new Label("Connectez-vous avec votre email et mot de passe");
        subLbl.setFont(Font.font("Georgia", 13));
        subLbl.setStyle("-fx-text-fill:#888;");
        subLbl.setWrapText(true);

        // Champ email
        Label emailLbl = fieldLabel("Email");
        TextField emailField = buildTextField("exemple@gmail.com");

        // Champ mot de passe (max 8 caractères)
        Label passLbl = fieldLabel("Mot de passe (max 8 caractères)");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Votre mot de passe");
        passField.setMaxWidth(Double.MAX_VALUE);
        styleInputField(passField);

        // Limiter à 8 caractères
        passField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() > 8) passField.setText(oldVal);
        });

        // Message d'erreur
        Label erreurLbl = new Label("");
        erreurLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;");
        erreurLbl.setWrapText(true);

        // Bouton Connexion
        Button loginBtn = actionBtn("Se connecter  →", "#1b5e20", "#27ae60");
        loginBtn.setOnAction(e -> {
            String email = emailField.getText().trim();
            String pass  = passField.getText().trim();

            // Validation email
            String errEmail = validerEmail(email);
            if (errEmail != null) { erreurLbl.setText("❌  " + errEmail); return; }

            // Validation mot de passe
            String errPass = validerMotDePasse(pass);
            if (errPass != null) { erreurLbl.setText("❌  " + errPass); return; }

            erreurLbl.setText("⏳  Connexion en cours...");

            try {
                Optional<Utilisateur> optUser = userRepo.authentifier(email, pass);

                if (optUser.isEmpty()) {
                    erreurLbl.setText("❌  Email ou mot de passe incorrect.");
                    return;
                }

                Utilisateur u = optUser.get();
                // Enregistrer la session
                UserSession.getInstance().login(u.getIdUtilisateur(), u.getNom(), u.getEmail(), u.getRole());

                // ── RBAC : redirection selon le rôle ──
                redirecterSelonRole(u.getRole());

            } catch (Exception ex) {
                erreurLbl.setText("❌  " + ex.getMessage());
            }
        });

        // Touche Entrée
        passField.setOnAction(e -> loginBtn.fire());

        // Bouton Quitter
        Button exitBtn = new Button("Quitter l'application");
        exitBtn.setMaxWidth(Double.MAX_VALUE);
        exitBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#aaa;-fx-font-family:Georgia;-fx-font-size:12px;-fx-padding:8 20;-fx-background-radius:10;-fx-cursor:hand;");
        exitBtn.setOnMouseEntered(e -> exitBtn.setStyle("-fx-background-color:#fdecea;-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;-fx-padding:8 20;-fx-background-radius:10;-fx-cursor:hand;"));
        exitBtn.setOnMouseExited(e  -> exitBtn.setStyle("-fx-background-color:transparent;-fx-text-fill:#aaa;-fx-font-family:Georgia;-fx-font-size:12px;-fx-padding:8 20;-fx-background-radius:10;-fx-cursor:hand;"));
        exitBtn.setOnAction(e -> System.exit(0));

        form.getChildren().addAll(titleLbl, subLbl, emailLbl, emailField, passLbl, passField, erreurLbl, loginBtn, exitBtn);
        return form;
    }

    // ─────────────────────────────────────────────────────
    // FORMULAIRE D'INSCRIPTION
    // ─────────────────────────────────────────────────────
    private VBox buildFormInscription() {
        VBox form = new VBox(9);
        form.setPadding(new Insets(28, 40, 40, 40));

        Label titleLbl = new Label("Créer un compte");
        titleLbl.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titleLbl.setStyle("-fx-text-fill:#1a1a1a;");

        Label subLbl = new Label("Remplissez le formulaire — vous pourrez ensuite vous connecter");
        subLbl.setFont(Font.font("Georgia", 12));
        subLbl.setStyle("-fx-text-fill:#888;");
        subLbl.setWrapText(true);

        // Champs
        TextField nomField    = buildTextField("Votre nom");
        TextField prenomField = buildTextField("Votre prénom");
        TextField emailField  = buildTextField("exemple@gmail.com");
        TextField telField    = buildTextField("Téléphone (optionnel)");

        PasswordField passField    = new PasswordField(); passField.setPromptText("Mot de passe (4-8 caractères)"); passField.setMaxWidth(Double.MAX_VALUE); styleInputField(passField);
        PasswordField passConfField = new PasswordField(); passConfField.setPromptText("Confirmer le mot de passe"); passConfField.setMaxWidth(Double.MAX_VALUE); styleInputField(passConfField);

        // Limiter à 8 caractères
        passField.textProperty().addListener((obs, o, n) -> { if (n.length() > 8) passField.setText(o); });
        passConfField.textProperty().addListener((obs, o, n) -> { if (n.length() > 8) passConfField.setText(o); });

        // Sélection du rôle
        Label roleLbl = fieldLabel("Rôle");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("CLIENT", "AGENT");
        roleCombo.setValue("CLIENT");
        roleCombo.setMaxWidth(Double.MAX_VALUE);
        roleCombo.setStyle("-fx-font-family:Georgia;-fx-font-size:13px;-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;");

        // Info rôle
        Label infoRole = new Label("ℹ️  Le rôle DIRECTEUR est créé uniquement par l'administrateur système.");
        infoRole.setFont(Font.font("Georgia", 11));
        infoRole.setStyle("-fx-text-fill:#888;-fx-background-color:#f8f9fa;-fx-padding:6 10;-fx-background-radius:6;");
        infoRole.setWrapText(true);

        // Message résultat
        Label msgLbl = new Label("");
        msgLbl.setStyle("-fx-font-family:Georgia;-fx-font-size:12px;");
        msgLbl.setWrapText(true);

        // Bouton Créer
        Button creerBtn = actionBtn("Créer mon compte  →", "#1b5e20", "#27ae60");
        creerBtn.setOnAction(e -> {
            String nom    = nomField.getText().trim();
            String prenom = prenomField.getText().trim();
            String email  = emailField.getText().trim();
            String tel    = telField.getText().trim();
            String pass   = passField.getText().trim();
            String passC  = passConfField.getText().trim();
            String role   = roleCombo.getValue();

            // Validations
            if (nom.isEmpty())    { msgLbl.setText("❌  Le nom est obligatoire."); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;"); return; }
            if (prenom.isEmpty()) { msgLbl.setText("❌  Le prénom est obligatoire."); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;"); return; }

            String errEmail = validerEmail(email);
            if (errEmail != null) { msgLbl.setText("❌  " + errEmail); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;"); return; }

            String errPass = validerMotDePasse(pass);
            if (errPass != null)  { msgLbl.setText("❌  " + errPass); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;"); return; }

            if (!pass.equals(passC)) { msgLbl.setText("❌  Les mots de passe ne correspondent pas."); msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;"); return; }

            try {
                if (userRepo.emailExiste(email)) {
                    msgLbl.setText("❌  Cet email est déjà utilisé. Connectez-vous.");
                    msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;");
                    return;
                }

                Utilisateur u = new Utilisateur();
                u.setNom(nom); u.setPrenom(prenom);
                u.setEmail(email); u.setTelephone(tel);
                u.setLogin(email); // login = email
                u.setMotDePasse(pass);
                u.setRole(role);

                userRepo.creer(u);

                msgLbl.setText("✅  Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
                msgLbl.setStyle("-fx-text-fill:#27ae60;-fx-font-family:Georgia;-fx-font-size:12px;");

                // Vider les champs
                nomField.clear(); prenomField.clear(); emailField.clear();
                telField.clear(); passField.clear(); passConfField.clear();
                roleCombo.setValue("CLIENT");

            } catch (Exception ex) {
                msgLbl.setText("❌  " + ex.getMessage());
                msgLbl.setStyle("-fx-text-fill:#c0392b;-fx-font-family:Georgia;-fx-font-size:12px;");
            }
        });

        form.getChildren().addAll(
                titleLbl, subLbl,
                fieldLabel("Nom"),    nomField,
                fieldLabel("Prénom"), prenomField,
                fieldLabel("Email"),  emailField,
                fieldLabel("Téléphone"), telField,
                fieldLabel("Mot de passe"), passField,
                fieldLabel("Confirmer le mot de passe"), passConfField,
                roleLbl, roleCombo, infoRole,
                msgLbl, creerBtn
        );
        return form;
    }

    // ─────────────────────────────────────────────────────
    // RBAC — Redirection selon le rôle
    // ─────────────────────────────────────────────────────
    private void redirecterSelonRole(String role) {
        switch (role) {
            case "CLIENT" -> {
                DashboardClientView v = new DashboardClientView(primaryStage);
                primaryStage.getScene().setRoot(v.getRoot());
                primaryStage.setTitle("MicroFinance MRU — Espace Client");
            }
            case "AGENT" -> {
                AccueilView v = new AccueilView(primaryStage);
                primaryStage.getScene().setRoot(v.getRoot());
                primaryStage.setTitle("MicroFinance MRU — Espace Agent");
            }
            case "DIRECTEUR" -> {
                DashboardDirecteurView v = new DashboardDirecteurView(primaryStage);
                primaryStage.getScene().setRoot(v.getRoot());
                primaryStage.setTitle("MicroFinance MRU — Espace Directeur");
            }
            default -> {
                showAlert("Rôle inconnu", "Rôle non reconnu : " + role);
            }
        }
    }

    // ─────────────────────────────────────────────────────
    // VALIDATIONS
    // ─────────────────────────────────────────────────────

    /**
     * Valide l'email — doit contenir @ et domaine autorisé.
     * Retourne null si valide, message d'erreur sinon.
     */
    private String validerEmail(String email) {
        if (email == null || email.isEmpty()) return "L'email est obligatoire.";
        if (!email.contains("@")) return "L'email doit contenir @.";

        String[] domainesAutorises = {
                "gmail.com", "icloud.com", "hotmail.com",
                "yahoo.com", "outlook.com", "yahoo.fr",
                "hotmail.fr", "live.com"
        };

        String emailLower = email.toLowerCase();
        boolean domainValide = false;
        for (String domaine : domainesAutorises) {
            if (emailLower.endsWith("@" + domaine) || emailLower.endsWith("." + domaine.replace(".com","") + ".com")) {
                domainValide = true;
                break;
            }
        }

        // Validation plus souple : après le @, doit avoir un domaine valide connu
        String[] parts = email.split("@");
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            return "Format email invalide.";
        }
        String domaine = parts[1].toLowerCase();
        boolean ok = false;
        for (String d : domainesAutorises) {
            if (domaine.equals(d)) { ok = true; break; }
        }
        if (!ok) return "Domaine email non autorisé. Utilisez gmail.com, icloud.com, hotmail.com, yahoo.com ou outlook.com.";

        return null; // Valide
    }

    /**
     * Valide le mot de passe — entre 4 et 8 caractères.
     * Retourne null si valide, message d'erreur sinon.
     */
    private String validerMotDePasse(String pass) {
        if (pass == null || pass.isEmpty()) return "Le mot de passe est obligatoire.";
        if (pass.length() < 4) return "Le mot de passe doit contenir au moins 4 caractères.";
        if (pass.length() > 8) return "Le mot de passe ne doit pas dépasser 8 caractères.";
        return null; // Valide
    }

    // ─────────────────────────────────────────────────────
    // PANNEAU DROIT (image billets)
    // ─────────────────────────────────────────────────────
    private StackPane buildRightPanel() {
        StackPane rightPanel = new StackPane();

        try {
            InputStream is = getClass().getResourceAsStream("/images/bg_billets.jpeg");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage bg = new BackgroundImage(bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, true));
                rightPanel.setBackground(new Background(bg));
            }
        } catch (Exception ignored) {}

        Region overlay = new Region();
        overlay.setMaxWidth(Double.MAX_VALUE); overlay.setMaxHeight(Double.MAX_VALUE);
        overlay.setStyle("-fx-background-color:linear-gradient(to bottom right, rgba(20,82,20,0.85) 0%, rgba(20,82,20,0.55) 50%, rgba(0,0,0,0.25) 100%);");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(60));

        Label quoteIcon = new Label("❝");
        quoteIcon.setStyle("-fx-font-size:48px;-fx-text-fill:rgba(241,196,15,0.65);");

        Label quote = new Label("Ensemble, finançons\nles projets de demain");
        quote.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        quote.setStyle("-fx-text-fill:white;-fx-text-alignment:center;");
        quote.setWrapText(true);

        Label quoteSub = new Label("Microfinance islamique — sans intérêt\nconforme à la charia mauritanienne");
        quoteSub.setFont(Font.font("Georgia", 13));
        quoteSub.setStyle("-fx-text-fill:#c8e6c9;-fx-text-alignment:center;");
        quoteSub.setWrapText(true);

        // Badges des 3 rôles
        VBox rolesBadges = new VBox(10);
        rolesBadges.setAlignment(Pos.CENTER);
        rolesBadges.getChildren().addAll(
                roleBadge("👤  CLIENT", "Consultez vos contrats et échéancier"),
                roleBadge("👩‍💼  AGENT", "Gérez les contrats Mourabaha"),
                roleBadge("👨‍💼  DIRECTEUR", "Superviser et analyser les performances")
        );

        content.getChildren().addAll(quoteIcon, quote, quoteSub, rolesBadges);
        rightPanel.getChildren().addAll(overlay, content);
        return rightPanel;
    }

    private HBox roleBadge(String titre, String desc) {
        HBox badge = new HBox(10);
        badge.setAlignment(Pos.CENTER_LEFT);
        badge.setPadding(new Insets(10, 16, 10, 16));
        badge.setStyle("-fx-background-color:rgba(255,255,255,0.12);-fx-background-radius:10;-fx-border-color:rgba(255,255,255,0.2);-fx-border-radius:10;");
        VBox texts = new VBox(2);
        Label t = new Label(titre); t.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); t.setStyle("-fx-text-fill:white;");
        Label d = new Label(desc);  d.setFont(Font.font("Georgia", 11)); d.setStyle("-fx-text-fill:#a5d6a7;");
        texts.getChildren().addAll(t, d);
        badge.getChildren().add(texts);
        return badge;
    }

    // ─── Helpers UI ───
    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        l.setStyle("-fx-text-fill:#333;");
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private TextField buildTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        styleInputField(tf);
        return tf;
    }

    private void styleInputField(TextInputControl field) {
        String base  = "-fx-background-color:#f8f9fa;-fx-border-color:#e0e0e0;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 12;-fx-font-size:13px;-fx-font-family:Georgia;";
        String focus = "-fx-background-color:#eafaf1;-fx-border-color:#27ae60;-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 12;-fx-font-size:13px;-fx-font-family:Georgia;";
        field.setStyle(base);
        field.focusedProperty().addListener((obs, o, n) -> field.setStyle(n ? focus : base));
    }

    private Button actionBtn(String text, String bg, String hover) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        String s  = "-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:14px;-fx-padding:13 20;-fx-background-radius:10;-fx-cursor:hand;";
        String sh = "-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:14px;-fx-padding:13 20;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(s); btn.setOnMouseEntered(e->btn.setStyle(sh)); btn.setOnMouseExited(e->btn.setStyle(s));
        return btn;
    }

    private Button tabBtn(String text, boolean active) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btn, Priority.ALWAYS);
        if (active) styleTabActive(btn); else styleTabInactive(btn);
        return btn;
    }

    private void styleTabActive(Button btn) {
        btn.setStyle("-fx-background-color:#1b5e20;-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 20;-fx-background-radius:0;-fx-cursor:hand;");
    }
    private void styleTabInactive(Button btn) {
        btn.setStyle("-fx-background-color:#f0f4f0;-fx-text-fill:#555;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 20;-fx-background-radius:0;-fx-cursor:hand;");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public HBox getRoot() { return root; }
}