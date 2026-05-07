module com.campusflow {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // Java built-in HTTP client
    requires java.net.http;

    // Jackson
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    // Open model package to Jackson for reflection-based deserialization
    opens com.campusflow.models to com.fasterxml.jackson.databind;

    // Open controller package to javafx.fxml for @FXML injection
    opens com.campusflow.controllers to javafx.fxml;
    opens com.campusflow.app         to javafx.fxml, javafx.graphics;

    // Export all application packages
    exports com.campusflow.app;
    exports com.campusflow.controllers;
    exports com.campusflow.models;
    exports com.campusflow.services;
    exports com.campusflow.api;
    exports com.campusflow.utils;
    exports com.campusflow.config;
}
