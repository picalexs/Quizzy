package com.backend.model;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "flashcard")
public class Flashcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flashcardid")
    private Long id;

    @Column(name = "question", nullable = false)
    private String question;

    @Column(name = "level")
    private int level;

    @Column(name = "laststudiedat")
    private Date lastStudiedAt;

    @Column(name = "questiontype")
    private String questionType;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "materialid")
    private Material material;

    @OneToMany(mappedBy = "flashcard", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AnswerFC> answers;
}