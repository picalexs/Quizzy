package com.backend.model;

import jakarta.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "streak")
public class Streak {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "streakid")
    private Long id;

    @Column(name = "currentstreak", nullable = false)
    private int currentStreak;

    @Column(name = "lastcompleteddate", nullable = false)
    private Date lastCompletedDate;

    @ManyToOne
    @JoinColumn(name = "userid", nullable = false)
    private User user;
}