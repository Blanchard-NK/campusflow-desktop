package com.campusflow.config;

/**
 * AppConfig — centralized application configuration.
 *
 * Change BASE_URL to point at your running CampusFlow backend.
 * All other constants tune timeouts, window sizes, and demo mode.
 */
public final class AppConfig {

    // ── Prevent instantiation ────────────────────────────────
    private AppConfig() {}

    // ── API ──────────────────────────────────────────────────

    /** Root URL of the CampusFlow REST API (no trailing slash). */
    public static final String BASE_URL = "http://localhost:8000";

    /** HTTP connect timeout (seconds). */
    public static final int CONNECT_TIMEOUT = 10;

    /** HTTP read timeout (seconds). */
    public static final int READ_TIMEOUT    = 30;

    // ── Application meta ────────────────────────────────────

    public static final String APP_NAME    = "CampusFlow";
    public static final String APP_VERSION = "1.0.0";

    // ── Window ───────────────────────────────────────────────

    public static final double WINDOW_WIDTH  = 1280;
    public static final double WINDOW_HEIGHT = 780;
    public static final double SIDEBAR_WIDTH = 230;

    // ── Demo mode ────────────────────────────────────────────

    /**
     * When true, a "Mode Démo" shortcut appears on the login screen.
     * Bypasses the real API and loads static sample data.
     * Set to false for production deployments.
     */
    public static final boolean DEMO_MODE = true;

    public static final String DEMO_EMAIL    = "admin@campusflow.fr";
    public static final String DEMO_PASSWORD = "password";
    public static final String DEMO_USERNAME = "Admin Dupont";
    public static final String DEMO_ROLE     = "Administrateur";
}
