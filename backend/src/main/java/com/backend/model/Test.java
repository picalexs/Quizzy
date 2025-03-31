package com.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "test")
public class Test {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "testid")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "date", nullable = false)
    private Date date;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private User professor;

    @ManyToOne
    @JoinColumn(name = "courseid", nullable = false)
    private Course course;
}
