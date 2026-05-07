package com.campusflow.api;

import com.campusflow.config.AppConfig;
import com.campusflow.utils.SessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * ApiClient — singleton wrapper around Java's built-in {@link HttpClient}.
 *
 * Responsibilities:
 * <ul>
 *   <li>Build and send GET / POST HTTP requests.</li>
 *   <li>Automatically attach {@code Authorization: Bearer TOKEN} when logged in.</li>
 *   <li>Map non-2xx responses to typed {@link ApiException}s.</li>
 *   <li>Expose a shared Jackson {@link ObjectMapper} for all JSON work.</li>
 * </ul>
 */
public class ApiClient {

    // ── Singleton ─────────────────────────────────────────────

    private static ApiClient instance;

    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }

    // ── Fields ────────────────────────────────────────────────

    private final HttpClient   httpClient;
    private final ObjectMapper objectMapper;

    // ── Constructor ───────────────────────────────────────────

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(AppConfig.CONNECT_TIMEOUT))
            .build();

        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    }

    // ── Public interface ──────────────────────────────────────

    /**
     * Authenticated GET request.
     *
     * @param path  path relative to {@link AppConfig#BASE_URL} (e.g. "/api/students")
     * @return      raw response body string
     * @throws ApiException on HTTP error or network failure
     */
    public String get(String path) throws ApiException {
        HttpRequest request = baseBuilder(path)
            .GET()
            .build();
        return send(request);
    }

    /**
     * Unauthenticated POST (used for /auth/login).
     *
     * @param path  path relative to BASE_URL
     * @param json  request body as JSON string
     * @return      raw response body string
     * @throws ApiException on HTTP error or network failure
     */
    public String postAnon(String path, String json) throws ApiException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri(path))
            .timeout(Duration.ofSeconds(AppConfig.READ_TIMEOUT))
            .header("Accept",       "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return send(request);
    }

    /**
     * Authenticated POST.
     */
    public String post(String path, String json) throws ApiException {
        HttpRequest request = baseBuilder(path)
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();
        return send(request);
    }

    /** Shared Jackson ObjectMapper (thread-safe after configuration). */
    public ObjectMapper getMapper() { return objectMapper; }

    // ── Internal ──────────────────────────────────────────────

    /** Creates a request builder pre-loaded with common headers + auth token. */
    private HttpRequest.Builder baseBuilder(String path) {
        HttpRequest.Builder b = HttpRequest.newBuilder()
            .uri(uri(path))
            .timeout(Duration.ofSeconds(AppConfig.READ_TIMEOUT))
            .header("Accept",       "application/json")
            .header("Content-Type", "application/json");

        String token = SessionManager.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            b.header("Authorization", "Bearer " + token);
        }
        return b;
    }

    /** Executes a request and returns the body, throwing on non-2xx. */
    private String send(HttpRequest request) throws ApiException {
        try {
            HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkStatus(response);
            return response.body();
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException("Network error: " + e.getMessage(), 0, e);
        }
    }

    /** Maps HTTP error codes to descriptive ApiExceptions. */
    private void checkStatus(HttpResponse<String> resp) throws ApiException {
        int code = resp.statusCode();
        if (code == 401) {
            SessionManager.getInstance().clearSession();
            throw new ApiException("Session expirée — veuillez vous reconnecter.", 401);
        }
        if (code == 403) throw new ApiException("Accès refusé (403).", 403);
        if (code == 404) throw new ApiException("Ressource introuvable (404).", 404);
        if (code >= 500) throw new ApiException("Erreur serveur (" + code + ").", code);
        if (code < 200 || code >= 300) {
            throw new ApiException("Erreur HTTP " + code + ": " + resp.body(), code);
        }
    }

    private URI uri(String path) {
        return URI.create(AppConfig.BASE_URL + path);
    }
}
