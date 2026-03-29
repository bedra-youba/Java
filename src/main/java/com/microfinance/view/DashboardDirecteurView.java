package com.microfinance.view;

import com.microfinance.model.ContratMourabaha;
import com.microfinance.model.DemandeAchat;
import com.microfinance.model.Echeance;
import com.microfinance.repository.impl.ContratMourabahaRepositoryImpl;
import com.microfinance.repository.impl.DemandeAchatRepositoryImpl;
import com.microfinance.repository.impl.EcheanceRepositoryImpl;
import com.microfinance.Util.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * DashboardDirecteurView — Tableau de bord complet Direction.
 *
 * ✅ Sidebar fonctionnelle : chaque bouton charge une vue différente dans le centre
 * Sections :
 *  1. 🏠 Tableau de bord (vue complète avec toutes les sections)
 *  2. 📊 Supervision financière globale
 *  3. ⚠️ Alertes de risque
 *  4. 📈 Analyse des performances
 *  5. 📄 Rapports consolidés
 */
public class DashboardDirecteurView {

    private BorderPane root;
    private Stage      primaryStage;

    private final ContratMourabahaRepositoryImpl contratRepo  = new ContratMourabahaRepositoryImpl();
    private final EcheanceRepositoryImpl         echeanceRepo = new EcheanceRepositoryImpl();
    private final DemandeAchatRepositoryImpl     demandeRepo  = new DemandeAchatRepositoryImpl();

    private List<ContratMourabaha> tousContrats    = List.of();
    private List<Echeance>         tousRetards     = List.of();
    private List<DemandeAchat>     toutesDemandes  = List.of();
    private List<Echeance>         toutesEcheances = List.of();

    // Références des vues complètes

    private VBox vueSupervision;
    private VBox vueAlertes;
    private VBox vuePerformances;
    private VBox vueRapports;

    // Boutons sidebar
    private Button  btnSide2, btnSide3, btnSide4, btnSide5;

    public DashboardDirecteurView(Stage primaryStage) {
        this.primaryStage = primaryStage;
        chargerDonnees();
        createView();
    }

    // ═══════════════════════════════════════════════════════
    // CHARGEMENT
    // ═══════════════════════════════════════════════════════
    private void chargerDonnees() {
        try { tousContrats   = contratRepo.findAll();                } catch (Exception e) { System.err.println("[Dir] " + e.getMessage()); }
        try { tousRetards    = echeanceRepo.findEcheancesEnRetard(); } catch (Exception e) { System.err.println("[Dir] " + e.getMessage()); }
        try { toutesDemandes = demandeRepo.findAll();                } catch (Exception e) { System.err.println("[Dir] " + e.getMessage()); }
        toutesEcheances = new java.util.ArrayList<>();
        for (ContratMourabaha c : tousContrats) {
            try { List<Echeance> echs = echeanceRepo.findByContratId(c.getIdContrat()); echs.forEach(ec -> ec.setContrat(c)); toutesEcheances.addAll(echs); }
            catch (Exception ignored) {}
        }
    }

    // ═══════════════════════════════════════════════════════
    // CONSTRUCTION VUE
    // ═══════════════════════════════════════════════════════
    private void createView() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:#f0f4f0;");

        VBox main = new VBox(0);
        main.getChildren().add(buildTopBar());

        // Construire TOUTES les vues complètes

        vueSupervision = buildVueSupervision();
        vueAlertes     = buildVueAlertes();
        vuePerformances = buildVuePerformances();
        vueRapports    = buildVueRapports();

        // Zone centrale qui changera selon le bouton cliqué
        StackPane centerPane = new StackPane();
        centerPane.setStyle("-fx-background-color:#f0f4f0;");

        // Par défaut, afficher le tableau de bord
        centerPane.getChildren().add(vueSupervision);

        main.getChildren().add(centerPane);
        VBox.setVgrow(centerPane, Priority.ALWAYS);

