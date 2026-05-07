package com.campusflow.controllers;

import com.campusflow.models.Course;
import com.campusflow.services.DataService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * CoursesController — bound to {@code /fxml/courses.fxml}.
 *
 * Displays formation cards with enrollment progress bars.
 */
public class CoursesController implements Initializable, MainController.RefreshableController {

    @FXML private TextField        searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private FlowPane         cardGrid;
    @FXML private Label            countLabel;
    @FXML private ScrollPane       scrollPane;
    @FXML private ProgressIndicator spinner;

    private final DataService dataService = DataService.getInstance();
    private List<Course> allCourses;
    private boolean loaded = false;

    private static final String[] LEVELS  = { "BTS", "Bachelor", "Licence", "Master" };
    private static final String[] COLORS  = {
        FxUtils.C_CYAN, FxUtils.C_SUCCESS, FxUtils.C_WARNING, FxUtils.C_VIOLET
    };
    private static final String[] LIGHTS  = {
        "rgba(6,182,212,0.15)", "rgba(16,185,129,0.15)",
        "rgba(245,158,11,0.15)", "rgba(139,92,246,0.15)"
    };

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        levelFilter.setItems(FXCollections.observableArrayList(
            "Tous niveaux", "BTS", "Bachelor", "Licence", "Master"
        ));
        levelFilter.setValue("Tous niveaux");
        levelFilter.valueProperty().addListener((obs, o, n) -> applyFilter());
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());

        cardGrid.setHgap(16);
        cardGrid.setVgap(16);
        cardGrid.setPadding(new Insets(4));
    }

    @Override
    public void refresh() {
        if (!loaded) loadAsync();
    }

    private void applyFilter() {
        if (allCourses == null) return;
        String q     = searchField.getText().toLowerCase().trim();
        String level = levelFilter.getValue();
        List<Course> filtered = allCourses.stream()
            .filter(c -> q.isEmpty()
                || c.getName().toLowerCase().contains(q)
                || (c.getDomain() != null && c.getDomain().toLowerCase().contains(q)))
            .filter(c -> "Tous niveaux".equals(level) || c.getLevel().equals(level))
            .collect(Collectors.toList());
        renderCards(filtered);
        countLabel.setText(filtered.size() + " formation" + (filtered.size() > 1 ? "s" : ""));
    }

    private void renderCards(List<Course> courses) {
        cardGrid.getChildren().clear();
        for (Course c : courses) cardGrid.getChildren().add(buildCard(c));
    }

    private VBox buildCard(Course course) {
        // Determine accent color by level
        String color = FxUtils.C_PRIMARY;
        String light = "rgba(37,99,235,0.15)";
        for (int i = 0; i < LEVELS.length; i++) {
            if (LEVELS[i].equals(course.getLevel())) {
                color = COLORS[i];
                light = LIGHTS[i];
                break;
            }
        }
        final String fColor = color;
        final String fLight = light;

        VBox card = new VBox(0);
        card.setPrefWidth(290);
        card.setMaxWidth(310);
        card.setStyle(
            "-fx-background-color: " + FxUtils.C_CARD + ";" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 14, 0, 0, 4);"
        );

        // Header strip
        VBox head = new VBox(6);
        head.setPadding(new Insets(16, 18, 12, 18));
        head.setStyle("-fx-background-color: " + fLight + "; -fx-background-radius: 14 14 0 0;");

        Label lvl = levelBadge(course.getLevel(), fColor);

        Label name = new Label(course.getName());
        name.setFont(Font.font("System", FontWeight.BOLD, 14));
        name.setTextFill(Color.web(FxUtils.C_TEXT));
        name.setWrapText(true);

        Label domain = new Label("📁  " + (course.getDomain() != null ? course.getDomain() : "—"));
        domain.setFont(Font.font("System", 11));
        domain.setTextFill(Color.web(FxUtils.C_MUTED));

        head.getChildren().addAll(lvl, name, domain);

        // Body
        VBox body = new VBox(12);
        body.setPadding(new Insets(14, 18, 14, 18));

        // Stats row
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);
        stats.getChildren().addAll(
            statWidget(String.valueOf(course.getEnrolled()), "Inscrits"),
            statWidget(String.valueOf(course.getCapacity()), "Capacité"),
            statWidget(course.getDuration(), "Durée")
        );

        // Progress
        VBox prog = new VBox(5);
        double pct = course.getFillPercentage();
        HBox progRow = new HBox();
        Label progLbl = new Label("Remplissage");
        progLbl.setFont(Font.font("System", 11)); progLbl.setTextFill(Color.web(FxUtils.C_MUTED));
        Label progPct = new Label(String.format("%.0f%%", pct));
        progPct.setFont(Font.font("System", FontWeight.BOLD, 11)); progPct.setTextFill(Color.web(FxUtils.C_TEXT));
        Region sp = FxUtils.spacer();
        progRow.getChildren().addAll(progLbl, sp, progPct);

        ProgressBar pb = FxUtils.darkProgressBar(pct / 100.0, fColor);
        prog.getChildren().addAll(progRow, pb);

        body.getChildren().addAll(stats, prog);

        if (course.getAverageGrade() > 0) {
            Label grade = new Label(String.format("🎯  Moy. %.1f / 20", course.getAverageGrade()));
            grade.setFont(Font.font("System", FontWeight.BOLD, 12));
            grade.setTextFill(Color.web(fColor));
            body.getChildren().add(grade);
        }

        // Footer
        HBox foot = new HBox();
        foot.setPadding(new Insets(8, 18, 12, 18));
        foot.setStyle("-fx-border-color: " + FxUtils.C_BORDER + "; -fx-border-width: 1 0 0 0;");
        Label seats = new Label("💺  " + course.getAvailableSeats() + " places disponibles");
        seats.setFont(Font.font("System", 11));
        seats.setTextFill(Color.web(FxUtils.C_MUTED));
        foot.getChildren().add(seats);

        card.getChildren().addAll(head, body, foot);
        return card;
    }

    private VBox statWidget(String value, String label) {
        VBox v = new VBox(2);
        v.setAlignment(Pos.CENTER);
        Label val = new Label(value);
        val.setFont(Font.font("System", FontWeight.BOLD, 18));
        val.setTextFill(Color.web(FxUtils.C_TEXT));
        Label lbl = new Label(label);
        lbl.setFont(Font.font("System", 10));
        lbl.setTextFill(Color.web(FxUtils.C_MUTED));
        v.getChildren().addAll(val, lbl);
        return v;
    }

    private Label levelBadge(String level, String color) {
        Label l = new Label(level);
        l.setFont(Font.font("System", FontWeight.BOLD, 9));
        l.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-text-fill: #0f172a;" +
            "-fx-background-radius: 99;" +
            "-fx-padding: 3 9 3 9;"
        );
        return l;
    }

    private void loadAsync() {
        spinner.setVisible(true);
        FxUtils.asyncRun(
            () -> {
                try { return dataService.fetchCourses(); }
                catch (ApiException e) { return dataService.getDemoCourses(); }
            },
            data -> {
                allCourses = data;
                renderCards(data);
                countLabel.setText(data.size() + " formations");
                spinner.setVisible(false);
                loaded = true;
            },
            ex -> { spinner.setVisible(false); }
        );
    }
}
