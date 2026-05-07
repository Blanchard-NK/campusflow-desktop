package com.campusflow.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Student model — maps to GET /api/students response items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

    @JsonProperty("id")          private int    id;
    @JsonProperty("first_name")  private String firstName;
    @JsonProperty("last_name")   private String lastName;
    @JsonProperty("email")       private String email;
    @JsonProperty("formation")   private String formation;
    @JsonProperty("speciality")  private String speciality;
    @JsonProperty("year")        private String year;
    @JsonProperty("status")      private String status;

    public Student() {}

    // ── Getters ───────────────────────────────────────────────
    public int    getId()           { return id; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public String getEmail()        { return email; }
    public String getFormation()    { return formation; }
    public String getSpeciality()   { return speciality; }
    public String getYear()         { return year; }
    public String getStatus()       { return status; }
    public String getFullName()     { return firstName + " " + lastName; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)               { this.id = id; }
    public void setFirstName(String v)      { this.firstName = v; }
    public void setLastName(String v)       { this.lastName = v; }
    public void setEmail(String v)          { this.email = v; }
    public void setFormation(String v)      { this.formation = v; }
    public void setSpeciality(String v)     { this.speciality = v; }
    public void setYear(String v)           { this.year = v; }
    public void setStatus(String v)         { this.status = v; }

    @Override public String toString() {
        return "Student{id=" + id + ", name=" + getFullName() + "}";
    }
}
