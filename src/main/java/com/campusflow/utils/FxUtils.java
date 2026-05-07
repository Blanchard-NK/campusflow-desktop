package com.campusflow.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.function.Consumer;

/**
 * FxUtils — shared JavaFX helpers.
 *
 * Threading, UI factories, and CSS snippets used across all controllers.
 */
public final class FxUtils {

    private FxUtils() {}

    // ── Threading ────────────────────────────────────────────

    /**
     * Runs {@code task} on a daemon background thread.
     * On success calls {@code onSuccess} on the FX thread.
     * On failure calls {@code onError} on the FX thread.
     */
    public static <T> void asyncRun(
            ThrowingSupplier<T> task,
            Consumer<T>         onSuccess,
            Consumer<Exception> onError) {

        Thread t = new Thread(() -> {
            try {
                T result = task.get();
                Platform.runLater(() -> onSuccess.accept(result));
            } catch (Exception e) {
                Platform.runLater(() -> onError.accept(e));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    // ── Color palette ─────────────────────────────────────────

    public static final String C_PRIMARY   = "#2563eb";
    public static final String C_SURFACE   = "#1e293b";
    public static final String C_BG        = "#0f172a";
    public static final String C_CARD      = "#1e293b";
    public static final String C_BORDER    = "#334155";
    public static final String C_TEXT      = "#f1f5f9";
    public static final String C_MUTED     = "#64748b";
    public static final String C_SUCCESS   = "#10b981";
    public static final String C_WARNING   = "#f59e0b";
    public static final String C_DANGER    = "#ef4444";
    public static final String C_VIOLET    = "#8b5cf6";
    public static final String C_CYAN      = "#06b6d4";

    public static final String[] LEVEL_COLORS = { C_CYAN, C_SUCCESS, C_WARNING, C_VIOLET };
    public static final String[] LEVEL_LIGHTS = {
        "rgba(6,182,212,0.15)", "rgba(16,185,129,0.15)",
        "rgba(245,158,11,0.15)", "rgba(139,92,246,0.15)"
    };
    public static final String[] LEVELS = { "BTS", "Bachelor", "Licence", "Master" };

    // ── UI factory methods ────────────────────────────────────

    /** A label styled as a page title (large, bold, light text). */
    public static Label pageTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 24));
        l.setTextFill(Color.web(C_TEXT));
        return l;
    }

    /** A label styled as a section subtitle. */
    public static Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 15));
        l.setTextFill(Color.web(C_TEXT));
        return l;
    }

    /** A muted secondary label. */
    public static Label muted(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("System", 12));
        l.setTextFill(Color.web(C_MUTED));
        return l;
    }

    /**
     * Dark themed card VBox.
     * Padding 20, rounded corners via inline style.
     */
    public static VBox card(javafx.scene.Node... children) {
        VBox v = new VBox(12);
        v.setPadding(new Insets(20));
        v.setStyle(
            "-fx-background-color: " + C_CARD + ";" +
            "-fx-background-radius: 12;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0, 0, 4);"
        );
        v.getChildren().addAll(children);
        return v;
    }

    /**
     * KPI stat card: icon + big number + label.
     *
     * @param icon    emoji or text symbol
     * @param value   big number string
     * @param label   description below
     * @param color   hex accent color for icon background tint
     * @param light   hex/rgba light version for icon badge background
     */
    public static VBox kpiCard(String icon, String value, String label,
                               String color, String light) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: " + C_CARD + ";" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 14, 0, 0, 4);"
        );

        Label iconLbl = new Label(icon);
        iconLbl.setFont(Font.font("System", 20));
        iconLbl.setStyle(
            "-fx-background-color: " + light + ";" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12 8 12;"
        );
        iconLbl.setTextFill(Color.web(color));

        Label valLbl = new Label(value);
        valLbl.setFont(Font.font("System", FontWeight.BOLD, 32));
        valLbl.setTextFill(Color.web(C_TEXT));

        Label lblLbl = new Label(label);
        lblLbl.setFont(Font.font("System", 12));
        lblLbl.setTextFill(Color.web(C_MUTED));

        card.getChildren().addAll(iconLbl, valLbl, lblLbl);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    /** Inline badge label (colored pill). */
    public static Label badge(String text, String bg, String fg) {
        Label l = new Label(text);
        l.setFont(Font.font("System", FontWeight.BOLD, 10));
        l.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: " + fg + ";" +
            "-fx-background-radius: 99;" +
            "-fx-padding: 3 9 3 9;"
        );
        return l;
    }

    /** Styled text field for dark theme. */
    public static TextField darkTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-prompt-text-fill: " + C_MUTED + ";" +
            "-fx-padding: 10 13 10 13;" +
            "-fx-font-size: 13px;"
        );
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    /** Styled password field for dark theme. */
    public static PasswordField darkPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(
            "-fx-background-color: #0f172a;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-prompt-text-fill: " + C_MUTED + ";" +
            "-fx-padding: 10 13 10 13;" +
            "-fx-font-size: 13px;"
        );
        pf.setMaxWidth(Double.MAX_VALUE);
        return pf;
    }

    /** Primary action button. */
    public static Button primaryButton(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: " + C_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 11 24 11 24;" +
            "-fx-background-radius: 9;" +
            "-fx-cursor: hand;"
        );
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color: #1d4ed8;" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 11 24 11 24;" +
            "-fx-background-radius: 9;" +
            "-fx-cursor: hand;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color: " + C_PRIMARY + ";" +
            "-fx-text-fill: white;" +
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-padding: 11 24 11 24;" +
            "-fx-background-radius: 9;" +
            "-fx-cursor: hand;"
        ));
        return b;
    }

    /** Ghost / secondary button. */
    public static Button ghostButton(String text) {
        Button b = new Button(text);
        b.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_MUTED + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 9 16 9 16;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );
        b.setOnMouseEntered(e -> b.setStyle(
            "-fx-background-color: " + C_SURFACE + ";" +
            "-fx-text-fill: " + C_TEXT + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 9 16 9 16;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        b.setOnMouseExited(e -> b.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + C_MUTED + ";" +
            "-fx-font-size: 13px;" +
            "-fx-padding: 9 16 9 16;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        ));
        return b;
    }

    /** Circular avatar with initials. */
    public static StackPane avatar(String initials, String bgColor, double radius) {
        Circle circle = new Circle(radius);
        circle.setFill(Color.web(bgColor));
        Text text = new Text(initials);
        text.setFont(Font.font("System", FontWeight.BOLD, radius * 0.6));
        text.setFill(Color.WHITE);
        StackPane sp = new StackPane(circle, text);
        sp.setAlignment(Pos.CENTER);
        return sp;
    }

    /** Flexible spacer that fills remaining HBox/VBox space. */
    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        VBox.setVgrow(r, Priority.ALWAYS);
        return r;
    }

    /** TableView pre-styled for dark theme. */
    public static <T> TableView<T> darkTable() {
        TableView<T> table = new TableView<>();
        table.setStyle(
            "-fx-background-color: " + C_CARD + ";" +
            "-fx-border-color: " + C_BORDER + ";" +
            "-fx-border-radius: 10;" +
            "-fx-background-radius: 10;" +
            "-fx-table-cell-border-color: " + C_BORDER + ";"
        );
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);
        return table;
    }

    /** Progress bar styled for dark theme. */
    public static ProgressBar darkProgressBar(double value, String accentColor) {
        ProgressBar pb = new ProgressBar(value);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setStyle(
            "-fx-accent: " + accentColor + ";" +
            "-fx-background-color: rgba(255,255,255,0.1);" +
            "-fx-background-radius: 4;" +
            "-fx-pref-height: 7;"
        );
        return pb;
    }

    /** Show a simple information alert. */
    public static void alertInfo(String title, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }

    /** Show a simple error alert. */
    public static void alertError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
