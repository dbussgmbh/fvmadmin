package com.example.application.views;

import com.example.application.data.entity.Ablaufdaten;
import com.example.application.data.entity.Configuration;
import com.example.application.data.entity.Metadaten;
import com.example.application.data.service.ConfigurationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@PageTitle("Tabelle Metadaten")
@Route(value = "metadaten", layout= MainLayout.class)
//@Route(value = "")
public class MetadatenView extends VerticalLayout {

    @Autowired
    JdbcTemplate jdbcTemplate;
    private ConfigurationService service;
    private ComboBox<Configuration> comboBox;
    Grid<Metadaten> grid = new Grid<>(Metadaten.class, false);
    //Grid<Ablaufdaten> gridAblaufdaten = new Grid<>(Ablaufdaten.class, false);
    Grid<Ablaufdaten> gridAblaufdaten = new Grid<>(Ablaufdaten.class, false);

    TextField filterText = new TextField();
    Integer ret = 0;
    Button button = new Button("Refresh");
    List<Metadaten> metadaten;
    List<Ablaufdaten> ablaufdaten;
    GridListDataView<Metadaten> dataView=grid.setItems();

    public MetadatenView (ConfigurationService service){

        this.service = service;

        add(new H3("Metadaten Viewer"));

        comboBox = new ComboBox<>("Verbindung");
        comboBox.setItems(service.findMessageConfigurations());
        comboBox.setItemLabelGenerator(Configuration::get_Message_Connection);

        comboBox.setValue(service.findAllConfigurations().stream().findFirst().get());

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(comboBox,button);
        hl.setAlignItems(FlexComponent.Alignment.BASELINE);

        add(hl);

        addClassName("list-view");
      //  setSizeFull();

        gridAblaufdaten.addColumn(Ablaufdaten::getNAME_NLS).setHeader("NAME_NLS").setSortable(true).setResizable(true);
        gridAblaufdaten.addColumn(Ablaufdaten::getNAME).setHeader("NAME").setSortable(true).setResizable(true);
        gridAblaufdaten.addColumn(Ablaufdaten::getTYP).setHeader("TYP").setSortable(true).setResizable(true);
        gridAblaufdaten.addColumn(Ablaufdaten::getSTART_DATUM).setHeader("Start").setSortable(true).setResizable(true);
        gridAblaufdaten.addColumn(Ablaufdaten::getENDE_DATUM).setHeader("Ende").setSortable(true).setResizable(true);
        gridAblaufdaten.addColumn(Ablaufdaten::getTIMESTAMPVERSION).setHeader("Timestamp").setSortable(true).setResizable(true);
        gridAblaufdaten.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        gridAblaufdaten.addThemeVariants(GridVariant.LUMO_COMPACT);

        grid.addColumn(Metadaten::getNACHRICHTIDEXTERN).setHeader("NachrichtID-Extern")
                .setAutoWidth(true).setResizable(true).setSortable(true).setResizable(true);
        grid.addColumn(Metadaten::getEINGANGSDATUMSERVER).setHeader("Eingangsdatum")
                .setAutoWidth(true).setResizable(true).setSortable(true).setResizable(true);
        grid.addColumn(Metadaten::getVERARBEITET).setHeader("Verarbeitet")
                .setAutoWidth(true).setResizable(true).setSortable(true).setResizable(true);

       // grid.setItemDetailsRenderer(createPersonDetailsRenderer());
       // grid.addItemDoubleClickListener(e->showAblaufdaten(e));
        grid.addItemClickListener(e->showAblaufdaten(e));


        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
      //  GridListDataView<Metadaten> dataView =grid.setItems();

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Suchen");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
                                                    System.out.println("Suche nach: " + searchField.getValue());
                                                   // metadaten=getMailboxes(searchField.getValue());
                                                   // grid.setItems(metadaten);
                                                    try{
                                                        dataView.setFilter((item -> item.getNACHRICHTIDEXTERN().toLowerCase().contains(e.getValue().toLowerCase())));
                                                        //dataView.refreshAll();
                                                    }
                                                    catch (Exception exception) {
                                                        System.out.println("Keine Eintrag gefunden..." + exception.getMessage());
                                                    }

                                                });



        VerticalLayout layout = new VerticalLayout(searchField, grid);
        layout.setPadding(false);

        add(layout);


        add(gridAblaufdaten);
        button.addClickListener(clickEvent -> {

            UI ui = UI.getCurrent();
            dataView = grid.setItems();
            dataView.refreshAll();
            metadaten=null;
            // Instruct client side to poll for changes and show spinner
            ui.setPollInterval(500);
            // Start background task
            CompletableFuture.runAsync(() -> {

                // Do some long running task
                try {
                    System.out.println("Hole Mailbox Infos");

                    metadaten=getMailboxes();


                    //Thread.sleep(2000); //2 Sekunden warten
                    Thread.sleep(20); //2 Sekunden warten

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Need to use access() when running from background thread
                ui.access(() -> {
                    // Stop polling and hide spinner
                    ui.setPollInterval(-1);

                    if (ret != 0) {
                        System.out.println("Keine Mailbox Infos gefunden!");
                        dataView = grid.setItems();
                        dataView.refreshAll();

                        return;
                    }
                    else{
                        //grid.setItems(metadaten);
                        dataView =grid.setItems(metadaten);
                        dataView.refreshAll();
                    }

                });
            });


        });

    }

   // private void showAblaufdaten(ItemDoubleClickEvent<Metadaten> e) {
   private void showAblaufdaten(ItemClickEvent<Metadaten> e) {

        System.out.println(("Aktualisiere Ablaufdaten Grid für NachrichtidIntern: " +  e.getItem().getNACHRICHTIDINTERN()));
        gridAblaufdaten.setItems();
        gridAblaufdaten.setItems(getAblaufdaten(e.getItem().getNACHRICHTIDINTERN().toString()));
    }

    //  private static ComponentRenderer<PersonDetailsFormLayout, Metadaten> createPersonDetailsRenderer() {
  //      return new ComponentRenderer<>(PersonDetailsFormLayout::new,
  //              PersonDetailsFormLayout::setAblaufdaten);
  //  }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    private void configureGrid() {
        grid.addClassName("metadaten-grid");
        grid.setSizeFull();
        grid.setColumns("NACHRICHTIDINTERN","NACHRICHTIDEXTERN", "STATUS", "FEHLERTAG", "VERARBEITET", "LOESCHTAG"); // primitive Variablen der Klasse können direkt angegeben werden.

        grid.getColumns().forEach(col -> col.setAutoWidth(true));


    }


    private List<Metadaten> getMailboxes(String searchTerm) {

        String sql = "select * from EKP.Metadaten where Nachrichtidextern ='" + searchTerm +"'";

        System.out.println("Filter EKP.Metadaten (MetadatenView.java) auf " + searchTerm );

        DriverManagerDataSource ds = new DriverManagerDataSource();
        Configuration conf;
        conf = comboBox.getValue();

        ds.setUrl(conf.getDb_Url());
        ds.setUsername(conf.getUserName());
        ds.setPassword(conf.getPassword());

        try {

            jdbcTemplate.setDataSource(ds);

            metadaten = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper(Metadaten.class));



            System.out.println("Metadaten eingelesen");

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return metadaten;
    }

    private List<Metadaten> getMailboxes() {

        String sql = "select TIMESTAMPVERSION,\n" +
                "       SENDERROLLEN,\n" +
                "       ID,\n" +
                "       nvl(to_char(EINGANGSDATUMSERVER,'dd.MM.YYYY HH24:MI'),'unbekannt') as EINGANGSDATUMSERVER,\n" +
                "       NACHRICHTIDINTERN,\n" +
                "       NACHRICHTIDEXTERN,\n" +
                "       STATUS,\n" +
                "       NACHRICHTTYP,\n" +
                "       TRANSPORTART,\n" +
                "       TRANSPORTVERSION,\n" +
                "       ART,\n" +
                "       SENDER,\n" +
                "       SENDERAKTENZEICHEN,\n" +
                "       SENDERGOVELLOID,\n" +
                "       SENDERPOSTFACHNAME,\n" +
                "       SENDERGESCHAEFTSZEICHEN,\n" +
                "       EMPFAENGER,\n" +
                "       EMPFAENGERAKTENZEICHEN,\n" +
                "       EMPFAENGERGOVELLOID,\n" +
                "       EMPFAENGERPOSTFACHNAME,\n" +
                "       WEITERLEITUNGGOVELLOID,\n" +
                "       WEITERLEITUNGPOSTFACHNAME,\n" +
                "       BETREFF,\n" +
                "       BEMERKUNG,\n" +
                "       ERSTELLUNGSDATUM,\n" +
                "       ABHOLDATUM,\n" +
                "       VERFALLSDATUM,\n" +
                "       SIGNATURPRUEFUNGSDATUM,\n" +
                "       VALIDIERUNGSDATUM,\n" +
                "       SIGNATURSTATUS,\n" +
                "       FACHVERFAHREN,\n" +
                "       FACHBEREICH,\n" +
                "       SACHGEBIET,\n" +
                "       ABTEILUNGE1,\n" +
                "       ABTEILUNGE2,\n" +
                "       PRIO,\n" +
                "       XJUSTIZVERSION,\n" +
                "       MANUELLBEARBEITETFLAG,\n" +
                "       BEARBEITERNAME,\n" +
                "       BEARBEITERKENNUNG,\n" +
                "       FEHLERTAG,\n" +
                "       PAPIERVORGANG,\n" +
                "       VERARBEITET,\n" +
                "       LOESCHTAG\n" +
                "from EKP.Metadaten \n " +
                "where nachrichtidextern is not null and (eingangsdatumserver is null or eingangsdatumserver > sysdate -1)";

        System.out.println("Abfrage EKP.Metadaten (MetadatenView.java)");

        DriverManagerDataSource ds = new DriverManagerDataSource();
        Configuration conf;
        conf = comboBox.getValue();

        ds.setUrl(conf.getDb_Url());
        ds.setUsername(conf.getUserName());
        ds.setPassword(conf.getPassword());

        try {

            jdbcTemplate.setDataSource(ds);

            metadaten = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper(Metadaten.class));



            System.out.println("Metadaten eingelesen");

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return metadaten;
    }

    private List<Ablaufdaten> getAblaufdaten(String nachrichtidintern) {

        String sql = "select * from EKP.Ablaufdaten where Nachrichtidintern ='" + nachrichtidintern +"'";

        System.out.println("Filter EKP.Ablaufdaten (MetadatenView.java) auf " + nachrichtidintern );

        DriverManagerDataSource ds = new DriverManagerDataSource();
        Configuration conf;
        conf = comboBox.getValue();

        ds.setUrl(conf.getDb_Url());
        ds.setUsername(conf.getUserName());
        ds.setPassword(conf.getPassword());

        try {

            jdbcTemplate.setDataSource(ds);

            ablaufdaten = jdbcTemplate.query(
                    sql,
                    new BeanPropertyRowMapper(Ablaufdaten.class));



            System.out.println("Ablaufdaten eingelesen");

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }

        return ablaufdaten;
    }

    private static class PersonDetailsFormLayout extends FormLayout {
        private final TextField emailField = new TextField("NachrichtidExtern");


        public PersonDetailsFormLayout() {
            Stream.of(emailField).forEach(field -> {
                field.setReadOnly(true);
                add(field);
            });

            setResponsiveSteps(new ResponsiveStep("0", 3));
            setColspan(emailField, 3);

        }

        public void setAblaufdaten(Configuration conf, Metadaten person) {
            emailField.setValue(person.getNACHRICHTIDEXTERN());
      //      getAblaufdaten(person.getNACHRICHTIDINTERN());

        }




    }



}