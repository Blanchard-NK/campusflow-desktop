package com.campusflow.controllers;

import com.campusflow.models.Teacher;
import com.campusflow.services.DataService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * TeachersController — bound to {@code /fxml/teachers.fxml}.
 */
public class TeachersController implements Initializable, MainController.RefreshableController {

    @FXML private TextField         searchField;
    @FXML private ComboBox<String>  specFilter;
    @FXML private TableView<Teacher> table;
    @FXML private Label             countLabel;
    @FXML private ProgressIndicator spinner;

    private final DataService dataService = DataService.getInstance();
    private ObservableList<Teacher> allTeachers;
    private FilteredList<Teacher>   filtered;
    private boolean loaded = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildColumns();
        specFilter.setItems(FXCollections.observableArrayList(
            "Toutes spécialités","Informatique","Mathématiques",
            "Économie","Langues","Sciences","Management"
        ));
        specFilter.setValue("Toutes spécialités");
        specFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        table.setPlaceholder(new Label("Chargement en cours…"));
    }

    @Override
    public void refresh() {
        if (!loaded) loadAsync();
    }

    private void buildColumns() {
        TableColumn<Teacher, String> colName = new TableColumn<>("Enseignant");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                Teacher t = getTableView().getItems().get(getIndex());
                HBox cell = new HBox(10);
                cell.setAlignment(Pos.CENTER_LEFT);
                String ini = (t.getFirstName() != null && !t.getFirstName().isEmpty() ? t.getFirstName().substring(0,1) : "?").toUpperCase()
                           + (t.getLastName()  != null && !t.getLastName().isEmpty()  ? t.getLastName().substring(0,1)  : "?").toUpperCase();
                StackPane av = FxUtils.avatar(ini, FxUtils.C_VIOLET, 16);
                VBox info = new VBox(1);
                Label nm = new Label(name);
                nm.setFont(Font.font("System", FontWeight.BOLD, 13));
                nm.setTextFill(Color.web(FxUtils.C_TEXT));
                Label em = new Label(t.getEmail() != null ? t.getEmail() : "");
                em.setFont(Font.font("System", 11));
                em.setTextFill(Color.web(FxUtils.C_MUTED));
                info.getChildren().addAll(nm, em);
                cell.getChildren().addAll(av, info);
                setGraphic(cell);
            }
        });
        colName.setPrefWidth(200);

        TableColumn<Teacher, String> colSpec = new TableColumn<>("Spécialité");
        colSpec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpeciality()));
        colSpec.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(FxUtils.badge(v, "rgba(37,99,235,0.2)", FxUtils.C_PRIMARY));
            }
        });
        colSpec.setPrefWidth(130);

        TableColumn<Teacher, String> colFormations = new TableColumn<>("Formations");
        colFormations.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormations()));

        TableColumn<Teacher, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String bg = switch (v) {
                    case "Titulaire"   -> "rgba(37,99,235,0.2)";
                    case "Contractuel" -> "rgba(245,158,11,0.2)";
                    default            -> "rgba(239,68,68,0.2)";
                };
                String fg = switch (v) {
                    case "Titulaire"   -> FxUtils.C_PRIMARY;
                    case "Contractuel" -> FxUtils.C_WARNING;
                    default            -> FxUtils.C_DANGER;
                };
                setGraphic(FxUtils.badge(v, bg, fg));
            }
        });
        colStatus.setPrefWidth(110);

        TableColumn<Teacher, Number> colHours = new TableColumn<>("H/sem.");
        colHours.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getHoursPerWeek()));
        colHours.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                Label l = new Label(v + "h");
                l.setFont(Font.font("System", FontWeight.BOLD, 13));
                l.setTextFill(Color.web(FxUtils.C_TEXT));
                setGraphic(l);
            }
        });
        colHours.setPrefWidth(80);

        table.getColumns().setAll(colName, colSpec, colFormations, colStatus, colHours);
    }

    private void applyFilter() {
        if (filtered == null) return;
        String q    = searchField.getText().toLowerCase().trim();
        String spec = specFilter.getValue();
        filtered.setPredicate(t -> {
            boolean mQ = q.isEmpty()
                || t.getFullName().toLowerCase().contains(q)
                || (t.getEmail() != null && t.getEmail().toLowerCase().contains(q));
            boolean mS = "Toutes spécialités".equals(spec) || t.getSpeciality().equals(spec);
            return mQ && mS;
        });
        int n = filtered.size();
        countLabel.setText(n + " enseignant" + (n > 1 ? "s" : ""));
    }

    private void loadAsync() {
        spinner.setVisible(true);
        FxUtils.asyncRun(
            () -> {
                try { return dataService.fetchTeachers(); }
                catch (ApiException e) { return dataService.getDemoTeachers(); }
            },
            data -> {
                allTeachers = FXCollections.observableArrayList(data);
                filtered    = new FilteredList<>(allTeachers);
                table.setItems(filtered);
                table.setPlaceholder(new Label("Aucun enseignant trouvé."));
                countLabel.setText(data.size() + " enseignants");
                spinner.setVisible(false);
                loaded = true;
            },
            ex -> { table.setPlaceholder(new Label("Erreur: " + ex.getMessage())); spinner.setVisible(false); }
        );
    }
}
