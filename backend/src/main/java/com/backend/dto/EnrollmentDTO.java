package com.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class EnrollmentDTO {
    private Integer userId;
    private Long courseId;
    private Date enrollmentDate;
    private String grade;

    public void setUserId(Long userId) {
        this.userId = userId;
    }
    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }
    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public Long getUserId() {
        return userId;
    }
    public Long getCourseId() {
        return courseId;
    }
    public Date getEnrollmentDate() {
        return enrollmentDate;
    }
    public String getGrade() {
        return grade;
    }
}