package com.campusflow.app;

import com.campusflow.utils.SceneRouter;
import com.campusflow.utils.SessionManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * CampusFlowApp — JavaFX Application entry point.
 *
 * Initialises the {@link SceneRouter} singleton with the primary {@link Stage},
 * then navigates directly to the login screen.
 *
 * Run with: {@code mvn javafx:run}
 */
public class CampusFlowApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Configure the primary stage
        primaryStage.setTitle("CampusFlow — Academic Management Dashboard");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(680);
        primaryStage.setWidth(1280);
        primaryStage.setHeight(780);

        // Initialise navigation router
        SceneRouter router = SceneRouter.getInstance();
        router.init(primaryStage);

        // Clear any stale session
        SessionManager.getInstance().clearSession();

        // Show login screen
        router.show(SceneRouter.LOGIN);

        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up session on application exit
        SessionManager.getInstance().clearSession();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
