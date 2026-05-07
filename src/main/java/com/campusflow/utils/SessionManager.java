package com.campusflow.utils;

import com.campusflow.config.AppConfig;

/**
 * SessionManager — singleton that holds the active user session.
 *
 * Stores the JWT token after login and clears it on logout or
 * when a 401 response is received from the API.
 * No disk persistence — session resets on each application launch.
 */
public class SessionManager {

    private static SessionManager instance;

    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    private SessionManager() {}

    // ── Session fields ────────────────────────────────────────

    private String  token;
    private String  userName;
    private String  userEmail;
    private String  userRole;
    private boolean loggedIn;
    private boolean demoMode;

    // ── Session lifecycle ─────────────────────────────────────

    /** Called after a successful real API login. */
    public void startSession(String token, String userName, String userEmail, String userRole) {
        this.token     = token;
        this.userName  = userName;
        this.userEmail = userEmail;
        this.userRole  = userRole;
        this.loggedIn  = true;
        this.demoMode  = false;
    }

    /** Called when the user chooses demo mode (no real API). */
    public void startDemoSession() {
        this.token     = "demo-token";
        this.userName  = AppConfig.DEMO_USERNAME;
        this.userEmail = AppConfig.DEMO_EMAIL;
        this.userRole  = AppConfig.DEMO_ROLE;
        this.loggedIn  = true;
        this.demoMode  = true;
    }

    /** Clears all session data (logout / 401 / app exit). */
    public void clearSession() {
        this.token     = null;
        this.userName  = null;
        this.userEmail = null;
        this.userRole  = null;
        this.loggedIn  = false;
        this.demoMode  = false;
    }

    // ── Getters ───────────────────────────────────────────────

    public String  getToken()     { return token; }
    public String  getUserName()  { return userName; }
    public String  getUserEmail() { return userEmail; }
    public String  getUserRole()  { return userRole; }
    public boolean isLoggedIn()   { return loggedIn; }
    public boolean isDemoMode()   { return demoMode; }

    /**
     * Returns up-to-two-letter initials for avatar display.
     * "Admin Dupont" → "AD"
     */
    public String getInitials() {
        if (userName == null || userName.isBlank()) return "?";
        String[] parts = userName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
