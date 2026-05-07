package com.campusflow.controllers;

import com.campusflow.config.AppConfig;
import com.campusflow.services.AuthService;
import com.campusflow.api.ApiException;
import com.campusflow.utils.FxUtils;
import com.campusflow.utils.SceneRouter;
import com.campusflow.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController — bound to {@code /fxml/login.fxml}.
 *
 * Handles credential entry, real API login, and demo mode activation.
 * All HTTP calls run on a daemon background thread; UI updates are
 * dispatched back onto the JavaFX Application Thread via {@link Platform#runLater}.
 */
public class LoginController implements Initializable {

    // ── FXML injections ───────────────────────────────────────

    @FXML private VBox     rootBox;
    @FXML private Label    titleLabel;
    @FXML private Label    subtitleLabel;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button        loginButton;
    @FXML private Button        demoButton;
    @FXML private Label         errorLabel;
    @FXML private ProgressIndicator spinner;
    @FXML private Label    versionLabel;

    // ── Services ──────────────────────────────────────────────

    private final AuthService authService = AuthService.getInstance();

    // ── Initialise ────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        spinner.setVisible(false);

        // Pre-fill demo credentials for convenience
        emailField.setText(AppConfig.DEMO_EMAIL);
        passwordField.setText(AppConfig.DEMO_PASSWORD);

        // Hide demo button if demo mode disabled
        if (!AppConfig.DEMO_MODE && demoButton != null) {
            demoButton.setVisible(false);
            demoButton.setManaged(false);
        }

        versionLabel.setText(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);

        // Allow Enter to submit
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());
    }

    // ── Actions ───────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

        // Client-side validation
        if (email.isBlank()) {
            showError("Veuillez saisir votre adresse e-mail.");
            return;
        }
        if (!email.contains("@")) {
            showError("Adresse e-mail invalide.");
            return;
        }
        if (password.isBlank()) {
            showError("Veuillez saisir votre mot de passe.");
            return;
        }

        clearError();
        setLoading(true);

        // Background thread for HTTP call
        FxUtils.asyncRun(
            () -> { authService.login(email, password); return null; },
            ignored -> navigateToMain(),
            ex -> {
                String msg = (ex instanceof ApiException ae && ae.isNetworkError())
                    ? "Serveur inaccessible. Utilisez le mode démo pour tester l'application."
                    : ex.getMessage();
                showError(msg);
                setLoading(false);
            }
        );
    }

    @FXML
    private void handleDemoLogin() {
        SessionManager.getInstance().startDemoSession();
        navigateToMain();
    }

    // ── Private helpers ───────────────────────────────────────

    private void navigateToMain() {
        SceneRouter router = SceneRouter.getInstance();
        router.invalidate(SceneRouter.MAIN); // always load fresh
        router.show(SceneRouter.MAIN);

        // Initialise the main controller after the scene is set
        MainController mc = router.getController(SceneRouter.MAIN);
        if (mc != null) mc.onEnter();
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    private void setLoading(boolean loading) {
        loginButton.setDisable(loading);
        if (demoButton != null) demoButton.setDisable(loading);
        spinner.setVisible(loading);
    }
}
