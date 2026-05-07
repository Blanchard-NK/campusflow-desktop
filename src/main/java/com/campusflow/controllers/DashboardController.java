package com.campusflow.controllers;

import com.campusflow.models.DashboardStats;
import com.campusflow.services.DataService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import com.campusflow.utils.SceneRouter;
import com.campusflow.utils.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.*;

/**
 * DashboardController — bound to {@code /fxml/dashboard.fxml}.
 *
 * Builds and populates 7 JavaFX charts:
 * <ol>
 *   <li>PieChart — student distribution by level</li>
 *   <li>BarChart — enrollment per formation</li>
 *   <li>BarChart — average grade per course</li>
 *   <li>BarChart — average satisfaction per course</li>
 *   <li>LineChart — activity trend over months</li>
 *   <li>Global satisfaction gauge (label + progress bar)</li>
 *   <li>KPI stat cards (counts)</li>
 * </ol>
 */
public class DashboardController implements Initializable, MainController.RefreshableController {

    // ── FXML — KPI labels ─────────────────────────────────────

    @FXML private Label kpiStudents;
    @FXML private Label kpiTeachers;
    @FXML private Label kpiCourses;
    @FXML private Label kpiReviews;
    @FXML private Label kpiSatisfaction;

    // ── FXML — Charts ─────────────────────────────────────────

    @FXML private PieChart                      pieLevel;
    @FXML private BarChart<String, Number>      barEnrollment;
    @FXML private BarChart<String, Number>      barGrades;
    @FXML private BarChart<String, Number>      barSatisfaction;
    @FXML private LineChart<String, Number>     lineActivity;

    // ── FXML — Global satisfaction ────────────────────────────

    @FXML private Label       globalSatLabel;
    @FXML private ProgressBar globalSatBar;

    // ── Service ───────────────────────────────────────────────

    private final DataService dataService = DataService.getInstance();
    private boolean loaded = false;

    // ── Initialise ────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        styleCharts();
        showPlaceholders();
    }

    @Override
    public void refresh() {
        if (!loaded) loadAsync();
    }

    // ── Async load ────────────────────────────────────────────

    private void loadAsync() {
        FxUtils.asyncRun(
            () -> {
                try {
                    return dataService.fetchDashboardStats();
                } catch (ApiException e) {
                    return dataService.getDemoDashboardStats();
                }
            },
            stats -> {
                updateKpis(stats);
                updatePie(stats);
                updateBarEnrollment(stats);
                updateBarGrades(stats);
                updateBarSatisfaction(stats);
                updateLine(stats);
                updateGlobalSat(stats);
                loaded = true;

                // Update main status bar
                MainController mc = SceneRouter.getInstance().getController(SceneRouter.MAIN);
                if (mc != null) mc.setStatus(
                    stats.getTotalStudents() + " étudiants · " +
                    stats.getTotalCourses()  + " formations · " +
                    (SessionManager.getInstance().isDemoMode() ? "Mode Démo" : "API connectée")
                );
            },
            ex -> {
                MainController mc = SceneRouter.getInstance().getController(SceneRouter.MAIN);
                if (mc != null) mc.setStatus("Erreur: " + ex.getMessage());
            }
        );
    }

    // ── KPI update ────────────────────────────────────────────

    private void updateKpis(DashboardStats s) {
        kpiStudents.setText(String.valueOf(s.getTotalStudents()));
        kpiTeachers.setText(String.valueOf(s.getTotalTeachers()));
        kpiCourses.setText(String.valueOf(s.getTotalCourses()));
        kpiReviews.setText(String.valueOf(s.getTotalReviews()));
        kpiSatisfaction.setText(s.getFormattedSatisfaction());
    }

    // ── Pie chart ─────────────────────────────────────────────

    private void updatePie(DashboardStats s) {
        List<PieChart.Data> data = new ArrayList<>();
        s.getStudentsByLevel().forEach((level, count) ->
            data.add(new PieChart.Data(level + " (" + count + ")", count)));
        pieLevel.setData(FXCollections.observableArrayList(data));
    }

    // ── Bar — enrollment ──────────────────────────────────────

    private void updateBarEnrollment(DashboardStats s) {
        barEnrollment.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscrits");
        s.getStudentsByLevel().forEach((level, count) ->
            series.getData().add(new XYChart.Data<>(level, count)));
        barEnrollment.getData().add(series);
    }

    // ── Bar — grades ──────────────────────────────────────────

    private void updateBarGrades(DashboardStats s) {
        barGrades.getData().clear();
        if (s.getAvgGradePerCourse().isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne /20");
        s.getAvgGradePerCourse().forEach((course, grade) ->
            series.getData().add(new XYChart.Data<>(abbrev(course, 12), grade)));
        barGrades.getData().add(series);
    }

    // ── Bar — satisfaction ────────────────────────────────────

    private void updateBarSatisfaction(DashboardStats s) {
        barSatisfaction.getData().clear();
        if (s.getAvgSatPerCourse().isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Satisfaction /5");
        s.getAvgSatPerCourse().forEach((course, sat) ->
            series.getData().add(new XYChart.Data<>(abbrev(course, 12), sat)));
        barSatisfaction.getData().add(series);
    }

    // ── Line — activity trend ─────────────────────────────────

    private void updateLine(DashboardStats s) {
        lineActivity.getData().clear();
        if (s.getActivityByMonth().isEmpty()) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avis reçus");
        new TreeMap<>(s.getActivityByMonth()).forEach((month, count) ->
            series.getData().add(new XYChart.Data<>(month, count)));
        lineActivity.getData().add(series);
    }

    // ── Global satisfaction ───────────────────────────────────

    private void updateGlobalSat(DashboardStats s) {
        double pct = s.getGlobalSatisfaction();
        globalSatLabel.setText(String.format("%.1f%%", pct));
        globalSatBar.setProgress(pct / 100.0);
    }

    // ── Chart styling ─────────────────────────────────────────

    private void styleCharts() {
        // Animated
        pieLevel.setAnimated(true);
        barEnrollment.setAnimated(true);
        barGrades.setAnimated(true);
        barSatisfaction.setAnimated(true);
        lineActivity.setAnimated(true);

        // Legends
        pieLevel.setLegendVisible(true);
        barEnrollment.setLegendVisible(false);
        barGrades.setLegendVisible(false);
        barSatisfaction.setLegendVisible(false);
        lineActivity.setLegendVisible(false);

        // Symbols on line chart
        lineActivity.setCreateSymbols(true);
    }

    private void showPlaceholders() {
        kpiStudents.setText("…");
        kpiTeachers.setText("…");
        kpiCourses.setText("…");
        kpiReviews.setText("…");
        kpiSatisfaction.setText("…");
        globalSatLabel.setText("…");
        globalSatBar.setProgress(0);
    }

    // ── Helpers ───────────────────────────────────────────────

    private String abbrev(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max) + "…" : s;
    }
}
