package com.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "enrollment")
public class Enrollment {

    @EmbeddedId
    private EnrollmentId id;

    @ManyToOne
    @MapsId("userID")
    @JoinColumn(name = "userid")
    private User user;

    @ManyToOne
    @MapsId("courseID")
    @JoinColumn(name = "courseid")
    private Course course;

    @Column(name = "enrollmentdate", nullable = false)
    private Date enrollmentDate;

    @Column(name = "grade")
    private String grade;
}
