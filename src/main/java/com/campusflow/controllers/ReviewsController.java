package com.campusflow.controllers;

import com.campusflow.models.Review;
import com.campusflow.services.DataService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * ReviewsController — bound to {@code /fxml/reviews.fxml}.
 *
 * Shows student reviews in a table and computes per-course satisfaction stats
 * displayed as a side panel with progress bars.
 */
public class ReviewsController implements Initializable, MainController.RefreshableController {

    @FXML private TextField        searchField;
    @FXML private TableView<Review> table;
    @FXML private Label             countLabel;
    @FXML private Label             globalSatLabel;
    @FXML private VBox              statsPanel;
    @FXML private ProgressIndicator spinner;

    private final DataService dataService = DataService.getInstance();
    private ObservableList<Review> allReviews;
    private FilteredList<Review>   filtered;
    private boolean loaded = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        buildColumns();
        searchField.textProperty().addListener((obs, o, n) -> applyFilter());
        table.setPlaceholder(new Label("Chargement en cours…"));
    }

    @Override
    public void refresh() {
        if (!loaded) loadAsync();
    }

    private void buildColumns() {
        TableColumn<Review, String> colStudent = new TableColumn<>("Étudiant");
        colStudent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStudentName()));
        colStudent.setPrefWidth(160);

        TableColumn<Review, String> colCourse = new TableColumn<>("Formation");
        colCourse.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCourseName()));
        colCourse.setPrefWidth(160);

        TableColumn<Review, Number> colRating = new TableColumn<>("Note");
        colRating.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getRating()));
        colRating.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Number v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                Review r = getTableView().getItems().get(getIndex());
                HBox row = new HBox(6);
                row.setAlignment(Pos.CENTER_LEFT);
                Label stars = new Label(r.getStarRating());
                stars.setFont(Font.font("System", 13));
                stars.setTextFill(Color.web(FxUtils.C_WARNING));
                Label num = new Label(String.format("%.1f", v.doubleValue()));
                num.setFont(Font.font("System", FontWeight.BOLD, 12));
                num.setTextFill(Color.web(FxUtils.C_TEXT));
                row.getChildren().addAll(stars, num);
                setGraphic(row);
            }
        });
        colRating.setPrefWidth(130);

        TableColumn<Review, String> colComment = new TableColumn<>("Commentaire");
        colComment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getComment()));
        colComment.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                Label l = new Label(v);
                l.setWrapText(true);
                l.setMaxWidth(300);
                l.setFont(Font.font("System", 12));
                l.setTextFill(Color.web(FxUtils.C_MUTED));
                setGraphic(l);
            }
        });

        TableColumn<Review, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt()));
        colDate.setPrefWidth(110);

        table.getColumns().setAll(colStudent, colCourse, colRating, colComment, colDate);
    }

    private void applyFilter() {
        if (filtered == null) return;
        String q = searchField.getText().toLowerCase().trim();
        filtered.setPredicate(r ->
            q.isEmpty()
            || (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(q))
            || (r.getCourseName()  != null && r.getCourseName().toLowerCase().contains(q))
            || (r.getComment()     != null && r.getComment().toLowerCase().contains(q))
        );
        countLabel.setText(filtered.size() + " avis");
    }

    private void loadAsync() {
        spinner.setVisible(true);
        FxUtils.asyncRun(
            () -> {
                try { return dataService.fetchReviews(); }
                catch (ApiException e) { return dataService.getDemoReviews(); }
            },
            data -> {
                allReviews = FXCollections.observableArrayList(data);
                filtered   = new FilteredList<>(allReviews);
                table.setItems(filtered);
                table.setPlaceholder(new Label("Aucun avis trouvé."));
                countLabel.setText(data.size() + " avis");
                updateStats(data);
                spinner.setVisible(false);
                loaded = true;
            },
            ex -> { spinner.setVisible(false); }
        );
    }

    private void updateStats(List<Review> reviews) {
        double globalAvg = reviews.stream().mapToDouble(Review::getRating).average().orElse(0);
        globalSatLabel.setText(String.format("%.1f / 5  (%.0f%%)", globalAvg, globalAvg / 5.0 * 100));

        // Per-course average, sorted descending
        Map<String, DoubleSummaryStatistics> byCourse = reviews.stream()
            .filter(r -> r.getCourseName() != null)
            .collect(Collectors.groupingBy(Review::getCourseName,
                Collectors.summarizingDouble(Review::getRating)));

        List<Map.Entry<String, DoubleSummaryStatistics>> sorted = byCourse.entrySet().stream()
            .sorted((a, b) -> Double.compare(b.getValue().getAverage(), a.getValue().getAverage()))
            .collect(Collectors.toList());

        Label perCourseLbl = new Label("Par formation");
        perCourseLbl.setFont(Font.font("System", FontWeight.BOLD, 12));
        perCourseLbl.setTextFill(Color.web(FxUtils.C_TEXT));
        statsPanel.getChildren().add(perCourseLbl);

        for (Map.Entry<String, DoubleSummaryStatistics> e : sorted) {
            double avg = e.getValue().getAverage();
            long   cnt = e.getValue().getCount();
            String nm  = e.getKey();

            VBox row = new VBox(4);
            row.setPadding(new Insets(0, 0, 8, 0));

            HBox lRow = new HBox();
            Label cLbl = new Label(nm.length() > 22 ? nm.substring(0, 22) + "…" : nm);
            cLbl.setFont(Font.font("System", 12));
            cLbl.setTextFill(Color.web(FxUtils.C_MUTED));
            Label aLbl = new Label(String.format("%.1f (%d)", avg, cnt));
            aLbl.setFont(Font.font("System", FontWeight.BOLD, 11));
            aLbl.setTextFill(Color.web(FxUtils.C_PRIMARY));
            Region sp = FxUtils.spacer();
            lRow.getChildren().addAll(cLbl, sp, aLbl);

            String barColor = avg >= 4.0 ? FxUtils.C_SUCCESS
                            : avg >= 3.0 ? FxUtils.C_WARNING
                            : FxUtils.C_DANGER;
            ProgressBar pb = FxUtils.darkProgressBar(avg / 5.0, barColor);
            row.getChildren().addAll(lRow, pb);
            statsPanel.getChildren().add(row);
        }
    }
}
