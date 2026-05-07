package com.campusflow.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Review / Feedback model — maps to GET /api/reviews response items.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Review {

    @JsonProperty("id")           private int    id;
    @JsonProperty("student_id")   private int    studentId;
    @JsonProperty("student_name") private String studentName;
    @JsonProperty("course_id")    private int    courseId;
    @JsonProperty("course_name")  private String courseName;
    @JsonProperty("rating")       private double rating;       // 1.0 – 5.0
    @JsonProperty("comment")      private String comment;
    @JsonProperty("created_at")   private String createdAt;

    public Review() {}

    // ── Computed ──────────────────────────────────────────────
    public String getStarRating() {
        int full  = (int) Math.floor(rating);
        int empty = 5 - full;
        return "★".repeat(Math.max(0, full)) + "☆".repeat(Math.max(0, empty));
    }
    public double getRatingPercent() { return (rating / 5.0) * 100.0; }

    // ── Getters ───────────────────────────────────────────────
    public int    getId()           { return id; }
    public int    getStudentId()    { return studentId; }
    public String getStudentName()  { return studentName; }
    public int    getCourseId()     { return courseId; }
    public String getCourseName()   { return courseName; }
    public double getRating()       { return rating; }
    public String getComment()      { return comment; }
    public String getCreatedAt()    { return createdAt; }

    // ── Setters ───────────────────────────────────────────────
    public void setId(int id)               { this.id = id; }
    public void setStudentId(int v)         { this.studentId = v; }
    public void setStudentName(String v)    { this.studentName = v; }
    public void setCourseId(int v)          { this.courseId = v; }
    public void setCourseName(String v)     { this.courseName = v; }
    public void setRating(double v)         { this.rating = v; }
    public void setComment(String v)        { this.comment = v; }
    public void setCreatedAt(String v)      { this.createdAt = v; }

    @Override public String toString() {
        return "Review{id=" + id + ", course=" + courseName + ", rating=" + rating + "}";
    }
}
