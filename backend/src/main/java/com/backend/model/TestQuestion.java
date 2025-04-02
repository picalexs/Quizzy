package com.backend.model;
import jakarta.persistence.*;

@Entity
@Table(name = "testquestion")
public class TestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "questionid")
    private Long id;

    @Column(name = "questiontext", nullable = false)
    private String questionText;

    @Column(name = "pointvalue", nullable = false)
    private float pointValue;

    @ManyToOne
    @JoinColumn(name = "testid", nullable = false)
    private Test test;
}
