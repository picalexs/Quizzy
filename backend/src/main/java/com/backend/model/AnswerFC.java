package com.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "answerfc")
public class AnswerFC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answerid")
    private Long id;

    @Column(name = "optiontext", nullable = false)
    private String optionText;

    @Column(name = "iscorrect", nullable = false)
    private boolean isCorrect;

    @ManyToOne
    @JoinColumn(name = "flashcardid", nullable = false)
    private Flashcard flashcard;
}