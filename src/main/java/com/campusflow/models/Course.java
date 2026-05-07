package com.campusflow.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Course / Formation model — maps to GET /api/courses response items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    @JsonProperty("id")             private int    id;
    @JsonProperty("name")           private String name;
    @JsonProperty("level")          private String level;       // BTS, Bachelor, Licence, Master
    @JsonProperty("domain")         private String domain;
    @JsonProperty("enrolled")       private int    enrolled;
    @JsonProperty("capacity")       private int    capacity;
    @JsonProperty("duration")       private String duration;
    @JsonProperty("average_grade")  private double averageGrade;

    public Course() {}

    // ── Computed ──────────────────────────────────────────────
    public double getFillPercentage() {
        return capacity == 0 ? 0 : Math.min(100.0, (enrolled * 100.0) / capacity);
    }
    public int getAvailableSeats() {
        return Math.max(0, capacity - enrolled);
    }

    // ── Getters ───────────────────────────────────────────────
    public int    getId()            { return id; }
    public String getName()          { return name; }
    public String getLevel()         { return level; }
    public String getDomain()        { return domain; }
    public int    getEnrolled()      { return enrolled; }
    public int    getCapacity()      { return capacity; }
    public String getDuration()      { return duration; }
    public double getAverageGrade()  { return averageGrade; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)               { this.id = id; }
    public void setName(String v)           { this.name = v; }
    public void setLevel(String v)          { this.level = v; }
    public void setDomain(String v)         { this.domain = v; }
    public void setEnrolled(int v)          { this.enrolled = v; }
    public void setCapacity(int v)          { this.capacity = v; }
    public void setDuration(String v)       { this.duration = v; }
    public void setAverageGrade(double v)   { this.averageGrade = v; }

    @Override public String toString() {
        return "Course{id=" + id + ", name=" + name + ", level=" + level + "}";
    }
}
