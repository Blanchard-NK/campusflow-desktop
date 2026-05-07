package com.campusflow.controllers;

import com.campusflow.utils.FxUtils;
import com.campusflow.utils.SceneRouter;
import com.campusflow.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * MainController — bound to {@code /fxml/main.fxml}.
 *
 * Acts as the application shell: sidebar navigation + dynamic content pane.
 * Sub-pages are loaded lazily from their own FXML files and cached in memory.
 */
public class MainController implements Initializable {

    // ── Page keys ─────────────────────────────────────────────
    public static final String PAGE_DASHBOARD = "dashboard";
    public static final String PAGE_STUDENTS  = "students";
    public static final String PAGE_TEACHERS  = "teachers";
    public static final String PAGE_COURSES   = "courses";
    public static final String PAGE_REVIEWS   = "reviews";

    // ── FXML injections ───────────────────────────────────────

    @FXML private StackPane contentPane;
    @FXML private Label     userNameLabel;
    @FXML private Label     userRoleLabel;
    @FXML private Label     userInitialsLabel;
    @FXML private Label     statusLabel;
    @FXML private Label     breadcrumbLabel;
    @FXML private ProgressBar globalProgressBar;

    // Sidebar nav items
    @FXML private VBox navDashboard;
    @FXML private VBox navStudents;
    @FXML private VBox navTeachers;
    @FXML private VBox navCourses;
    @FXML private VBox navReviews;

    // ── Fields ────────────────────────────────────────────────

    private final Map<String, Node>   pageCache       = new HashMap<>();
    private final Map<String, Object> controllerCache = new HashMap<>();
    private String currentPage = "";

    // ── Initialise ────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Will be populated in onEnter() after the session is ready
    }

    /** Called by LoginController after navigating to this scene. */
    public void onEnter() {
        SessionManager sm = SessionManager.getInstance();
        userNameLabel.setText(sm.getUserName());
        userRoleLabel.setText(sm.isDemoMode() ? "Mode Démo" : sm.getUserRole());
        userInitialsLabel.setText(sm.getInitials());

        setStatus(sm.isDemoMode()
            ? "Mode Démo — données simulées"
            : "Connecté à " + com.campusflow.config.AppConfig.BASE_URL);

        navigateTo(PAGE_DASHBOARD);
    }

    // ── Navigation ────────────────────────────────────────────

    @FXML private void onNavDashboard() { navigateTo(PAGE_DASHBOARD); }
    @FXML private void onNavStudents()  { navigateTo(PAGE_STUDENTS); }
    @FXML private void onNavTeachers()  { navigateTo(PAGE_TEACHERS); }
    @FXML private void onNavCourses()   { navigateTo(PAGE_COURSES); }
    @FXML private void onNavReviews()   { navigateTo(PAGE_REVIEWS); }

    @FXML
    private void onLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText("Confirmer la déconnexion");
        alert.setContentText("Voulez-vous vraiment vous déconnecter ?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().clearSession();
            pageCache.clear();
            controllerCache.clear();
            SceneRouter.getInstance().show(SceneRouter.LOGIN);
        }
    }

    // ── Internal nav ──────────────────────────────────────────

    private void navigateTo(String pageKey) {
        if (pageKey.equals(currentPage)) return;
        currentPage = pageKey;

        // Update sidebar highlighting
        updateSidebarHighlight(pageKey);
        updateBreadcrumb(pageKey);

        // Load page if not cached
        if (!pageCache.containsKey(pageKey)) {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/" + pageKey + ".fxml"));
                Node node = loader.load();
                pageCache.put(pageKey, node);
                controllerCache.put(pageKey, loader.getController());
            } catch (IOException e) {
                setStatus("Erreur chargement page: " + e.getMessage());
                return;
            }
        }

        // Swap content
        contentPane.getChildren().setAll(pageCache.get(pageKey));

        // Notify sub-controller
        Object ctrl = controllerCache.get(pageKey);
        if (ctrl instanceof RefreshableController rc) rc.refresh();
    }

    // ── Sidebar state ──────────────────────────────────────────

    private static final String ACTIVE_STYLE =
        "-fx-background-color: rgba(37,99,235,0.25);" +
        "-fx-background-radius: 8;";
    private static final String INACTIVE_STYLE =
        "-fx-background-color: transparent;" +
        "-fx-background-radius: 8;";

    private void updateSidebarHighlight(String pageKey) {
        Map<String, VBox> navMap = Map.of(
            PAGE_DASHBOARD, navDashboard,
            PAGE_STUDENTS,  navStudents,
            PAGE_TEACHERS,  navTeachers,
            PAGE_COURSES,   navCourses,
            PAGE_REVIEWS,   navReviews
        );
        navMap.forEach((key, box) ->
            box.setStyle(key.equals(pageKey) ? ACTIVE_STYLE : INACTIVE_STYLE));
    }

    private static final Map<String, String> BREADCRUMBS = Map.of(
        PAGE_DASHBOARD, "🏠  Tableau de bord",
        PAGE_STUDENTS,  "🏠  ›  Étudiants",
        PAGE_TEACHERS,  "🏠  ›  Enseignants",
        PAGE_COURSES,   "🏠  ›  Formations",
        PAGE_REVIEWS,   "🏠  ›  Avis & Feedback"
    );

    private void updateBreadcrumb(String pageKey) {
        breadcrumbLabel.setText(BREADCRUMBS.getOrDefault(pageKey, pageKey));
    }

    // ── Status / progress ─────────────────────────────────────

    public void setStatus(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    public void setLoading(boolean loading) {
        if (globalProgressBar != null) {
            globalProgressBar.setVisible(loading);
            globalProgressBar.setProgress(loading ? ProgressBar.INDETERMINATE_PROGRESS : 0);
        }
    }

    // ── Sub-controller interface ──────────────────────────────

    /**
     * Sub-page controllers may implement this to reload their data
     * every time the page is activated.
     */
    public interface RefreshableController {
        void refresh();
    }
}