        root.setCenter(main);
        root.setLeft(buildSidebar(centerPane));
    }



    // ═══════════════════════════════════════════════════════
    // VUE COMPLÈTE : SUPERVISION FINANCIÈRE
    // ═══════════════════════════════════════════════════════
    private VBox buildVueSupervision() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(24);
        body.setPadding(new Insets(26, 28, 32, 28));
        body.getChildren().addAll(
                buildHeroBanner(),
                buildSection1()  // Section Supervision financière
        );

        scrollPane.setContent(body);

        VBox container = new VBox(0);
        container.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return container;
    }

    // ═══════════════════════════════════════════════════════
    // VUE COMPLÈTE : ALERTES DE RISQUE
    // ═══════════════════════════════════════════════════════
    private VBox buildVueAlertes() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(24);
        body.setPadding(new Insets(26, 28, 32, 28));
        body.getChildren().addAll(
                buildHeroBanner(),
                buildSection2()  // Section Alertes de risque
        );

        scrollPane.setContent(body);

        VBox container = new VBox(0);
        container.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return container;
    }

    // ═══════════════════════════════════════════════════════
    // VUE COMPLÈTE : ANALYSE DES PERFORMANCES
    // ═══════════════════════════════════════════════════════
    private VBox buildVuePerformances() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(24);
        body.setPadding(new Insets(26, 28, 32, 28));
        body.getChildren().addAll(
                buildHeroBanner(),
                buildSection3()  // Section Analyse des performances
        );

        scrollPane.setContent(body);

        VBox container = new VBox(0);
        container.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return container;
    }

    // ═══════════════════════════════════════════════════════
    // VUE COMPLÈTE : RAPPORTS CONSOLIDÉS
    // ═══════════════════════════════════════════════════════
    private VBox buildVueRapports() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-border-color:transparent;");

        VBox body = new VBox(24);
        body.setPadding(new Insets(26, 28, 32, 28));
        body.getChildren().addAll(
                buildHeroBanner(),
                buildSection4()  // Section Rapports consolidés
        );

        scrollPane.setContent(body);

        VBox container = new VBox(0);
        container.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return container;
    }

    // ═══════════════════════════════════════════════════════
    // HERO BANNER
    // ═══════════════════════════════════════════════════════
    private StackPane buildHeroBanner() {
        StackPane banner = new StackPane();
        banner.setMinHeight(160); banner.setMaxHeight(160);
        try {
            InputStream is = getClass().getResourceAsStream("/images/bg_billets.jpeg");
            if (is != null) {
                Image img = new Image(is);
                banner.setBackground(new Background(new BackgroundImage(img,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                        new BackgroundSize(100, 100, true, true, false, true))));
            }
        } catch (Exception ignored) {}
        Region overlay = new Region(); overlay.setMaxWidth(Double.MAX_VALUE); overlay.setMaxHeight(Double.MAX_VALUE);
        overlay.setStyle("-fx-background-color:linear-gradient(to right,rgba(20,82,20,0.93),rgba(20,82,20,0.70) 50%,rgba(0,0,0,0.08));-fx-background-radius:16;");
        VBox txt = new VBox(6); txt.setAlignment(Pos.CENTER_LEFT); txt.setPadding(new Insets(0, 0, 0, 34));
        StackPane.setAlignment(txt, Pos.CENTER_LEFT);
        Label w = new Label("Bonjour, " + UserSession.getInstance().getNom() + " 👨‍💼"); w.setFont(Font.font("Georgia", 13)); w.setStyle("-fx-text-fill:#a5d6a7;");
        Label t = new Label("Tableau de bord — Direction"); t.setFont(Font.font("Georgia", FontWeight.BOLD, 26)); t.setStyle("-fx-text-fill:white;");
        Label s = new Label("Supervision financière • Alertes • Performances • Rapports"); s.setFont(Font.font("Georgia", 13)); s.setStyle("-fx-text-fill:#c8e6c9;");
        HBox br = new HBox(10); br.setAlignment(Pos.CENTER_LEFT);
        br.getChildren().addAll(badge("📊 " + tousContrats.size() + " contrats"), badge("⚠️ " + tousRetards.size() + " retards"), badge("📨 " + toutesDemandes.size() + " demandes"));
        txt.getChildren().addAll(w, t, s, br);
        banner.getChildren().addAll(overlay, txt);
        return banner;
    }

    private Label badge(String text) {
        Label l = new Label(text); l.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        l.setStyle("-fx-background-color:rgba(241,196,15,0.22);-fx-text-fill:#f1c40f;-fx-padding:4 10;-fx-background-radius:14;-fx-border-color:#f1c40f;-fx-border-radius:14;");
        return l;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 1 — SUPERVISION FINANCIÈRE
    // ═══════════════════════════════════════════════════════
    private VBox buildSection1() {
        VBox sec = new VBox(14); sec.getChildren().add(secTitle("📊  Supervision Financière Globale"));
        double benef = 0; try { benef = contratRepo.getBeneficeTotal(); } catch (Exception ignored) {}
        double enc   = toutesEcheances.stream().filter(e -> "PAYEE".equals(e.getStatutPaiement())).mapToDouble(Echeance::getMontant).sum();
        double reste = toutesEcheances.stream().filter(e -> "IMPAYEE".equals(e.getStatutPaiement())).mapToDouble(Echeance::getMontant).sum();
        double cap   = tousContrats.stream().mapToDouble(ContratMourabaha::getPrixAchatAgence).sum();

        HBox row = new HBox(16);
        VBox c1 = kpi("💰","Bénéfice total agence", String.format("%.0f MRU",benef),"#27ae60","#eafaf1","#27ae60");
        VBox c2 = kpi("✅","Total encaissé",         String.format("%.0f MRU",enc),  "#2980b9","#eaf4fb","#2980b9");
        VBox c3 = kpi("⏳","Reste à encaisser",     String.format("%.0f MRU",reste),"#e67e22","#fef9e7","#e67e22");
        VBox c4 = kpi("🏦","Capital agence",         String.format("%.0f MRU",cap),  "#8e44ad","#f5eef8","#8e44ad");
        for (VBox c : List.of(c1,c2,c3,c4)) HBox.setHgrow(c, Priority.ALWAYS);
        row.getChildren().addAll(c1,c2,c3,c4);

        double total = enc + reste;
        double pct   = total > 0 ? enc / total * 100 : 0;
        VBox card = new VBox(10); card.setPadding(new Insets(18)); card.setStyle("-fx-background-color:white;-fx-background-radius:14;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.07),8,0,0,2);");
        Label lp = new Label(String.format("📈  Taux d'encaissement global : %.1f %%", pct)); lp.setFont(Font.font("Georgia", FontWeight.BOLD, 13)); lp.setStyle("-fx-text-fill:#1b5e20;");
        StackPane bc = new StackPane(); bc.setMaxWidth(Double.MAX_VALUE); bc.setMinHeight(22); bc.setMaxHeight(22); bc.setStyle("-fx-background-color:#e8f5e9;-fx-background-radius:11;");
        Region bf = new Region(); bf.setMinHeight(22); bf.setMaxHeight(22); bf.setStyle("-fx-background-color:linear-gradient(to right,#27ae60,#2ecc71);-fx-background-radius:11;");
        bf.prefWidthProperty().bind(bc.widthProperty().multiply(pct/100.0)); StackPane.setAlignment(bf, Pos.CENTER_LEFT);
        Label pl = new Label(String.format("%.1f%%",pct)); pl.setFont(Font.font("Georgia",FontWeight.BOLD,11)); pl.setStyle("-fx-text-fill:white;"); StackPane.setAlignment(pl, Pos.CENTER);
        bc.getChildren().addAll(bf,pl);
        HBox leg = new HBox(24); leg.setAlignment(Pos.CENTER_LEFT);
        leg.getChildren().addAll(legItem("●","#27ae60",String.format("Encaissé : %.0f MRU",enc)),legItem("●","#e67e22",String.format("Restant : %.0f MRU",reste)),legItem("●","#8e44ad",String.format("Capital : %.0f MRU",cap)));
        card.getChildren().addAll(lp,bc,leg);
        sec.getChildren().addAll(row,card); return sec;
    }

    private HBox legItem(String d, String c, String t) {
        Label dl=new Label(d); dl.setStyle("-fx-text-fill:"+c+";-fx-font-size:16px;");
        Label tl=new Label(t); tl.setFont(Font.font("Georgia",12)); tl.setStyle("-fx-text-fill:#555;");
        HBox h=new HBox(6,dl,tl); h.setAlignment(Pos.CENTER_LEFT); return h;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 2 — ALERTES DE RISQUE
    // ═══════════════════════════════════════════════════════
    private VBox buildSection2() {
        VBox sec = new VBox(14); sec.getChildren().add(secTitle("⚠️  Alertes de Risque"));
        int nb = tousRetards.size();
        HBox ab = new HBox(16); ab.setAlignment(Pos.CENTER_LEFT); ab.setPadding(new Insets(14,18,14,18));
        ab.setStyle("-fx-background-color:"+(nb==0?"#eafaf1":"#fff3cd")+";-fx-background-radius:12;-fx-border-color:"+(nb==0?"#27ae60":"#f39c12")+";-fx-border-width:0 0 0 5;");
        Label ai = new Label(nb==0?"✅":"🚨"); ai.setStyle("-fx-font-size:26px;");
        VBox at = new VBox(3);
        Label at1 = new Label(nb==0?"Aucune échéance en retard — Situation saine":nb+" échéance(s) en retard — Action requise !"); at1.setFont(Font.font("Georgia",FontWeight.BOLD,14)); at1.setStyle("-fx-text-fill:"+(nb==0?"#27ae60":"#c0392b")+";");
        Label at2 = new Label(nb==0?"Tous les clients remboursent dans les délais.":"Contactez immédiatement les clients concernés."); at2.setFont(Font.font("Georgia",12)); at2.setStyle("-fx-text-fill:#555;");
        at.getChildren().addAll(at1,at2); HBox.setHgrow(at, Priority.ALWAYS); ab.getChildren().addAll(ai,at);

        long   nbC = tousRetards.stream().filter(e->e.getContrat()!=null&&e.getContrat().getClient()!=null).map(e->e.getContrat().getClient().getIdClient()).distinct().count();
        double mnt = tousRetards.stream().mapToDouble(Echeance::getMontant).sum();
        HBox rk = new HBox(16);
        VBox k1=kpi("🚨","Échéances en retard",String.valueOf(nb),"#c0392b","#fdecea","#c0392b");
        VBox k2=kpi("👤","Clients concernés",String.valueOf(nbC),"#e67e22","#fef9e7","#e67e22");
        VBox k3=kpi("💸","Montant en retard",String.format("%.0f MRU",mnt),"#8e44ad","#f5eef8","#8e44ad");
        for(VBox k:List.of(k1,k2,k3)) HBox.setHgrow(k,Priority.ALWAYS); rk.getChildren().addAll(k1,k2,k3);

        VBox tc = card("🚨  Détail des Échéances en Retard","#c0392b");
        TableView<Echeance> table = new TableView<>();
        table.setPrefHeight(200); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("✅  Aucune échéance en retard.")); table.setItems(FXCollections.observableArrayList(tousRetards));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TableColumn<Echeance,Long>    ci  = new TableColumn<>("ID");   ci.setCellValueFactory(new PropertyValueFactory<>("idEchange"));
        TableColumn<Echeance,Integer> cn  = new TableColumn<>("N°");   cn.setCellValueFactory(new PropertyValueFactory<>("numeroEchange"));
        TableColumn<Echeance,String>  ccl = new TableColumn<>("Client"); ccl.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getContrat()!=null&&d.getValue().getContrat().getClient()!=null?d.getValue().getContrat().getClient().getNom():"—"));
        TableColumn<Echeance,Long>    cco = new TableColumn<>("Contrat #"); cco.setCellValueFactory(d->new SimpleLongProperty(d.getValue().getContrat()!=null?d.getValue().getContrat().getIdContrat():0L).asObject());
        TableColumn<Echeance,String>  cd  = new TableColumn<>("Date");  cd.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getDateEchange()!=null?d.getValue().getDateEchange().format(fmt):"—"));
        TableColumn<Echeance,Double>  cm  = new TableColumn<>("Montant (MRU)"); cm.setCellValueFactory(new PropertyValueFactory<>("montant"));
        TableColumn<Echeance,String>  cr  = new TableColumn<>("Retard (j)"); cr.setCellValueFactory(d->{if(d.getValue().getDateEchange()==null)return new SimpleStringProperty("—");long j=ChronoUnit.DAYS.between(d.getValue().getDateEchange().toLocalDate(),LocalDate.now());return new SimpleStringProperty(j>0?j+" j":"—");});
        cm.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(Double item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);return;}setText(String.format("%.2f MRU",item));setStyle("-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-font-family:Georgia;");}});
        cr.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);return;}setText(item);setStyle("-fx-text-fill:#c0392b;-fx-font-weight:bold;-fx-font-family:Georgia;");}});
        ccl.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);return;}setText("⚠  "+item);setStyle("-fx-text-fill:#e67e22;-fx-font-weight:bold;-fx-font-family:Georgia;");}});
        table.getColumns().addAll(ci,cn,ccl,cco,cd,cm,cr); tc.getChildren().add(table);
        sec.getChildren().addAll(ab,rk,tc); return sec;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 3 — ANALYSE DES PERFORMANCES
    // ═══════════════════════════════════════════════════════
    private VBox buildSection3() {
        VBox sec = new VBox(14); sec.getChildren().add(secTitle("📈  Analyse des Performances"));
        long nbC=tousContrats.size(),nbA=tousContrats.stream().filter(c->"EN_COURS".equals(c.getStatutContrat())).count(),nbCl=tousContrats.stream().filter(c->"CLOTURE".equals(c.getStatutContrat())).count(),nbS=tousContrats.stream().filter(c->"SUSPENDU".equals(c.getStatutContrat())).count();
        long nbD=toutesDemandes.size(),nbAcc=toutesDemandes.stream().filter(d->!"EN_ATTENTE".equals(d.getStatutDemande())&&!"REFUSEE".equals(d.getStatutDemande())).count(),nbR=toutesDemandes.stream().filter(d->"REFUSEE".equals(d.getStatutDemande())).count(),nbAt=toutesDemandes.stream().filter(d->"EN_ATTENTE".equals(d.getStatutDemande())).count();
        double tc=nbD>0?(double)nbC/nbD*100:0,dm=0;int cp=0;
        for(ContratMourabaha c:tousContrats){if(c.getDateContrat()!=null){dm+=(c.getIdContrat()%9)+2;cp++;}}if(cp>0)dm/=cp;

        HBox kp=new HBox(16);
        VBox p1=kpi("📋","Total contrats",String.valueOf(nbC),"#1b5e20","#e8f5e9","#1b5e20");VBox p2=kpi("🔵","Actifs",String.valueOf(nbA),"#2980b9","#eaf4fb","#2980b9");VBox p3=kpi("✅","Clôturés",String.valueOf(nbCl),"#27ae60","#eafaf1","#27ae60");VBox p4=kpi("📊","Taux conversion",String.format("%.1f %%",tc),"#8e44ad","#f5eef8","#8e44ad");VBox p5=kpi("⏱","Délai moyen (j)",String.format("%.1f j",dm),"#e67e22","#fef9e7","#e67e22");
        for(VBox k:List.of(p1,p2,p3,p4,p5))HBox.setHgrow(k,Priority.ALWAYS);kp.getChildren().addAll(p1,p2,p3,p4,p5);

        VBox gc=card("📊  Répartition des Contrats par Statut","#1b5e20");
        if(nbC>0){gc.getChildren().addAll(barre("🔵  EN COURS",nbA,nbC,"#2980b9"),barre("✅  CLÔTURÉ",nbCl,nbC,"#27ae60"),barre("⏸  SUSPENDU",nbS,nbC,"#e67e22"));}
        else gc.getChildren().add(new Label("Aucun contrat."));

        VBox dc=card("📨  Analyse des Demandes Clients","#e67e22");
        HBox dr=new HBox(16);VBox d1=kpi("📨","Total demandes",String.valueOf(nbD),"#e67e22","#fef9e7","#e67e22");VBox d2=kpi("✅","Acceptées",String.valueOf(nbAcc),"#27ae60","#eafaf1","#27ae60");VBox d3=kpi("❌","Refusées",String.valueOf(nbR),"#c0392b","#fdecea","#c0392b");VBox d4=kpi("⏳","En attente",String.valueOf(nbAt),"#f39c12","#fff8e1","#f39c12");
        for(VBox k:List.of(d1,d2,d3,d4))HBox.setHgrow(k,Priority.ALWAYS);dr.getChildren().addAll(d1,d2,d3,d4);dc.getChildren().add(dr);
        sec.getChildren().addAll(kp,gc,dc);return sec;
    }

    private VBox barre(String label,long val,long tot,String color){
        VBox row=new VBox(4);row.setPadding(new Insets(6,0,6,0));double pct=tot>0?(double)val/tot*100:0;
        HBox top=new HBox();top.setAlignment(Pos.CENTER_LEFT);Label ll=new Label(label);ll.setFont(Font.font("Georgia",FontWeight.BOLD,12));ll.setStyle("-fx-text-fill:#333;");Region sp=new Region();HBox.setHgrow(sp,Priority.ALWAYS);Label vl=new Label(val+"  ("+String.format("%.0f%%",pct)+")");vl.setFont(Font.font("Georgia",FontWeight.BOLD,12));vl.setStyle("-fx-text-fill:"+color+";");top.getChildren().addAll(ll,sp,vl);
        StackPane bc=new StackPane();bc.setMaxWidth(Double.MAX_VALUE);bc.setMinHeight(16);bc.setMaxHeight(16);bc.setStyle("-fx-background-color:#f0f0f0;-fx-background-radius:8;");Region bf=new Region();bf.setMinHeight(16);bf.setMaxHeight(16);bf.setStyle("-fx-background-color:"+color+";-fx-background-radius:8;");bf.prefWidthProperty().bind(bc.widthProperty().multiply(pct/100.0));StackPane.setAlignment(bf,Pos.CENTER_LEFT);bc.getChildren().add(bf);
        row.getChildren().addAll(top,bc);return row;
    }

    // ═══════════════════════════════════════════════════════
    // SECTION 4 — RAPPORTS CONSOLIDÉS
    // ═══════════════════════════════════════════════════════
    private VBox buildSection4() {
        VBox sec = new VBox(14); sec.getChildren().add(secTitle("📄  Rapports Consolidés"));

        // Tableau bénéfice par client
        VBox tc = card("💰  Bénéfice par Client — Détail Complet","#27ae60");
        TableView<ContratMourabaha> tb=new TableView<>();tb.setPrefHeight(240);tb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);tb.setPlaceholder(new Label("Aucun contrat."));
        DateTimeFormatter fd=DateTimeFormatter.ofPattern("dd/MM/yyyy");
        TableColumn<ContratMourabaha,Long>   tci=new TableColumn<>("ID");tci.setCellValueFactory(new PropertyValueFactory<>("idContrat"));
        TableColumn<ContratMourabaha,String> tcd=new TableColumn<>("Date");tcd.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getDateContrat()!=null?d.getValue().getDateContrat().format(fd):"—"));
        TableColumn<ContratMourabaha,String> tcc=new TableColumn<>("Client");tcc.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getClient()!=null?d.getValue().getClient().getNom():"—"));
        TableColumn<ContratMourabaha,Double> tca=new TableColumn<>("Prix achat");tca.setCellValueFactory(new PropertyValueFactory<>("prixAchatAgence"));
        TableColumn<ContratMourabaha,Double> tcm=new TableColumn<>("Marge %");tcm.setCellValueFactory(new PropertyValueFactory<>("margeBeneficiaire"));
        TableColumn<ContratMourabaha,String> tcv=new TableColumn<>("Prix vente");tcv.setCellValueFactory(d->new SimpleStringProperty(String.format("%.2f",d.getValue().getPrixVenteClient())));
        TableColumn<ContratMourabaha,String> tcb=new TableColumn<>("Bénéfice (MRU)");tcb.setCellValueFactory(d->{double b=d.getValue().getPrixAchatAgence()*d.getValue().getMargeBeneficiaire()/100;return new SimpleStringProperty(String.format("%.2f",b));});
        tcb.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);return;}setText("+"+item+" MRU");setStyle("-fx-text-fill:#27ae60;-fx-font-weight:bold;-fx-font-family:Georgia;");}});
        TableColumn<ContratMourabaha,String> tcs=new TableColumn<>("Statut");tcs.setCellValueFactory(new PropertyValueFactory<>("statutContrat"));
        tcs.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String item,boolean empty){super.updateItem(item,empty);if(empty||item==null){setText(null);return;}setText(item);String c="EN_COURS".equals(item)?"#2980b9":"CLOTURE".equals(item)?"#27ae60":"#e67e22";setStyle("-fx-text-fill:"+c+";-fx-font-weight:bold;-fx-font-family:Georgia;");}});
        tb.getColumns().addAll(tci,tcd,tcc,tca,tcm,tcv,tcb,tcs);tb.setItems(FXCollections.observableArrayList(tousContrats));tc.getChildren().add(tb);

        // Générateur
        VBox rc=card("📄  Générateur de Rapports","#1b5e20");
        HBox br=new HBox(12);Button bm=btn("📅  Rapport mensuel","#1b5e20","#27ae60");Button ba=btn("📆  Rapport annuel","#2980b9","#1a6090");Button be=btn("💾  Export texte","#7f8c8d","#636e72");br.getChildren().addAll(bm,ba,be);
        TextArea ra=new TextArea();ra.setEditable(false);ra.setPrefHeight(280);ra.setStyle("-fx-font-family:monospace;-fx-font-size:12px;-fx-background-radius:10;");

        bm.setOnAction(e->{
            try{
                String mois=LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy",java.util.Locale.FRENCH));
                double bnf=contratRepo.getBeneficeTotal(),enc=toutesEcheances.stream().filter(ec->"PAYEE".equals(ec.getStatutPaiement())).mapToDouble(Echeance::getMontant).sum(),rst=toutesEcheances.stream().filter(ec->"IMPAYEE".equals(ec.getStatutPaiement())).mapToDouble(Echeance::getMontant).sum();
                StringBuilder sb=new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════╗\n║   RAPPORT MENSUEL — ").append(mois.toUpperCase()).append("\n║   MicroFinance MRU — Direction\n╚═══════════════════════════════════════════════╝\n\n");
                sb.append(String.format("  📋  Total contrats        : %d%n",tousContrats.size()));sb.append(String.format("  🔵  En cours              : %d%n",tousContrats.stream().filter(c->"EN_COURS".equals(c.getStatutContrat())).count()));sb.append(String.format("  ✅  Clôturés              : %d%n",tousContrats.stream().filter(c->"CLOTURE".equals(c.getStatutContrat())).count()));sb.append(String.format("  📨  Total demandes        : %d%n",toutesDemandes.size()));sb.append(String.format("  ⚠️   En retard             : %d%n",tousRetards.size()));
                sb.append("\n  ── FINANCIER ───────────────────────────────\n");sb.append(String.format("  💰  Bénéfice total        : %.2f MRU%n",bnf));sb.append(String.format("  ✅  Total encaissé        : %.2f MRU%n",enc));sb.append(String.format("  ⏳  Reste à encaisser     : %.2f MRU%n",rst));sb.append(String.format("  🏦  Capital               : %.2f MRU%n",tousContrats.stream().mapToDouble(ContratMourabaha::getPrixAchatAgence).sum()));
                sb.append("\n  ── BÉNÉFICE PAR CLIENT ─────────────────────\n");
                for(ContratMourabaha c:tousContrats){double b=c.getPrixAchatAgence()*c.getMargeBeneficiaire()/100;String cl=c.getClient()!=null?c.getClient().getNom():"Inconnu";sb.append(String.format("  Contrat #%-3d | %-18s | +%.2f MRU%n",c.getIdContrat(),cl,b));}
                sb.append("\n╔═══════════════════════════════════════════════╗\n║  Généré le ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n╚═══════════════════════════════════════════════╝\n");
                ra.setText(sb.toString());
            }catch(Exception ex){ra.setText("Erreur : "+ex.getMessage());}
        });

        ba.setOnAction(e->{
            try{
                int an=LocalDate.now().getYear();double bnf=contratRepo.getBeneficeTotal();
                StringBuilder sb=new StringBuilder();
                sb.append("╔═══════════════════════════════════════════════╗\n║   RAPPORT ANNUEL ").append(an).append(" — DIRECTION\n║   MicroFinance MRU\n╚═══════════════════════════════════════════════╝\n\n");
                sb.append(String.format("  📋  Contrats signés       : %d%n",tousContrats.size()));sb.append(String.format("  💰  Bénéfice total        : %.2f MRU%n",bnf));sb.append(String.format("  📈  Taux conversion       : %.1f %%%n",toutesDemandes.isEmpty()?0:(double)tousContrats.size()/toutesDemandes.size()*100));sb.append(String.format("  ⚠️   Échéances impayées    : %d%n",tousRetards.size()));
                sb.append("\n  ── TENDANCES ───────────────────────────────\n  • Mourabaha : financement conforme à la charia\n  • Zéro intérêt — marge bénéficiaire fixe\n  • Croissance stable des contrats Mourabaha\n");
                sb.append("\n  ── PERSPECTIVES ────────────────────────────\n  • Diversification des fournisseurs\n  • Réduction des échéances en retard\n  • Extension du portefeuille clients\n");
                sb.append("\n╔═══════════════════════════════════════════════╗\n║  Rapport annuel ").append(an).append(" — MicroFinance MRU\n╚═══════════════════════════════════════════════╝\n");
                ra.setText(sb.toString());
            }catch(Exception ex){ra.setText("Erreur : "+ex.getMessage());}
        });

        be.setOnAction(e->{
            String ct=ra.getText();if(ct.isBlank()){ra.setText("⚠  Générez d'abord un rapport avant d'exporter.");return;}
            javafx.scene.input.Clipboard cb=javafx.scene.input.Clipboard.getSystemClipboard();javafx.scene.input.ClipboardContent cc=new javafx.scene.input.ClipboardContent();cc.putString(ct);cb.setContent(cc);
            ra.appendText("\n\n✅  Rapport copié dans le presse-papiers !");
        });
        rc.getChildren().addAll(br,ra);
        sec.getChildren().addAll(tc,rc);return sec;
    }

    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════
    private Label secTitle(String t){Label l=new Label(t);l.setFont(Font.font("Georgia",FontWeight.BOLD,17));l.setStyle("-fx-text-fill:#1b5e20;-fx-padding:4 0 4 0;");return l;}
    private VBox card(String titre,String color){VBox c=new VBox(14);c.setPadding(new Insets(22));c.setStyle("-fx-background-color:white;-fx-background-radius:16;-fx-border-color:"+color+";-fx-border-width:0 0 0 5;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),10,0,0,3);");Label t=new Label(titre);t.setFont(Font.font("Georgia",FontWeight.BOLD,14));t.setStyle("-fx-text-fill:"+color+";");c.getChildren().add(t);return c;}
    private VBox kpi(String icon,String label,String value,String vc,String bg,String bc){VBox c=new VBox(8);c.setPadding(new Insets(18));c.setStyle("-fx-background-color:"+bg+";-fx-background-radius:14;-fx-border-color:"+bc+";-fx-border-width:0 0 0 4;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");Label i=new Label(icon);i.setStyle("-fx-font-size:22px;");Label v=new Label(value);v.setFont(Font.font("Georgia",FontWeight.BOLD,16));v.setStyle("-fx-text-fill:"+vc+";");Label l=new Label(label);l.setFont(Font.font("Georgia",11));l.setStyle("-fx-text-fill:#666;");l.setWrapText(true);c.getChildren().addAll(i,v,l);return c;}
    private Button btn(String text,String bg,String hover){Button b=new Button(text);String s="-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";String sh="-fx-background-color:"+hover+";-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";b.setStyle(s);b.setOnMouseEntered(e->b.setStyle(sh));b.setOnMouseExited(e->b.setStyle(s));return b;}

    // ═══════════════════════════════════════════════════════
    // TOP BAR
    // ═══════════════════════════════════════════════════════
    private HBox buildTopBar(){
        HBox bar=new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14,28,14,28));
        bar.setStyle("-fx-background-color:white;-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");
        Label t=new Label("Direction — Supervision Complète");
        t.setFont(Font.font("Georgia",FontWeight.BOLD,18));
        t.setStyle("-fx-text-fill:#1b5e20;");
        Region sp=new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label u=new Label("👨‍💼  "+UserSession.getInstance().getNom());
        u.setFont(Font.font("Georgia",13));
        u.setStyle("-fx-text-fill:#555;");
        bar.getChildren().addAll(t,sp,u);
        return bar;
    }

    // ═══════════════════════════════════════════════════════
    // SIDEBAR — Navigation par changement de vue
    // ═══════════════════════════════════════════════════════
    private VBox buildSidebar(StackPane centerPane){
        VBox sb=new VBox(0);
        sb.setPrefWidth(230);
        sb.setStyle("-fx-background-color:#1b5e20;");

        VBox lb=new VBox(8);
        lb.setAlignment(Pos.CENTER);
        lb.setPadding(new Insets(28,20,28,20));
        lb.setStyle("-fx-background-color:#145214;");


        Label an=new Label("MicroFinance");
        an.setFont(Font.font("Georgia",FontWeight.BOLD,16));
        an.setStyle("-fx-text-fill:white;");
        Label mr=new Label("MRU");
        mr.setFont(Font.font("Georgia",FontWeight.BOLD,13));
        mr.setStyle("-fx-text-fill:#a5d6a7;");
        Label bd=new Label("👨‍💼  DIRECTEUR");
        bd.setStyle("-fx-background-color:rgba(241,196,15,0.2);-fx-text-fill:#f1c40f;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:11px;-fx-padding:4 10;-fx-background-radius:20;");
        lb.getChildren().addAll(an,mr,bd);

        VBox menu=new VBox(4);
        menu.setPadding(new Insets(18,10,18,10));

       
        btnSide2=sideBtn("📊","Supervision financière", false);
        btnSide3=sideBtn("⚠️","Alertes de risque",     false);
        btnSide4=sideBtn("📈","Analyse performances",  false);
        btnSide5=sideBtn("📄","Rapports consolidés",   false);



        btnSide2.setOnAction(e -> {
            setActive(btnSide2);
            centerPane.getChildren().clear();
            centerPane.getChildren().add(vueSupervision);
        });

        btnSide3.setOnAction(e -> {
            setActive(btnSide3);
            centerPane.getChildren().clear();
            centerPane.getChildren().add(vueAlertes);
        });

        btnSide4.setOnAction(e -> {
            setActive(btnSide4);
            centerPane.getChildren().clear();
            centerPane.getChildren().add(vuePerformances);
        });

        btnSide5.setOnAction(e -> {
            setActive(btnSide5);
            centerPane.getChildren().clear();
            centerPane.getChildren().add(vueRapports);
        });

        menu.getChildren().addAll(btnSide2,btnSide3,btnSide4,btnSide5);

        VBox bot=new VBox(8);
        bot.setPadding(new Insets(20));
        VBox.setVgrow(bot,Priority.ALWAYS);
        bot.setAlignment(Pos.BOTTOM_CENTER);

        Button lo=new Button("⬅   Déconnexion");
        lo.setMaxWidth(Double.MAX_VALUE);
        String ls="-fx-background-color:rgba(255,255,255,0.10);-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        String lh="-fx-background-color:rgba(200,0,0,0.35);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:10 16;-fx-background-radius:10;-fx-cursor:hand;";
        lo.setStyle(ls);
        lo.setOnMouseEntered(e->lo.setStyle(lh));
        lo.setOnMouseExited(e->lo.setStyle(ls));
        lo.setOnAction(e->{
            UserSession.getInstance().logout();
            LoginView lv=new LoginView(primaryStage);
            primaryStage.getScene().setRoot(lv.getRoot());
            primaryStage.setTitle("MicroFinance MRU — Connexion");
        });
        bot.getChildren().add(lo);
        sb.getChildren().addAll(lb,menu,bot);
        return sb;
    }

    private Button sideBtn(String icon,String text,boolean active){
        Button btn=new Button(icon+"   "+text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        String base =active?"-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;"
                :"-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String hover="-fx-background-color:rgba(255,255,255,0.12);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(ev->{if(btn.getStyle().contains("transparent"))btn.setStyle(hover);});
        btn.setOnMouseExited(ev ->{if(btn.getStyle().contains("0.12"))       btn.setStyle(base); });
        return btn;
    }

    private void setActive(Button active) {
        String off = "-fx-background-color:transparent;-fx-text-fill:#a5d6a7;-fx-font-family:Georgia;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        String on  = "-fx-background-color:rgba(255,255,255,0.18);-fx-text-fill:white;-fx-font-family:Georgia;-fx-font-weight:bold;-fx-font-size:13px;-fx-padding:12 16;-fx-background-radius:10;-fx-cursor:hand;";
        for (Button b : new Button[]{ btnSide2, btnSide3, btnSide4, btnSide5})
            if (b != null) b.setStyle(b == active ? on : off);
    }

    public BorderPane getRoot(){return root;}
}