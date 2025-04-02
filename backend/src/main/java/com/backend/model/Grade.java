package com.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "grade")
public class Grade {

    @EmbeddedId
    private GradeId id;

    @Column(name = "grade", nullable = false)
    private float grade;

    @Column(name = "submissiondate", nullable = false)
    private Date submissionDate;

    @ManyToOne
    @JoinColumn(name = "testid")
    @MapsId("testID")
    private Test test;

    @ManyToOne
    @JoinColumn(name = "userid")
    @MapsId("userID")
    private User user;
}