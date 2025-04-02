package com.backend.model;

import jakarta.persistence.Embeddable;

@Embeddable
class GradeId implements java.io.Serializable {
    private Long testID;
    private Long userID;
}