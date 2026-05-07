package com.campusflow.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * SceneRouter — centralised navigation helper.
 *
 * Loads FXML files, caches them, and swaps the root scene on the primary Stage.
 * Controllers can call {@code SceneRouter.getInstance().show("dashboard")} from
 * any thread as long as it is eventually invoked on the JavaFX Application Thread.
 */
public class SceneRouter {

    // ── Singleton ─────────────────────────────────────────────

    private static SceneRouter instance;

    public static synchronized SceneRouter getInstance() {
        if (instance == null) instance = new SceneRouter();
        return instance;
    }

    private SceneRouter() {}

    // ── Fields ────────────────────────────────────────────────

    private Stage primaryStage;

    /** FXML cache: scene name → loaded Parent root node. */
    private final Map<String, Parent> cache = new HashMap<>();

    /** Controllers cache: scene name → controller instance. */
    private final Map<String, Object> controllers = new HashMap<>();

    // ── Scene names (constants) ───────────────────────────────

    public static final String LOGIN     = "login";
    public static final String MAIN      = "main";

    // ── Initialisation ────────────────────────────────────────

    /** Must be called once from {@code CampusFlowApp.start()} before any navigation. */
    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    // ── Navigation ────────────────────────────────────────────

    /**
     * Shows the named scene on the primary stage.
     * The FXML file is expected at {@code /fxml/<name>.fxml}.
     *
     * @param name scene name (use constants defined above)
     * @throws RuntimeException if the FXML cannot be loaded
     */
    public void show(String name) {
        try {
            Parent root = loadFxml(name);
            Scene scene = primaryStage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                scene.setRoot(root);
            }
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load FXML for scene: " + name, e);
        }
    }

    /**
     * Returns the controller associated with a previously loaded scene.
     *
     * @param name scene name
     * @param <T>  expected controller type
     * @return controller instance, or null if not yet loaded
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(String name) {
        return (T) controllers.get(name);
    }

    // ── Internal ──────────────────────────────────────────────

    private Parent loadFxml(String name) throws IOException {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        String resource = "/fxml/" + name + ".fxml";
        URL url = getClass().getResource(resource);
        if (url == null) {
            throw new IOException("FXML not found on classpath: " + resource);
        }

        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        cache.put(name, root);
        controllers.put(name, loader.getController());
        return root;
    }

    // ── Stage accessors ───────────────────────────────────────

    public Stage getPrimaryStage() { return primaryStage; }

    /**
     * Force-reloads a scene (drops it from cache).
     * Use this after logout to reset the main view state.
     */
    public void invalidate(String name) {
        cache.remove(name);
        controllers.remove(name);
    }
}
