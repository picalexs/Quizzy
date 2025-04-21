package com.backend.service;

import com.backend.model.Enrollment;
import com.backend.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.util.Collection;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public Collection<Enrollment> getAllEnrollments() {
        return enrollmentRepository.getAllEnrollments();
    }

    public Collection<Enrollment> getALLEnrollmentsByUserId(Integer id) {
        return enrollmentRepository.findByUserId(id);
    }
    @Transactional
    public void addEnrollment(Integer userId, Long courseId) {
        enrollmentRepository.insertEnrollment(userId,courseId);
    }

}
