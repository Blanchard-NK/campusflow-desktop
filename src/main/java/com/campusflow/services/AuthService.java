package com.campusflow.services;

import com.campusflow.api.ApiClient;
import com.campusflow.api.ApiException;
import com.campusflow.utils.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * AuthService — handles login and logout against the CampusFlow API.
 *
 * <pre>POST /auth/login → { "token": "...", "user": { "name": "...", "email": "..." } }</pre>
 */
public class AuthService {

    private static AuthService instance;

    public static synchronized AuthService getInstance() {
        if (instance == null) instance = new AuthService();
        return instance;
    }

    private AuthService() {}

    private final ApiClient client = ApiClient.getInstance();

    // ── Public API ────────────────────────────────────────────

    /**
     * Attempts a real API login.
     *
     * @param email    user email
     * @param password user password
     * @throws ApiException if credentials are wrong or network fails
     */
    public void login(String email, String password) throws ApiException {
        String body = String.format(
            "{\"email\":\"%s\",\"password\":\"%s\"}",
            escapeJson(email),
            escapeJson(password)
        );

        String response = client.postAnon("/auth/login", body);

        try {
            JsonNode root = client.getMapper().readTree(response);

            // Accept both "token" and "access_token" field names
            String token = null;
            if (root.has("token"))        token = root.get("token").asText();
            else if (root.has("access_token")) token = root.get("access_token").asText();

            if (token == null || token.isBlank()) {
                throw new ApiException("Réponse invalide : aucun token reçu.", 200);
            }

            // Parse user info (optional fields)
            String name  = email; // fallback
            String role  = "Utilisateur";
            if (root.has("user")) {
                JsonNode user = root.get("user");
                if (user.has("name"))  name  = user.get("name").asText();
                if (user.has("email")) email  = user.get("email").asText();
                if (user.has("role"))  role   = user.get("role").asText();
            }

            SessionManager.getInstance().startSession(token, name, email, role);

        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ApiException("Impossible d'analyser la réponse: " + ex.getMessage(), 0, ex);
        }
    }

    /** Clears the local session without making an API call (stateless JWT). */
    public void logout() {
        SessionManager.getInstance().clearSession();
    }

    // ── Helpers ───────────────────────────────────────────────

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
