package com.backend.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "flashcardsession")
public class FlashcardSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sessionid")
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private Date timestamp;

    @Column(name = "flashcardcount", nullable = false)
    private int flashcardCount;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "courseid", nullable = false)
    private Course course;
}

