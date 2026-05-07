package com.campusflow.api;

/**
 * ApiException — thrown by ApiClient for HTTP or network failures.
 *
 * Carries the HTTP status code (0 = no response / network error)
 * so callers can decide whether to show an auth error, retry, etc.
 */
public class ApiException extends Exception {

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public ApiException(String message, int statusCode, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /** HTTP status code; 0 means network/IO failure before a response arrived. */
    public int getStatusCode() { return statusCode; }

    /** True when the server returned 401 Unauthorized. */
    public boolean isAuthError()    { return statusCode == 401; }

    /** True when no HTTP response was received (timeout, server down, etc.). */
    public boolean isNetworkError() { return statusCode == 0; }

    @Override public String toString() {
        return "ApiException[" + statusCode + "]: " + getMessage();
    }
}
