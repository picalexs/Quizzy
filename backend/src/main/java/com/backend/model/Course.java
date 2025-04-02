package com.backend.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "courseid")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "semestru")
    private String semestru;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private User professor;

    @OneToMany(mappedBy = "course")
    private Set<Material> materials;

    @OneToMany(mappedBy = "course")
    private Set<Enrollment> enrollments;

    @OneToMany(mappedBy = "course")
    private Set<Test> tests;

    @OneToMany(mappedBy = "course")
    private Set<FlashcardSession> flashcardSessions;
}