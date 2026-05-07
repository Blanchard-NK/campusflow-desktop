package com.campusflow.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Teacher model — maps to GET /api/teachers response items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Teacher {

    @JsonProperty("id")              private int    id;
    @JsonProperty("first_name")      private String firstName;
    @JsonProperty("last_name")       private String lastName;
    @JsonProperty("email")           private String email;
    @JsonProperty("speciality")      private String speciality;
    @JsonProperty("formations")      private String formations;
    @JsonProperty("status")          private String status;
    @JsonProperty("hours_per_week")  private int    hoursPerWeek;

    public Teacher() {}

    // ── Getters ───────────────────────────────────────────────
    public int    getId()            { return id; }
    public String getFirstName()     { return firstName; }
    public String getLastName()      { return lastName; }
    public String getEmail()         { return email; }
    public String getSpeciality()    { return speciality; }
    public String getFormations()    { return formations; }
    public String getStatus()        { return status; }
    public int    getHoursPerWeek()  { return hoursPerWeek; }
    public String getFullName()      { return firstName + " " + lastName; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)                 { this.id = id; }
    public void setFirstName(String v)        { this.firstName = v; }
    public void setLastName(String v)         { this.lastName = v; }
    public void setEmail(String v)            { this.email = v; }
    public void setSpeciality(String v)       { this.speciality = v; }
    public void setFormations(String v)       { this.formations = v; }
    public void setStatus(String v)           { this.status = v; }
    public void setHoursPerWeek(int v)        { this.hoursPerWeek = v; }

    @Override public String toString() {
        return "Teacher{id=" + id + ", name=" + getFullName() + "}";
    }
}
