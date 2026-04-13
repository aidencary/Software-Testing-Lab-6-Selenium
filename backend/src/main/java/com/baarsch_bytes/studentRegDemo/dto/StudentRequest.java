package com.baarsch_bytes.studentRegDemo.dto;

import com.baarsch_bytes.studentRegDemo.model.Course;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class StudentRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
    private String name;

    @NotBlank(message = "Major is required")
    @Size(min = 1, max = 255, message = "Major must be between 1 and 255 characters")
    private String major;

    @DecimalMin(value = "0.0", message = "No negative GPAs allowed")
    @DecimalMax(value = "4.0", message = "No GPAs above 4.0 allowed")
    private Double gpa;
    private Set<Long> courses;

    public StudentRequest(){}

    public StudentRequest(String name, String major, Double gpa, Set<Long> courses) {
        this.name = name;
        this.major = major;
        this.gpa = gpa;
        this.courses = courses;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Double getGpa() {
        return gpa;
    }

    public void setGpa(Double gpa) {
        this.gpa = gpa;
    }

    public Set<Long> getCourses() {
        return courses;
    }

    public void setCourses(Set<Long> courses) {
        this.courses = courses;
    }
}
