package com.campusflow.controllers;

import com.campusflow.models.Student;
import com.campusflow.services.DataService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import com.campusflow.utils.SceneRouter;
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
import java.util.List;
import java.util.ResourceBundle;

/**
 * StudentsController — bound to {@code /fxml/students.fxml}.
 *
 * Displays all students in a searchable, filterable TableView.
 * Data is fetched on first display, cached for the session lifetime.
 */
public class StudentsController implements Initializable, MainController.RefreshableController {

    @FXML private TextField      searchField;
    @FXML private ComboBox<String> formationFilter;
    @FXML private TableView<Student> table;
    @FXML private Label          countLabel;
    @FXML private ProgressIndicator spinner;

    private final DataService dataService = DataService.getInstance();
    private ObservableList<Student> allStudents;
    private FilteredList<Student>   filtered;
    private boolean loaded = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildColumns();
        formationFilter.setItems(FXCollections.observableArrayList(
            "Toutes formations", "BTS", "Bachelor", "Licence", "Master"
        ));
        formationFilter.setValue("Toutes formations");
        formationFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        table.setPlaceholder(new Label("Chargement en cours…"));
    }

    @Override
    public void refresh() {
        if (!loaded) loadAsync();
    }

    // ── Table columns ─────────────────────────────────────────

    private void buildColumns() {
        // Name column with avatar
        TableColumn<Student, String> colName = new TableColumn<>("Étudiant");
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colName.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setGraphic(null); return; }
                Student s = getTableView().getItems().get(getIndex());
                HBox cell = new HBox(10);
                cell.setAlignment(Pos.CENTER_LEFT);

                String ini = initials(s.getFirstName(), s.getLastName());
                StackPane av = FxUtils.avatar(ini, FxUtils.C_PRIMARY, 16);

                VBox info = new VBox(1);
                Label nm = new Label(name);
                nm.setFont(Font.font("System", FontWeight.BOLD, 13));
                nm.setTextFill(Color.web(FxUtils.C_TEXT));
                Label em = new Label(s.getEmail() != null ? s.getEmail() : "");
                em.setFont(Font.font("System", 11));
                em.setTextFill(Color.web(FxUtils.C_MUTED));
                info.getChildren().addAll(nm, em);
                cell.getChildren().addAll(av, info);
                setGraphic(cell);
            }
        });
        colName.setPrefWidth(210);

        // Formation
        TableColumn<Student, String> colFormation = new TableColumn<>("Formation");
        colFormation.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormation()));
        colFormation.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(levelBadge(v));
            }
        });
        colFormation.setPrefWidth(110);

        // Speciality
        TableColumn<Student, String> colSpec = new TableColumn<>("Spécialité");
        colSpec.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpeciality()));
        colSpec.setStyle("-fx-text-fill: " + FxUtils.C_MUTED + "; -fx-font-size: 12px;");

        // Year
        TableColumn<Student, String> colYear = new TableColumn<>("Année");
        colYear.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getYear()));
        colYear.setPrefWidth(110);

        // Status
        TableColumn<Student, String> colStatus = new TableColumn<>("Statut");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(statusBadge(v));
            }
        });
        colStatus.setPrefWidth(100);

        table.getColumns().setAll(colName, colFormation, colSpec, colYear, colStatus);
    }

    // ── Filter ────────────────────────────────────────────────

    private void applyFilter() {
        if (filtered == null) return;
        String q    = searchField.getText().toLowerCase().trim();
        String form = formationFilter.getValue();
        filtered.setPredicate(s -> {
            boolean mQ = q.isEmpty()
                || s.getFullName().toLowerCase().contains(q)
                || (s.getEmail() != null && s.getEmail().toLowerCase().contains(q))
                || (s.getSpeciality() != null && s.getSpeciality().toLowerCase().contains(q));
            boolean mF = "Toutes formations".equals(form) || s.getFormation().equals(form);
            return mQ && mF;
        });
        int n = filtered.size();
        countLabel.setText(n + " étudiant" + (n > 1 ? "s" : ""));
    }

    // ── Async load ────────────────────────────────────────────

    private void loadAsync() {
        spinner.setVisible(true);
        FxUtils.asyncRun(
            () -> {
                try { return dataService.fetchStudents(); }
                catch (ApiException e) { return dataService.getDemoStudents(); }
            },
            data -> {
                allStudents = FXCollections.observableArrayList(data);
                filtered    = new FilteredList<>(allStudents);
                table.setItems(filtered);
                table.setPlaceholder(new Label("Aucun étudiant trouvé."));
                countLabel.setText(data.size() + " étudiants");
                spinner.setVisible(false);
                loaded = true;
            },
            ex -> {
                table.setPlaceholder(new Label("Erreur: " + ex.getMessage()));
                spinner.setVisible(false);
            }
        );
    }

    // ── UI helpers ────────────────────────────────────────────

    private Label levelBadge(String level) {
        return switch (level) {
            case "BTS"      -> FxUtils.badge("BTS",      "rgba(6,182,212,0.2)",   FxUtils.C_CYAN);
            case "Bachelor" -> FxUtils.badge("Bachelor", "rgba(16,185,129,0.2)",  FxUtils.C_SUCCESS);
            case "Licence"  -> FxUtils.badge("Licence",  "rgba(245,158,11,0.2)",  FxUtils.C_WARNING);
            case "Master"   -> FxUtils.badge("Master",   "rgba(139,92,246,0.2)",  FxUtils.C_VIOLET);
            default         -> FxUtils.badge(level,      "rgba(100,116,139,0.2)", FxUtils.C_MUTED);
        };
    }

    private Label statusBadge(String status) {
        return switch (status) {
            case "Actif"    -> FxUtils.badge("Actif",    "rgba(16,185,129,0.2)",  FxUtils.C_SUCCESS);
            case "Suspendu" -> FxUtils.badge("Suspendu", "rgba(245,158,11,0.2)",  FxUtils.C_WARNING);
            case "Diplômé"  -> FxUtils.badge("Diplômé",  "rgba(37,99,235,0.2)",   FxUtils.C_PRIMARY);
            default         -> FxUtils.badge(status,     "rgba(100,116,139,0.2)", FxUtils.C_MUTED);
        };
    }

    private String initials(String first, String last) {
        String a = (first != null && !first.isEmpty()) ? first.substring(0,1).toUpperCase() : "?";
        String b = (last  != null && !last.isEmpty())  ? last.substring(0,1).toUpperCase()  : "?";
        return a + b;
    }
}
