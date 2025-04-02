package com.backend.model;

import jakarta.persistence.*;

@Embeddable
class EnrollmentId implements java.io.Serializable {
    private Long userID;
    private Long courseID;
}
