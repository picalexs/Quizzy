package com.backend.dto;

import lombok.Data;
import java.util.Date;

@Data
public class EnrollmentDTO {
    private Integer userId;
    private Long courseId;
    private Date enrollmentDate;
    private String grade;
}