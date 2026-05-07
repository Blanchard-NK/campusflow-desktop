package com.campusflow.models;

import java.util.*;
import java.util.stream.Collectors;

/**
 * DashboardStats — client-side aggregation of raw API lists.
 *
 * Computed once after all data is fetched; immutable after construction.
 */
public class DashboardStats {

    private final int    totalStudents;
    private final int    totalTeachers;
    private final int    totalCourses;
    private final int    totalReviews;
    private final double globalSatisfaction;        // 0–100 %

    private final Map<String, Long>   studentsByLevel;       // "BTS" → count
    private final Map<String, Double> avgGradePerCourse;     // courseName → avg
    private final Map<String, Double> avgSatPerCourse;       // courseName → avg rating
    private final Map<String, Long>   activityByMonth;       // "2024-10" → count

    // ── Constructor ───────────────────────────────────────────

    public DashboardStats(List<Student> students,
                          List<Teacher> teachers,
                          List<Course>  courses,
                          List<Review>  reviews) {

        this.totalStudents = students.size();
        this.totalTeachers = teachers.size();
        this.totalCourses  = courses.size();
        this.totalReviews  = reviews.size();

        this.globalSatisfaction = reviews.isEmpty() ? 0 :
            reviews.stream().mapToDouble(Review::getRating).average().orElse(0) / 5.0 * 100.0;

        this.studentsByLevel = students.stream()
            .collect(Collectors.groupingBy(
                s -> s.getFormation() != null ? s.getFormation() : "Autre",
                LinkedHashMap::new, Collectors.counting()));

        this.avgGradePerCourse = courses.stream()
            .filter(c -> c.getAverageGrade() > 0)
            .collect(Collectors.toMap(
                Course::getName, Course::getAverageGrade,
                (a, b) -> a, LinkedHashMap::new));

        this.avgSatPerCourse = reviews.stream()
            .filter(r -> r.getCourseName() != null && !r.getCourseName().isBlank())
            .collect(Collectors.groupingBy(
                Review::getCourseName, LinkedHashMap::new,
                Collectors.averagingDouble(Review::getRating)));

        this.activityByMonth = reviews.stream()
            .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().length() >= 7)
            .collect(Collectors.groupingBy(
                r -> r.getCreatedAt().substring(0, 7),
                LinkedHashMap::new, Collectors.counting()));
    }

    // ── Getters ───────────────────────────────────────────────

    public int    getTotalStudents()            { return totalStudents; }
    public int    getTotalTeachers()            { return totalTeachers; }
    public int    getTotalCourses()             { return totalCourses; }
    public int    getTotalReviews()             { return totalReviews; }
    public double getGlobalSatisfaction()       { return globalSatisfaction; }
    public Map<String, Long>   getStudentsByLevel()     { return studentsByLevel; }
    public Map<String, Double> getAvgGradePerCourse()   { return avgGradePerCourse; }
    public Map<String, Double> getAvgSatPerCourse()     { return avgSatPerCourse; }
    public Map<String, Long>   getActivityByMonth()     { return activityByMonth; }

    public String getFormattedSatisfaction() {
        return String.format("%.1f%%", globalSatisfaction);
    }
}
